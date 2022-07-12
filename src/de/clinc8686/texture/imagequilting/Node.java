package de.clinc8686.texture.imagequilting;

import java.util.ArrayList;

public class Node {
    public Coords node;
    public int costs;
    public Node parent;
    public boolean visited = false;

    Node(Coords node, int costs, Node parent) {
        this.node = node;
        this.costs = costs;
        this.parent = parent;
    }
}
