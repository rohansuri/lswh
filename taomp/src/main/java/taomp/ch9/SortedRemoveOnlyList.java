package taomp.ch9;

import java.util.Set;

/*
    Helps to think if a CAS construct is sufficient for only one kind of actor.
    Remove involves finding a node and setting its pred.next to curr.next.

    What if while we're removing curr someone removes curr.next?
    1 -> 2 -> 3 -> 4 -> 5.
    Concurrent remove for 3, 4.
    3 has pred=2, curr=3, next=4.
    4 has pred=3, curr=4, next=5.
    4 proceeds first:
                   _________
                  |         |
        1 -> 2 -> 3  [4] -> 5

        then 3 proceeds.
                 ________
                 |       |
        1 -> 2  [3] [4]-> 5
             |_______|

        This is wrong. We still have a removed node 4 linked from 2.
    3 proceeds first:
             __________
             |        |
        1 -> 2  [3]-> 4 -> 5

        then 4 proceeds.

              _________
             |        |
        1 -> 2  [3]  [4] -> 5
                 |__________|

        Again 4 a removed node still stays reachable.

    Why did this happen?
    Because we let 2.next to set to a removed node.
    So as part of 3's removal, we didn't check if its next node itself is removed or not.
    So thinking of delete just being protected CAS on changing pred.next isn't enough.
    What the curr.next is, should also not get deleted.
    This is why FineList takes lock on both pred, curr.
    But we want to stay lock-free i.e. ensure at least one of the threads completes in a finite number of steps.
    For which we don't want to use locks but rather employ CAS everywhere.

    Since CAS can only be done on a single variable, we need to do just enough to let anyone else know
    that a node is removed so that they don't work on a incorrect node. And later take help from all
    actors to notice this incomplete step and help each other to complete the left over actions.

    The list we finally get is broken for only one reason: it has a removed node and we don't know about it.
    And so we might think it is still there.
    How about if before removing a node we mark it as being removed.
    Later whoever sees a marked node can simply do the left over task of changing pred.next to curr.next.

    What about the letting the concurrent unlinking to continue to happen?
    So there'll always be a first unlinked node and last unlinked node (sentinels or some nodes which are not being deleted).
    As long as we can reach from the first to next non deleted node, we're good.
    Does the above scheme make sure we can continue to do this?
    Having deleted nodes in the path is ok. But not being able to reach to non deleted nodes, is a problem.

    first = first non deleted node.
    last = next non deleted node.
    ..... = concurrent deletes happening in between.
    all concurrent deletes can be modelled like this.
    this represents "one section" where the delete is happening.
    there maybe many such sections separated by a nondeleted node.

    first -> ...... -> last.
    so you sure would take first.next and reach some node.
    maybe directly last which means all intermediate nodes have already been deleted.
    or maybe some marked nodes in the middle.
    but can those intermediate marked nodes always take you to last?

 */
public class SortedRemoveOnlyList {
    static class Node {
        Node next;
        int value;

        Node(Node next, int value) {
            this.next = next;
            this.value = value;
        }
    }

    Node head;

    SortedRemoveOnlyList(Set<Integer> s) {
        Node last = new Node(null, Integer.MAX_VALUE);
        head = new Node(last, Integer.MIN_VALUE);

        // Set is already sorted in order so keep adding to tail.
        Node pred = head;
        final Node curr = head.next; // points to last node.
        for (int x : s) {
            // Keep inserting at tail.
            Node newNode = new Node(curr, x);
            pred.next = newNode;
            pred = newNode;
        }
    }

    boolean remove(int item) {
        return false;
    }

    void printAll() {
        // First node is sentinel, hence we start from next.
        Node curr = head.next;
        while(curr != null) {
            System.out.println(curr.value);
            curr = curr.next;
        }
    }
}
