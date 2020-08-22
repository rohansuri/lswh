package serde;


import java.util.*;

class Node {
    Node[] children; // optimise later to not preallocate the whole array.
    int value;
    int size; // Number of children.

    Node(int n, int value) {
        children = new Node[n];
        this.value = value;
    }

    boolean isLeaf() {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                return false;
            }
        }
        return true;
    }
}

// This works and is optimised for saving space due to run length encoding.
// BUT isn't great because even for the null nodes we're going over the tree.
// So computationally is bad.
public class NAryTree {
    private final int n;
    Node root; // package private only for testing to create a tree by hand.
    private static final String NULL_MARKER = "null"; // in level order.
    private static final String LEVEL_MARKER = ";";
    private static final String NODE_MARKER = ",";

    NAryTree(int n) {
        this.n = n;
    }

    // TODO: remove n. Variable ary tree.
    static NAryTree deserialize(String s, int n) {
        NAryTree t = new NAryTree(n);
        if (s.startsWith(NULL_MARKER)) {
            return t;
        }

        int position = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            // Consume current node value.
            char c = s.charAt(i);
            while (c != NODE_MARKER.charAt(0)) {
                sb.append(c);
                c = s.charAt(i);
            }

            String value = sb.toString();
            if (value.equals(NULL_MARKER)) {
                // Skip over.
            } else {
                int val = Integer.parseInt(value);

            }
            sb.setLength(0);
            position++;
        }

        return t;
    }

    // Run length encodes nulls.
    String serialize() {
        String lo = levelOrder();
        System.out.println("level order = " + lo);
        StringBuilder sb = new StringBuilder();
        int nulls = 0;
        for (int i = 0; i < lo.length(); ) {
            char c = lo.charAt(i);
            if (c == NULL_MARKER.charAt(0)) {
                nulls++;
                i += NULL_MARKER.length();
                i += NODE_MARKER.length(); // skip ,
            } else {
                // The moment sequence of nulls breaks.
                if (nulls != 0) {
                    sb.append(NULL_MARKER);
                    sb.append(nulls); // run length encode.
                    sb.append(",");
                    nulls = 0;
                }
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }


    String levelOrder() {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Deque<Node> q = new LinkedList<>();
        q.add(root);
        int level = 0;
        System.out.println("\nlevel = 0");
        int added = 1;
        while (!q.isEmpty()) {
            Node n = q.pop();
            added--;
            if (n != null && !n.isLeaf()) {
                q.addAll(Arrays.asList(n.children));
            }
            if (n != null) {
                System.out.print(n.value + NODE_MARKER);
                sb.append(n.value);
            } else {
                System.out.print(NULL_MARKER + NODE_MARKER);
                sb.append(NULL_MARKER);

            }
            sb.append(NODE_MARKER);

            if (added == 0) {
                level++;
                System.out.println("\nlevel = " + level);
                // sb.deleteCharAt(sb.length() - 1);
                sb.append(LEVEL_MARKER);
                added = q.size();
            }

        }
        return sb.toString();
    }
}
