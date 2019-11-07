package fi.utu.protproc.group3.nodes;

public interface RouterNode extends NetworkNode {
    /**
     * Gets the router's background thread, routing IP packets among its interfaces.
     */
    Thread getRouterThread();
}
