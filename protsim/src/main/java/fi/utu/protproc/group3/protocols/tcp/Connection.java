package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.util.Objects;

public abstract class Connection {
    protected final EthernetInterface ethernetInterface;
    public DatagramHandler.ConnectionState connectionState;

    public Connection(EthernetInterface ethernetInterface) {
        Objects.requireNonNull(ethernetInterface);

        this.ethernetInterface = ethernetInterface;
    }

    /**
     * Initiates the connection to the given target port.
     */
    public void connect(IPAddress ipAddress, short port) {
        ethernetInterface.getTCPHandler().connect(this, ipAddress, port);
    }

    /**
     * Callback after connection has been established.
     */
    public void connected(DatagramHandler.ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Callback upon arrival of a message.
     */
    public void messageReceived(byte[] message) {
        // NOP
    }

    /**
     * Sends a message to the peer.
     */
    public final void send(byte[] message) {
        if (connectionState != null && connectionState.getStatus() == DatagramHandler.ConnectionStatus.Established) {
            connectionState.send(message);
        }
    }

    /**
     * Closes the current TCP connection.
     */
    public final void close() {
        if (connectionState != null) {
            connectionState.close();
            connectionState = null;
        }
    }

    /**
     * Callback for closed TCP connection.
     */
    public void closed() {
        // NOP
    }
}
