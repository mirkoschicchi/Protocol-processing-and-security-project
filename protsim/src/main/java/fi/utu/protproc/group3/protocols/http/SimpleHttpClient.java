package fi.utu.protproc.group3.protocols.http;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;

import java.util.logging.Logger;

public class SimpleHttpClient extends Connection {
    private static final Logger LOGGER = Logger.getLogger(SimpleHttpClient.class.getName());

    public SimpleHttpClient(NetworkNode node) {
        super(node);
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);

        send("GET / HTTP/1.0".getBytes());

        LOGGER.info("Sent GET request for " + connectionState.getDescriptor());
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        LOGGER.info("Received response for " + connectionState.getDescriptor());

        close();
    }
}
