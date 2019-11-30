package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.simulator.EthernetInterface;

public class ReflectiveServer implements Server {
    protected final EthernetInterface ethernetInterface;
    private final short port;
    private final Class<? extends Connection> connectionHandler;

    protected ReflectiveServer(EthernetInterface ethernetInterface, short port, Class<? extends Connection> connectionHandler) {
        this.ethernetInterface = ethernetInterface;
        this.port = port;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void start() {
        ethernetInterface.getTCPHandler().listen(port, this);
    }

    @Override
    public Connection accept(DatagramHandler.ConnectionDescriptor descriptor) {
        try {
            return connectionHandler.getConstructor(EthernetInterface.class).newInstance(ethernetInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void shutdown() {
        ethernetInterface.getTCPHandler().close(this);
    }
}
