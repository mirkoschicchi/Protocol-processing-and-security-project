package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

// TODO: connect to another server and ask for trust values for any number of routers
public class TrustAgentClient extends Connection {
    private static final Logger LOGGER = Logger.getLogger(TrustAgentClient.class.getName());
    private final List<IPAddress> peers;

    public TrustAgentClient(EthernetInterface ethernetInterface, List<IPAddress> peers) {
        super(ethernetInterface);

        this.peers = peers;

        LOGGER.info("CHIAMATO CLIENT");
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);

        LOGGER.info("Vote SENT: " + connectionState.getDescriptor());
    }

    public void requestScores() {
        // Send as first parameter the number of IPaddresses which I need to get evaluated
        // Here I have to ask for all peers their scores
        // send a message with router 1 IP address (I'm router S and communicating with router 4)
        // this.peers in this case has only one peer because we are not using another structure which supports multiple routers
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);

        byteBuffer.putDouble(context.getObservedTrust());

        send(byteBuffer.array());
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        LOGGER.info("Vote RECEIVED: " + connectionState.getDescriptor() + "\nMessage length: " + message.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(message);

        context.addToPeersVote(byteBuffer.getDouble());
    }
}

