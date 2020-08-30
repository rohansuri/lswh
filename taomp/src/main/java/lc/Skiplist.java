package lc;

import java.util.concurrent.ThreadLocalRandom;

// 1206.
// Q: Why having duplicate nodes didn't work?
public class Skiplist {
    private static final int MAX_LEVEL = 7; // 0 to 7.

    private static final double P = 0.5;

    static class Node {
        int value;
        Node next[]; // size would indicate the max level of this node.
        int count; // For duplicates.

        Node(int maxLevel, int value) {
            this.value = value;
            next = new Node[maxLevel + 1];
            count = 1;
        }
    }

    // TAIL is greater than all other elements.
    // This is static since all of tail's next is always null.
    private static final Node TAIL = new Node(MAX_LEVEL, 0); // Value doesn't matter.

    // Head is just a sentinel.
    // Actual value starts from head.next.
    // Head is less than every other value.
    Node head;

    public Skiplist() {
        head = new Node(MAX_LEVEL, 0);
        for (int i = 0; i <= MAX_LEVEL; i++) {
            head.next[i] = TAIL;
        }
    }

    public boolean search(int target) {
        Node[] pred = new Node[MAX_LEVEL + 1];
        return find(target, pred) != -1;
    }

    // Returns the level at which num was found.
    // Returns -1 if num isn't found.
    // pred[] contains the predecessor node for each level from which downward
    // descent started.
    private int find(int num, Node preds[]) {
        Node pred = head;
        int found = -1;
        // Start from the highest level. Head and tail both are at the highest level.
        for (int i = MAX_LEVEL; i >= 0; i--) {
            Node curr = pred.next[i];
            // Keep traversing this level until num is greater.
            while (curr != TAIL && num > curr.value) {
                pred = curr;
                curr = curr.next[i];
            }
            // Either we reached tail or curr node is greater or equal to given item.
            // head -> 5 -> 10 -> tail
            // num = 6.
            // curr = 10, pred = 5.
            // we should take pred downstairs.
            //
            // In case curr == item.
            // head -> 5 -> 10 -> tail
            // num = 10.
            // curr = 10, pred = 5.
            // Even in this case we take pred downstairs.
            // Why not take curr?
            // Because then we won't find curr's pred in the next levels.
            if (curr != TAIL && num == curr.value && found == -1) {
                found = i;
            }
            preds[i] = pred;
        }
        return found;
    }

    private int randomLevel() {
        int level = 0;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (; level < MAX_LEVEL; ) {
            if (r.nextDouble() > P) {
                break;
            }
            level++;
        }
        return level;
    }

    public void add(int num) {
        Node preds[] = new Node[MAX_LEVEL + 1];
        int found = find(num, preds);
        if (found != -1) {
            Node n = preds[found].next[found];
            n.count++;
            return;
        }
        // Decide a level for this new node.
        int level = randomLevel();
        Node n = new Node(level, num);
        for (int i = 0; i <= level; i++) {
            // Pred for this level.
            Node pred = preds[i];
            n.next[i] = pred.next[i];
            pred.next[i] = n;
        }
    }

    public boolean erase(int num) {
        Node preds[] = new Node[MAX_LEVEL + 1];
        int found = find(num, preds);
        if (found == -1) {
            return false;
        }
        Node n = preds[found].next[found];
        if (n.count == 1) {
            // Last occurrence, unlink.
            for (int i = 0; i < n.next.length; i++) {
                preds[i].next[i] = n.next[i];
            }
        }
        n.count--;
        return true;
    }
}

/**
 * Your Skiplist object will be instantiated and called as such:
 * Skiplist obj = new Skiplist();
 * boolean param_1 = obj.search(target);
 * obj.add(num);
 * boolean param_3 = obj.erase(num);
 */