package btree;

import java.util.Comparator;

public class BTree<K, V> {

    static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    static class Node<K, V> {
        // Stored in sorted order.
        Entry<K, V>[] entries;
        // Size of children = size of entries + 1;
        Node<K, V>[] children;
        // Number of children this node has.
        int n;
        boolean leaf;

        Node(int order) {
            this.children = (Node<K, V>[]) new Node[order];
            this.entries = (Entry<K, V>[]) new Entry[order - 1];
        }
    }

    Node<K, V> root;
    // Every node except root has max these number of children.
    // Every non-leaf node has minimum ceil(order/2) children.
    private final int order;
    private final Comparator<? super K> comparator;

    // With ? super K we're saying we're fine accepting a comparator
    // for K or any of its superclasses. Which sounds right since when calling
    // compare we'd be sure K or any of its subclasses can be upcasted to K or its super type.
    BTree(int order, Comparator<? super K> comparator) {
        this.order = order;
        this.comparator = comparator;
        root = new Node<>(order);
        root.leaf = true;
    }

    void insert(K key, V value) {
    }

    // Btrees are one pass, you never need to unwind the stack.
    // Hence iterative solution is easy to write.
    // We should provide a contains method to differentiate between the case when value is null
    // or key doesn't exist.
    V get(K key) {
        Node<K, V> curr = root;
        outer:
        while (true) {
            for (int i = 0; i < curr.n - 1; i++) {
                int c = comparator.compare(key, curr.entries[i].key);
                if (c == 0) {
                    return curr.entries[i].value;
                }
                if (c < 0) {
                    if (curr.leaf) {
                        return null;
                    }
                    //  1 -> 3 -> 5
                    // /   /     /  \
                    curr = curr.children[i];
                    // Q: can the child pointer be null?
                    continue outer;
                }
            }
            if (curr.leaf) {
                return null;
            }
            // Key is greater than all elements in this node.
            curr = curr.children[curr.n - 1];
        }
    }
}
