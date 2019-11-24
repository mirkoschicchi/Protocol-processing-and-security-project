package fi.utu.protproc.group3.utils;

import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class AutonomousSystemIterator<T extends Node> implements Iterator<T> {
    protected boolean directed;
    protected Graph graph;
    protected Node[] queue;
    protected int[] depth;
    protected int qHead;
    protected int qTail;
    protected int autonomousSystem;

    public AutonomousSystemIterator(Node startNode, boolean directed) {
        this.directed = directed;
        this.autonomousSystem = getAutonomousSystem(startNode);
        this.graph = startNode.getGraph();
        int n = this.graph.getNodeCount();
        this.queue = new Node[graph.getNodeCount()];
        this.depth = new int[n];
        int s = startNode.getIndex();

        for(int i = 0; i < n; ++i) {
            this.depth[i] = i == s ? 0 : -1;
        }

        this.queue[0] = startNode;
        this.qHead = 0;
        this.qTail = 1;
    }

    public boolean hasNext() {
        return this.qHead < this.qTail;
    }

    public T next() {
        if (this.qHead >= this.qTail) {
            throw new NoSuchElementException();
        } else {
            Node current = this.queue[this.qHead++];
            int level = this.depth[current.getIndex()] + 1;
            Iterable<Edge> edges = this.directed ? current.getEachLeavingEdge() : current.getEachEdge();
            Iterator i$ = edges.iterator();

            while(i$.hasNext()) {
                Edge e = (Edge)i$.next();
                Node node = e.getOpposite(current);

                if (getAutonomousSystem(node) != autonomousSystem) continue;

                int j = node.getIndex();
                if (this.depth[j] == -1) {
                    this.queue[this.qTail++] = node;
                    this.depth[j] = level;
                }
            }

            return (T) current;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("This iterator does not support remove");
    }

    public int getDepthOf(Node node) {
        return this.depth[node.getIndex()];
    }

    public int getDepthMax() {
        return this.depth[this.queue[this.qTail - 1].getIndex()];
    }

    public boolean isDirected() {
        return this.directed;
    }

    private int getAutonomousSystem(Node node) {
        if (!node.hasAttribute("as")) {
            return 0;
        }

        return node.getAttribute("as");
    }
}
