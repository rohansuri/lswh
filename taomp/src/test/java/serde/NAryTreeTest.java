package serde;

import org.junit.Test;

public class NAryTreeTest {

    /*

                            1
               /            |           \           \
               2            2           3           -
             /\ \ \        / | |  \     / | | \     /| \ \
            4 - - -      -   - -  5    -  - 1 -    - - - -
     */
    @Test
    public void test() {
        int n = 4;
        NAryTree t = new NAryTree(n);
        t.root = new Node(n, 1);
        t.root.children[0] = new Node(n, 2);
        t.root.children[1] = new Node(n, 2);
        t.root.children[2] = new Node(n, 3);
        t.root.children[0].children[0] = new Node(n, 4);
        t.root.children[1].children[3] = new Node(n, 5);
        t.root.children[2].children[2] = new Node(n, 1);
        // System.out.println(t.levelOrder());
        System.out.println(t.serialize());
    }
}
