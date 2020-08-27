package taomp.ch9;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
    Aim: to avoid retraversing the list in absence of contention (vs OptimisticList).
    Also make contains wait-free i.e all concurrent calls to it will complete in a finite number of steps.
    Note this wait-free is possible only if the list can have a finite number of elements in the possible
    object space. For example for integers there's a valid min, max.
    So if this queue is for a string, will the algorithm not be wait-free?
    Since maybe concurrent adders keep adding new and new elements that sort at the end?

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
    Yes. Since when add is checking if pred is marked or curr is marked,
    in between these two checks again we don't want any deletes to happen.
    Lets say after checking pred isn't marked, we're checking curr isn't marked.
    But meanwhile pred gets deleted, then we'd insert a new node in between pred and curr
    which will be unreachable.
 */
public class LazyList implements SortedList {

    class Node {
        Node next;
        int value;
        Lock lock;
        boolean marked;

        Node(Node next, int value) {
            this.value = value;
            this.next = next;
            lock = new ReentrantLock();
        }
    }

    Node head;

    LazyList() {
        head = new Node(null, Integer.MIN_VALUE);
        head.next = new Node(null, Integer.MAX_VALUE);
    }

    // This is the big win! After obtaining the locks on pred, curr we know they can't be removed.
    // So all add needs to check is whether these references have been removed from the list.
    // Which now it can simply check using the marked flag. It is delete's responsibility
    // to first mark the nodes it wants to remove and only then remove it.
    boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
    }

    @Override
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
                // In case the node we found is removed, after the locks
                // we'd have a happens before on pred, curr.
                // So now we can safely check the marked values and know
                // whether the found node is removed or not.
                // If the found node is removed we start our traversal again
                // and since it is removed, this time the marked node won't
                // be reachable since it must've been unlinked.
                // Therefore an add for a removed item will succeed.
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

    @Override
    public boolean remove(int item) {
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
                // Q: What happens if there's a node that is already removed?
                // And we try to remove it again?
                // If any remover finds it, it'll find it as marked already and so
                // will restart the traversal and next traversal won't find it.
                // Since the node must have been unlinked.
                if (validate(pred, curr)) {
                    if (curr.value != item) {
                        return false;
                    }
                    curr.marked = true;
                    pred.next = curr.next;
                    return true;
                }
            } finally {
                curr.lock.unlock();
                pred.lock.unlock();
            }
        }
    }

    @Override
    public boolean contains(int item) {
        // Q: There's no happens before? So even if I add one node, contains might never see the node?
        // There is no memory barrier?
        // From a theory perspective this is fine, since we'll "linearize" all such unsuccessful contains calls before
        // the write. But what if some threads see the contains as successful and hence linearize it?
        // What about other threads not being able to see it?
        Node curr = head;
        while (curr.value < item) {
            curr = curr.next;
        }
        return !curr.marked && curr.value == item;
    }
}
