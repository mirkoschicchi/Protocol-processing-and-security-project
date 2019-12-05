package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

// TODO: connect to another server and ask for trust values for any number of routers
public class TrustAgentClient extends Connection {
    private static final Logger LOGGER = Logger.getLogger(TrustAgentClient.class.getName());
    private final List<IPAddress> peers;
    private final Consumer<Map<IPAddress, Double>> dataReceived;

    public TrustAgentClient(EthernetInterface ethernetInterface, List<IPAddress> peers, Consumer<Map<IPAddress, Double>> dataReceived) {
        super(ethernetInterface);

        this.peers = peers;
        this.dataReceived = dataReceived;
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);
    }

    public void requestScores() {
        // Send as first parameter the number of IPaddresses which I need to get evaluated
        // Here I have to ask for all peers their scores
        // send a message with router 1 IP address (I'm router S and communicating with router 4)
        // this.peers in this case has only one peer because we are not using another structure which supports multiple routers
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + peers.size() * 16);
        byteBuffer.put((byte) peers.size());
        for (IPAddress peer : peers) {
            byteBuffer.put(peer.toArray());
        }

        send(byteBuffer.array());
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        ByteBuffer byteBuffer = ByteBuffer.wrap(message);
        byte mapSize = byteBuffer.get();

        Map<IPAddress, Double> mapPeerAndObservedTrust = new HashMap<>();
        for (var i = 0; i < mapSize; i++) {
            byte[] bytes = new byte[16];
            byteBuffer.get(bytes);
            IPAddress ipAddress = new IPAddress(new byte [byteBuffer.get(16)]);
            double observedTrust = byteBuffer.getDouble();
            mapPeerAndObservedTrust.put(ipAddress, observedTrust);
        }

        // send the map once
        dataReceived.accept(mapPeerAndObservedTrust);

        LOGGER.info("Vote RECEIVED: " + connectionState.getDescriptor() + "\nMessage length: " + message.length);
    }
}

