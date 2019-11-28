package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.lang.reflect.InvocationTargetException;

public class Server {
    private final EthernetInterface ethernetInterface;
    private final Class<? extends Connection> connectionHandler;

    public static Server listen(EthernetInterface ethernetInterface, short port, Class<? extends Connection> connectionHandler) {
        var result = new Server(ethernetInterface, connectionHandler);

        ethernetInterface.getTCPHandler().listen(port, result);

        return result;
    }

    private Server(EthernetInterface ethernetInterface, Class<? extends Connection> connectionHandler) {
        this.ethernetInterface = ethernetInterface;
        this.connectionHandler = connectionHandler;
    }

    public Connection accept(DatagramHandler.ConnectionDescriptor descriptor) {
        try {
            return connectionHandler.getConstructor(EthernetInterface.class).newInstance(ethernetInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void close() {
        ethernetInterface.getTCPHandler().close(this);
    }
}
