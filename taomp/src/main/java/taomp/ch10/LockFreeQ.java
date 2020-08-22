package taomp.ch10;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;

// Chapter 10 from taomp.
@ThreadSafe
public class LockFreeQ<T> {
    static class Node<T> {
        T value;
        AtomicReference<Node<T>> next;

        Node(T value) {
            this.value = value;
            // AtomicReference to null.
            // This tells us if this node is a tail or not.
            // Since a tail doesn't have a next.
            this.next = new AtomicReference<Node<T>>(null);
        }
    }

    private AtomicReference<Node<T>> head, tail;

    public LockFreeQ() {
        // Our sentinel node.
        Node<T> sentinel = new Node<>(null);
        // This is important! The sentinel is a value, not a reference.
        // Else upon our initial insert itself, when tail is moved forward,
        // we'd incorrectly change head's reference as well!
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
    }

    public void put(T value) {
        /*
            create new node.
            get tail.
            set tail.next to new node.
            at this point the new node is linked...
            BUT we also need to update the tail for other concurrent writers to correctly
            keep extending the list.

            BUT what if after we've linked our new node and before we've set tail to our new node
            someone else reads the old tail and starts to link to it?
            So they somehow need to know that the tail is stale.
            The definition of tail is the its next should be null.
            But because our CAS of extending current tail has succeeded, that means,
            the current stale tail will point to the old tail which now has a next reference!
            So this way the concurrent writers know that they're reading a stale tail.

            What do they do after they've detected a stale tail? They keep moving the tail to the next node.
            So they keep forwarding the tail until we satisfy the tail invariant of tail's next being null.
         */
        Node<T> newNode = new Node<>(value);

        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();

            // Q: Why does Maurice check again that last == tail.get() ?
            // https://stackoverflow.com/questions/3873689/lock-free-queue-algorithm-repeated-reads-for-consistency

            // Is the tail we read even a valid tail?
            // If it has a next then it is not.
            if (next == null) {
                // last is the latest tail.
                // this means that at least what we've read is fine to build upon.
                // If we don't check this then we'll probably write our node to some intermediate node!
                // Infact the CAS being done here could simply be written as next.CAS(null, newNode).
                if (last.next.compareAndSet(next, newNode)) {
                    // If we succeed then it means newNode is the last linked node.
                    // Lets update tail now.
                    tail.compareAndSet(last, newNode);
                    // It is ok to not check whether we succeeded or not.
                    // BUT then we must make sure all other actors of this class
                    // check the invariant that tail.next is null.
                    // Question: why don't we self check the tail reset failed or not?
                    // We could BUT I guess it'll bring down the throughput.
                    // Also anyways if we failed it'd mean someone already changed tail and
                    // surely moved it ahead, so our job is done!
                    // Since the invariant of a put is that tail should either be on this new node
                    // or on some new node later.
                    // This is why Maurice calls this enqueue LAZY.
                    // This is the reason why in enqueue after a successful write to tail.next,
                    // when moving tail to the new node, we don’t check for CAS failure.
                    // because it’d mean someone else has already changed tail.
                    // and that’s ok as long as they are only moving the tail ahead.
                    // but we just have to be sure that if anyone else is moving the tail ahead they can only do that by extending on top of our new node.
                    // which we’re sure about since current tail’s next won’t be null anymore!

                    // We'll only fail to advance the tail if tail reference has already changed.
                    // But this can only mean that someone extended our last written linked node as well.
                    // So we leave the responsibility to them to move the tail ahead.
                    return;
                }
            } else {
                // seems a concurrent writer has extended the list BUT hasn't updated tail.
                // lets move tail forward.
                // NOTE: it is not a blind set! Since otherwise we might pull back the tail!
                // and kind of reset it and lose data! So we must only set it if we're sure
                // we're the ones who can forward it.
                tail.compareAndSet(last, next);
            }
        }
    }

    public void printAll(){
        System.out.print("Elements in queue: ");
        Node<T> n = head.get();
        while(n.next.get() != null) { // Sentinel.
            n = n.next.get();
            System.out.print(n.value + ", ");
        }
        System.out.println();
    }

    public T get() {
        /*
            set head to head.next.
            head is always a sentinel and it points to a next node which is the first node.
         */
        while (true) {


            Node<T> first = head.get();
            Node<T> next = first.next.get();
            Node<T> last = tail.get();
            // We need to check if tail is on pointing to head.
            // In which case we will need to move tail ahead as well.
            // Else we'll move head but tail will move back to head...
            // and tail will never be reachable.
            if (first == last) {
                if (next == null) {
                    // Empty queue? Would this mean tail == head?
                    // Can next == null happen alone in any case?

                    // Q: What to return on empty element? Depends what we want the behaviour to be in case of empty queue.
                    // Java's collections return null. Maurice throws an EmptyException.

                    // One would think OH we're just checking a snapshot, local copied value
                    // and on basis of that deciding whether queue is empty.
                    // But that is fine at the instant the caller called remove since empty is a valid answer.
                    return null;
                }
                // If not then tail is lagging, lets help it.
                // Do we need to check if this CAS succeeded or failed?
                // If we succeed then great we moved tail ahead...
                // If we didn't, then are we sure someone else moved it ahead?
                // I guess so..since the only actor that can move the tail ahead is the writer.
                // And if it does move the current tail ahead, well and good.
                // We just have to make sure from a remover's point of view that we don't
                // break the chain by removing a tail and making all intermediate nodes unreachable.
                tail.compareAndSet(last, next);
                continue;
            }

            if (head.compareAndSet(first, next)) {
                return next.value; // Head is just a sentinel, it is the next node which has the actual value.
            }
        }
    }
}
