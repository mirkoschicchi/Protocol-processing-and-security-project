package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.simulator.EthernetInterface;

public interface Server {
    static ReflectiveServer listen(EthernetInterface ethernetInterface, short port, Class<? extends Connection> connectionHandler) {
        var result = new ReflectiveServer(ethernetInterface, port, connectionHandler);
        result.start();

        return result;
    }

    void start();

    Connection accept(DatagramHandler.ConnectionDescriptor descriptor);

    void shutdown();
}
