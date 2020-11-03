package taomp.ch9;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;

/*
    Aim: To be completely non blocking.
    LazyList is still blocking after finding the matching node to mark the node and change next references.

    Just to revisit the manipulations add and remove require...
    Add needs to create a new node, set pred.next to newnode and newnode.next to curr.
    While this is going on another new node shouldn't come in between.
    How do we ensure that?
    If we CAS on pred.next we're safe from concurrent adders to not be able to add
    a new node in between.

    How do deletes interfere? While we're adding the new node, the pred might be removed.
    So we need to CAS on that next doesn't change as well as pred doesn't get marked.
    Then we're good to set pred.next to newNode.
    Also newNode's next must be set to curr.
    What if curr is removed?
    Then we'll break the chain.
    This is the part where all method calls "need to tolerate" each other's effects.
    And help each other. This is the basis of lock-free. This is the common theme.
    For example in this case, add can still go ahead and set newNode.next to curr and yes curr maybe removed.
    BUT when later traversals happen, they will know and check that curr is a marked node so lets remove it.
    So contains, adds, even deletes must help pending unlinks i.e. deletes.

    How would this helping look like?
    If you find that node is marked, then atomically change pred.next to curr.next.
    Then move onto curr.next.
    Check again if it is marked and so on...
 */
public class LockFreeList implements SortedList {

    class Node {
        AtomicMarkableReference<Node> next;
        int value;

        Node(AtomicMarkableReference<Node> next, int value) {
            this.value = value;
            this.next = next;
        }
    }

    AtomicMarkableReference<Node> head;

    LockFreeList() {
        head = new AtomicMarkableReference<>(new Node(null, Integer.MIN_VALUE), false);
        head.getReference().next = new AtomicMarkableReference<>(new Node(null, Integer.MAX_VALUE), false);
    }

    @Override
    public boolean add(int item) {
        return false;
    }

    @Override
    public boolean remove(int item) {
        // It'd be easier to start with removes.
        // To form the idea of how removes are going to happen.
        // And how others will have to help removes.

        /*
            Find the matching node. Mark it. Then change pred.next to curr.next.
            Since the node to be deleted and pred.next both are separate memories and CAS acts on only one.
            We can only do one of these steps and make it the linearization point.
            After which there'll be a side effect left which all other actors have to take care of.
            Seems marking the node atomically is the right linearization point.
            Later everyone can help remove it.

            So it could happen that after we've marked curr, someone else sees this
            and tries to unlink it. Unlink means removing a deleted node from the
            reachable path. So to do that pred.next is to be set to curr.next.
            Now what if curr.next is marked?
            That would mean again someone needs to unlink it.
            Can someone be on curr.next and trying to mark it as deleted?
            Yes. Imagine if two adjacent nodes are being deleted.
            1 -> 2 -> 3 -> 4.
            2 and 3 both are being deleted.
            So both get marked.
            Then final list should be 1 -> 4.
            So what can intermediate lists be when either of their removes overlap?
            1 -> [2] -> 4 when 3's thread removes 3.
            Later 1 -> 4 when 2's thread removes 2.

            OR

            1 -> [3] -> 4.
                 /
            [2]--

            i.e. 2 got unlinked.
            But 3's thread pred is still 2.
            So it makes 2 point to 4.
            1 -> [3] -> 4.
                        /
                   [2]--

            so 1 is still left pointing to 3.
            And someone in future will have to do this cleanup.

            What are the consequences of changing a marked node's next reference?
            Doesn't seem like only deletes have any consequence.
            Do adds have any?
            So if 1 -> 2 -> 4
            And we were adding 3 between 2 and 4.
            Concurrently someone deleted 2.
            Mark 2.
            1 -> [2] -> 4.
            3's pred is 2 and next is 4.
            1 -> [2] -> 3 -> 4.
            But now when 2 was being removed, pred was 1 and 2.next was 4.
            So 2's thread would make the list as.
            1 -> 4
                /
            3 --
            and 3 would be unreachable!
            Why did this happen?
            Since someone changed 2's next without checking if it is marked.
            If it would've known, then we know 3 cannot be 2's next.
            Also if we do that then we're risking 3's reachability on
            2's reachability. Which is dangerous since 2 can be unlinked anytime.
            Hence 3 must restart its add to find the correct place to add.
            And also when changing pred.next's reference, pred itself should be marked.

            CAS on pred where its next ref is what we read before and mark bit isn't set.
            Then we CAS pred with a new pred whose next reference is changed.
            Note: we CAS on pred by proposing a new pred reference with next field changed.
            So add only needs to ensure this, pred not getting marked and its next field is unchanged.

            So we know why a marked node's next reference shouldn't be changed in case of adds.
            BUT is it safe to change marked node's next reference in case of concurrent deletes?
            1 -> 2 -> 3 -> 4 -> 5 -> 6.
            4,5 are being concurrently deleted.
            All execute their first marked step.
            1 -> 2 -> 3 -> [4] -> [5] -> 6.
            Next all try to set their pred.next to curr.next without checking if pred has been deleted.
            So if first 5's thread makes it pred.next to be curr.next, we'd have:

            1 -> 2 -> 3 -> [4] ->  6
                                   /
                              [5]--
            Next 4's thread unlinks 4.
            But its local view of 4's next is 5 -- which has been unlinked already.
            1 -> 2 -> 3   [4] -> 6
                        \
                        [5]

            And 6 becomes unreachable!

         */
        return false;
    }

    @Override
    public boolean contains(int item) {
        return false;
    }
}
