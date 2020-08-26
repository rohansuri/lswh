package taomp.ch9;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
    Optimistic = get to the target node without locks, lock the pred, curr since both
    add and delete overlap on them (just like FineList) BUT confirm that both of them
    are still reachable from head.

    Note we don't need to acquire locks over all other nodes when validating whether pred, curr are reachable.
    This is because all we want to see is whether pred has been removed from the list
    OR maybe there's another added node between pred and curr which we may not
    have seen before when we first found pred, curr. Hence we verify that pred.next == curr.
    And pred is reachable from head. Also since we verify this while holding pred, curr locks
    we know no new node can come in between and neither pred nor curr can be removed.

    This is an improvement over FineList since for the initial "finding the right position"
    we let all concurrent threads to traverse the list. We no longer have the pipelined restriction.
    This permits the scenario where one thread is working on one section of the list
    and the other is working on some section later. THIS other wise would not be permitted in FineList,
    since that would pipeline the traversal of these two concurrent threads.

    So threads accessing non-adjacent nodes never interfere.

    Note: requires "absence from interference". Even nodes that are unlinked, their next pointers
    should not be touched so that if there's any traversals happening over them, they can
    continue to traverse the list.
    Well what else will happen? Lets say we set it to null?
    Usually we'll never access a null since we have a "last" sentinel as well.
    But now suddenly on our traversal we'll encounter a null?
    Is that the only downside? So atmost to handle this, we'll self check for nulls?
    Are there any correctness issues?
 */
public class OptimisticList implements SortedList {

    class Node {
        Node next;
        int value;
        Lock lock;

        Node(Node next, int value) {
            this.next = next;
            this.value = value;
            lock = new ReentrantLock();
        }
    }

    Node head;

    OptimisticList() {
        head = new Node(null, Integer.MIN_VALUE);
        head.next = new Node(null, Integer.MAX_VALUE);
    }

    boolean validate(Node pred, Node curr) {
        Node n = head;
        while (n.value <= pred.value) {
            if (n == pred) {
                return pred.next == curr;
            }
            n = n.next;
        }
        return false;
    }

    public boolean add(int item) {
        while (true) {
            Node pred = head;
            Node curr = pred.next;
            while (curr.value < item) {
                pred = curr;
                curr = curr.next;
            }
            try {
                pred.lock.lock();
                curr.lock.lock();
                if (validate(pred, curr)) {
                    if (curr.value == item) {
                        return false;
                    }
                    Node newNode = new Node(curr, item);
                    pred.next = newNode;
                    return true;
                }
            } finally {
                curr.lock.unlock();
                pred.lock.unlock();
            }
        }
    }

    public boolean remove(int item) {
        while (true) {
            Node pred = head;
            Node curr = pred.next;
            while (curr.value < item) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock.lock();
            curr.lock.lock();
            try {
                if (validate(pred, curr)) {
                    if (curr.value != item) {
                        return false;
                    }
                    pred.next = curr.next;
                    return true;
                }
            } finally {
                curr.lock.unlock();
                pred.lock.unlock();
            }
        }

    }

    public boolean contains(int item) {
        while (true) {
            Node pred = head;
            Node curr = pred.next;
            while (curr.value < item) {
                pred = curr;
                curr = curr.next;
            }
            try {
                pred.lock.lock();
                curr.lock.lock();
                if (validate(pred, curr)) {
                    return curr.value == item;
                }
            } finally {
                curr.lock.unlock();
                pred.lock.unlock();
            }
        }
    }
}
