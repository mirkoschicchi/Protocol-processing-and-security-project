package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TrustAgentClient extends Connection {
    private static final Logger LOGGER = Logger.getLogger(TrustAgentClient.class.getName());
    private final Set<IPAddress> peers;
    private final Consumer<Map<IPAddress, Double>> dataReceived;

    public TrustAgentClient(EthernetInterface ethernetInterface, Set<IPAddress> peers, Consumer<Map<IPAddress, Double>> dataReceived) {
        super(ethernetInterface);

        this.peers = peers;
        this.dataReceived = dataReceived;
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);
    }

    public void requestScores() {
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

        dataReceived.accept(mapPeerAndObservedTrust);

        LOGGER.info("Vote RECEIVED: " + connectionState.getDescriptor() + "\nMessage length: " + message.length);
    }
}

