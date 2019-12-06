package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class TrustAgentClient extends Connection {
    private static final Logger LOGGER = Logger.getLogger(TrustAgentClient.class.getName());
    private final IPAddress remoteIp;
    private final TrustAgentScoreListener listener;
    private Set<Integer> pendingRequest;

    public TrustAgentClient(NetworkNode node, IPAddress remoteIp, TrustAgentScoreListener listener) {
        super(node);
        this.remoteIp = remoteIp;

        this.listener = listener;
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);

        if (pendingRequest != null) {
            requestScores(pendingRequest);
            pendingRequest = null;
        }
    }

    public void requestScores(Set<Integer> peers) {
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
                pendingRequest = peers;
            } catch (UnsupportedOperationException e) {
                LOGGER.warning(node.getHostname() + " could not connect to router " + remoteIp);
            }
        }
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        ByteBuffer byteBuffer = ByteBuffer.wrap(message);
        var identifier = byteBuffer.getInt();
        byte mapSize = byteBuffer.get();

        Map<Integer, Double> results = new HashMap<>();
        for (var i = 0; i < mapSize; i++) {
            var peer = byteBuffer.getInt();
            double observedTrust = byteBuffer.getDouble();
            results.put(peer, observedTrust);
        }

        listener.scoreUpdated(identifier, results);

        LOGGER.fine("Vote RECEIVED: " + connectionState.getDescriptor() + "\nMessage length: " + message.length);
    }

    public interface TrustAgentScoreListener {
        void scoreUpdated(Integer bgpSource, Map<Integer, Double> votes);
    }
}

