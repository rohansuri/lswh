package taomp.ch9;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
    Aim: to avoid retraversing the list in absence of contention (vs OptimisticList).
    Also make contains wait-free i.e all concurrent calls to it will complete in a finite number of steps.

    The main improvement we want to make to OptimisticList is that it traverses the list twice,
    even in absence of contention to check the reachability of pred from head.
    In which case will the reachability be hampered?
    When pred is removed.
    Now an adder cannot know that pred is removed unless it checks the reachability from head.
    BUT if we introduce some new state into the node i.e. a marked or deleted field which will be marked
    once the node has been deleted, in cases when pred isn't marked, adder doesn't need to
    retraverse the list! This is a big win in the non contended case. Imagine otherwise having to traverse
    the list everytime twice!

    OptmisticList was beneficial only when cost to traverse list twice is less than traversing list
    once with locks (FineList).

    So that's the plan in lazy list. A delete not only removes the node BUT first marks it as deleted,
    so that any current threads working/using/referring to a deleted node can know if they need
    to check reachability again or not.

    Q: So where do we restart traversal from if an add detects that pred has been marked as deleted?
    Since this is a single linked list, we cannot back traverse from pred to find the previous node
    that satisfies the "least node than to be inserted node" criteria.
    So it is safest to simply start from head again.

    Do we still need the per node lock?
    I think yes. Since when add is checking if pred is marked or curr is marked,
    in between these two checks again we don't want any deletes to happen.
    Lets say after checking pred isn't marked, we're checking curr isn't marked.
    But meanwhile pred gets deleted, then we'd insert a new node in between pred and curr
    which will be unreachable.
 */
public class LazyList implements SortedList {

    class Node {
        AtomicMarkableReference<Node> next;
        int value;
        Lock lock;

        Node(AtomicMarkableReference<Node> next, int value) {
            this.value = value;
            this.next = next;
            lock = new ReentrantLock();
        }
    }

    AtomicMarkableReference<Node> head;

    LazyList() {
        head = new AtomicMarkableReference<>(new Node(null, Integer.MIN_VALUE), false);
        head.getReference().next = new AtomicMarkableReference<>(new Node(null, Integer.MAX_VALUE), false);
    }

    // This is the big win! After obtaining the locks on pred, curr we know they can't be removed.
    // So all add needs to check is whether these references have been removed from the list.
    // Which now it can simply check using the marked flag. It is delete's responsibility
    // to first mark the nodes it wants to remove and only then remove it.
    boolean validate(AtomicMarkableReference<Node> pred, AtomicMarkableReference<Node> curr) {
        return !pred.isMarked() && !curr.isMarked() && pred.getReference().next == curr;
    }

    @Override
    public boolean add(int item) {
        while (true) {
            AtomicMarkableReference<Node> pred = head;
            AtomicMarkableReference<Node> curr = pred.getReference().next;
            while (curr.getReference().value < item) {
                pred = curr;
                curr = curr.getReference().next;
            }
            try {
                pred.getReference().lock.lock();
                curr.getReference().lock.lock();
                if (validate(pred, curr)) {
                    if (curr.getReference().value == item) {
                        return false;
                    }
                    AtomicMarkableReference<Node> newNode = new AtomicMarkableReference<>(new Node(curr, item), false);
                    pred.getReference().next = newNode;
                    return true;
                }
            } finally {
                curr.getReference().lock.unlock();
                pred.getReference().lock.unlock();
            }
        }
    }

    @Override
    public boolean remove(int item) {
        return false;
    }

    @Override
    public boolean contains(int item) {
        AtomicMarkableReference<Node> pred = head;
        AtomicMarkableReference<Node> curr = pred.getReference().next;
        while (curr.getReference().value < item) {
            pred = curr;
            curr = curr.getReference().next;
        }
        return validate(pred, curr) && curr.getReference().value == item;
    }
}
