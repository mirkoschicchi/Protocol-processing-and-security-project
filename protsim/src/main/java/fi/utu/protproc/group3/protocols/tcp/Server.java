package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.nodes.NetworkNode;

public interface Server {
    static ReflectiveServer listen(NetworkNode node, short port, Class<? extends Connection> connectionHandler) {
        var result = new ReflectiveServer(node, port, connectionHandler);
        result.start();

        return result;
    }

    void start();

    Connection accept(DatagramHandler.ConnectionDescriptor descriptor);

    void shutdown();
}
