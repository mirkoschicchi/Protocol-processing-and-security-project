package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.nodes.NetworkNode;

public class ReflectiveServer implements Server {
    private final short port;
    private final Class<? extends Connection> connectionHandler;
    private final NetworkNode node;

    ReflectiveServer(NetworkNode node, short port, Class<? extends Connection> connectionHandler) {
        this.node = node;
        this.port = port;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void start() {
        node.getTcpHandler().listen(port, this);
    }

    @Override
    public Connection accept(DatagramHandler.ConnectionDescriptor descriptor) {
        try {
            return connectionHandler.getConstructor(NetworkNode.class).newInstance(node);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void shutdown() {
        node.getTcpHandler().close(this);
    }
}
