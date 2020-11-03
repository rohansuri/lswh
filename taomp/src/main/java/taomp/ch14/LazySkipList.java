package taomp.ch14;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LazySkipList {
    private static final int MAX_LEVELS = 7;

    static class Node {
        final Node next[]; // size = topLevel+1.
        final int key;
        final int topLevel;
        // Volatile since this is our linearlization point. We need readers optimistically reading
        // our nodes to be safely checking this without taking locks.
        volatile boolean marked;
        private final Lock lock;
        volatile boolean fullyLinked;

        Node(int key, int topLevel) {
            this.key = key;
            this.topLevel = topLevel;
            next = new Node[topLevel + 1];
            lock = new ReentrantLock();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }

    // Head is sentinel. First node is head.next.
    final Node head;

    // Tail is a sentinel. Key doesn't matter.
    // Key property: All its next references are null. And it sorts after all keys.
    private static final Node tail = new Node(0, MAX_LEVELS);

    LazySkipList() {
        head = new Node(0, MAX_LEVELS);
        for (int i = 0; i <= MAX_LEVELS; i++) {
            head.next[i] = tail;
        }
    }

    // Takes no locks. Optimistic. Callers need to verify if the set of preds, succs captured
    // are still correct. This verification needs to be done while holding locks.
    int find(int key, Node[] preds, Node[] succs) {
        Node pred = head;
        int lFound = -1;
        for (int i = MAX_LEVELS; i >= 0; i--) {
            Node curr = pred.next[i];
            while (curr != tail && key > curr.key) {
                pred = curr;
                curr = curr.next[i];
            }
            // Why do we record the first level we found?
            // This is needed because it might happen that the node is in the process of being added
            // and it was actually added on some higher level, BUT due to some sequence of scheduling
            // when our find procedure started, we didn't see it on higher levels BUT now
            // we start seeing the node on one of the levels.
            // So later we need to verify if we're deleting this node, whether we found it on the right level.
            // ELSE we'll miss removing some of the higher links.
            if (curr != tail && lFound == -1 && key == curr.key) {
                lFound = i;
            }

            preds[i] = pred;
            // Note: this is called succs, but in case the element is found,
            // it actually stores the reference to the found node.
            // Q: but then while deletes how will we check that the real succs hasn't changed?
            // i.e. curr.next hasn't changed?
            succs[i] = curr;
        }
        return lFound;
    }

    // I think as an optimisation Pebble pre-calculates this?
    private int randomLevel() {
        int level = 0; // 0 is default.
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (; level < MAX_LEVELS; level++) {
            // Should we put it to next level?
            if (r.nextDouble() > 0.5) {
                break;
            }
        }
        return level;
    }

    boolean add(int key) {
        int topLevel = randomLevel();
        Node preds[] = new Node[MAX_LEVELS + 1];
        Node succs[] = new Node[MAX_LEVELS + 1];
        while (true) {
            int lFound = find(key, preds, succs);
            if (lFound != -1) {
                Node found = succs[lFound];

                // Q: why do we need this? other than this satisfying the logical property of skiplist
                // that a node should only be considered added only if it is fullyLinked?
                // Does it ensure any correctness properties from add's point of view?
                // No, but it does ensure logical view for the user.
                // If we return early then this concurrent add might return a "false" before the actual add
                // even finishes. i.e. we'll return a "false" meaning the element already exists
                // even before actually finishing the add call that will actually add it.
                // So fullyLinked is also the linearization point for adds.
                while (!found.fullyLinked) {
                }

                // Is it deleted?
                if (!found.marked) { // Happens before with a remover.
                    // This key already exists.
                    return false;
                }

                // So if we find the node to be marked, then we restart traversal.
                // Because we should've actually not seen this node at all.
                // Since if the node were totally unlinked then we'd not see this at all.
                // The list of preds, succs also are not correct, since they led us to this deleted node.
                // So we retry.
                continue;

                // Q: does it matter whether we check marked before or fullyLinked before?
                // I think first we should wait for it to be fullyLinked and then check if it is marked.
                // Since logically a node will first be fullyLinked and only then it will be marked.
            }
            // Not found.
            // But because we were optimistically gathering our preds, succs.
            // We need to lock and verify none of them changed.
            int highestLocked = -1;
            try {
                boolean valid = true;
                for (int i = 0; valid && i <= topLevel; i++) {
                    preds[i].lock();
                    highestLocked = i;
                    // Note: we don't need lock on succs. Since we're not modifying it.
                    // And also if someone else is adding a node before this succ, it'll overlap
                    // with our lock for pred. Similarly if someone is removing succ it'll overlap
                    // with our lock for pred.
                    //
                    // Q: Why don't we verify that have preds, succs nodes been fullyLinked?
                    valid = !preds[i].marked && !succs[i].marked && preds[i].next[i] == succs[i];
                }
                if (!valid) continue;
                Node newNode = new Node(key, topLevel);
                // Safe to insert.
                for (int i = 0; i <= topLevel; i++) {
                    preds[i].next[i] = newNode;
                    newNode.next[i] = succs[i];
                }
                // Linearisation point. Also establishes happens before edge with a remover.
                newNode.fullyLinked = true;
                return true;
            } finally {
                for (int i = 0; i <= highestLocked; i++) {
                    preds[i].unlock();
                }
            }
        }
    }

    boolean remove(int key) {
        Node preds[] = new Node[MAX_LEVELS + 1];
        Node succs[] = new Node[MAX_LEVELS + 1];
        while (true) {
            int lFound = find(key, preds, succs);
            if (lFound == -1) {
                // Q: is it fair to read optimistically and return?
                // There is no happens-before here?
                return false;
            }
            // If found, it might be already deleted i.e. marked, in which case we should let the concurrent remover
            // completely unlink it (the one who marked this as deleted).
            // Q: How can we simply return false here? Since we know a delete is in progress?
            // Shouldn't we wait for a fullyDeleted flag?
            // Because it'd be weird to first see a "false" i.e. remove unsuccessful since
            // there is nothing to remove and then see a "true" by the concurrent remover,
            // without having any adds in the middle.
            //
            //
            // The found node might in the process of being inserted i.e. might not be fully linked.
            // If we've correctly got all its links set, then we're good to remove.
            // Else we're not and since the removal is in progress, we return false i.e. nothing to remove.
            //
            // But how do we know if the add is in progress i.e. how do we know if all links are set at all levels?
            // Since we set the links bottom to top in ADD, if we find the node at the top then can we say we've found it?
            // NO.
            // Why? Because this doesn't guarantee that node's successor is correctly set.
            // It might be null. There is no happens-before, we just "luckily" seem to have found the newly
            // inserted node at the top. So that means newNode's pred is set correctly, but its successor
            // may not have been. But then can we not check for null? Or how about we first set newNode's succs?
            // And then its preds?
            // Who knows, maybe without a happens-before it might not be guaranteed that
            // the order of writes in bottom up fashion are exactly seen in the same order?
            // There might be reorderings, loop fusion, fission, etc.
            // This is the whole reason why we have happens-before and the JMM.
            Node found = succs[lFound];
            if (found.marked || lFound != found.topLevel || !found.fullyLinked) {
                return false;
            }

            // Verify captured nodes are not deleted.
            boolean valid = true;
            int highestLocked = -1;
            try {
                for (int i = 0; valid && i <= found.topLevel; i++) {
                    preds[i].lock();
                    highestLocked = i;
                    valid = !preds[i].marked && preds[i].next[i] == found;
                }
                if (!valid) continue;

                // It is important to mark before unlinking, since otherwise another actor
                // may not know that a node is to be deleted and still traverse it and "use it".
                // For example an add might incorrectly add a deleted node as the successor
                // of the new node.
                // But this can still happen right? Since add doesn't take locks on succ.
                found.marked = true;

                for (int i = found.topLevel; i >= 0; i--) {
                    preds[i].next[i] = found.next[i];
                }
            } finally {
                for (int i = 0; i <= highestLocked; i++) {
                    preds[i].unlock();
                }
            }
        }
    }
}
