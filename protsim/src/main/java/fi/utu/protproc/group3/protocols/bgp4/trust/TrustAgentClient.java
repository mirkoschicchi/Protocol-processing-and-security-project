package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.NetworkNode;
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
    private final IPAddress remoteIp;
    private final Set<Integer> peers;
    private final Consumer<Map<Integer, Double>> dataReceived;

    public TrustAgentClient(NetworkNode node, IPAddress remoteIp, Set<Integer> peers, Consumer<Map<Integer, Double>> dataReceived) {
        super(node);
        this.remoteIp = remoteIp;

        this.peers = peers;
        this.dataReceived = dataReceived;
    }

    @Override
    public void connected(EthernetInterface ethernetInterface, DatagramHandler.ConnectionState connectionState) {
        super.connected(ethernetInterface, connectionState);

        requestScores();
    }

    public void requestScores() {
        if (connectionState != null && connectionState.getStatus() == DatagramHandler.ConnectionStatus.Established) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1 + peers.size() * 4);
            byteBuffer.put((byte) peers.size());
            for (var peer : peers) {
                byteBuffer.putInt(peer);
            }

            send(byteBuffer.array());
        } else {
            try {
                connect(remoteIp, TrustAgentServer.PORT);
            } catch (UnsupportedOperationException e) {
                LOGGER.warning(ethernetInterface.getHost().getHostname() + " could not connect to router " + remoteIp);
            }
        }
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        ByteBuffer byteBuffer = ByteBuffer.wrap(message);
        byte mapSize = byteBuffer.get();

        Map<Integer, Double> results = new HashMap<>();
        for (var i = 0; i < mapSize; i++) {
            var peer = byteBuffer.getInt();
            double observedTrust = byteBuffer.getDouble();
            results.put(peer, observedTrust);
        }

        dataReceived.accept(results);

        LOGGER.fine("Vote RECEIVED: " + connectionState.getDescriptor() + "\nMessage length: " + message.length);
    }
}

