package fi.utu.protproc.group3.nodes;

public interface ServerNode extends NetworkNode {
    /**
     * Returns the server's background thread listening (and replying to) client requests.
     */
    Thread getServerThread();
}
