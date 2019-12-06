package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.protocols.tcp.Server;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TrustAgentServer implements Server {
    public static final short PORT = (short) 8080;
    private final RouterNode router;
    private final Collection<BGPPeerContext> peerings;

    public TrustAgentServer(RouterNode router, Collection<BGPPeerContext> peerings) {
        this.router = router;
        this.peerings = peerings;
    }

    @Override
    public void start() {
        router.getTcpHandler().listen(PORT, this);
    }

    @Override
    public Connection accept(DatagramHandler.ConnectionDescriptor descriptor) {
        return new TrustAgentServerConnection(router, peerings);
    }

    @Override
    public void shutdown() {
        router.getTcpHandler().close(this);
    }

    static class TrustAgentServerConnection extends Connection {
        private final Collection<BGPPeerContext> peerings;

        TrustAgentServerConnection(NetworkNode node, Collection<BGPPeerContext> peerings) {
            super(node);

            this.peerings = peerings;
        }

        @Override
        public void messageReceived(byte[] message) {
            super.messageReceived(message);

            ByteBuffer byteBufferReceived = ByteBuffer.wrap(message);
            byte listLength = byteBufferReceived.get();

            Map<Integer, Double> mapPeerAndObservedTrust = new HashMap<>();

            for (var i = 0; i < listLength; i++) {
                var peer = byteBufferReceived.getInt();
                var peering = peerings.stream().filter(p -> p.getBgpIdentifier() == peer).findAny();
                peering.ifPresent(bgpPeerContext -> mapPeerAndObservedTrust.put(peer, bgpPeerContext.getObservedTrust()));
            }

            ByteBuffer byteBufferToSend = ByteBuffer.allocate(5 + mapPeerAndObservedTrust.size() * (4 + 8));
            byteBufferToSend.putInt(((RouterNode) node).getBGPIdentifier());
            byteBufferToSend.put((byte) mapPeerAndObservedTrust.size());
            for (var entry : mapPeerAndObservedTrust.entrySet()) {
                byteBufferToSend.putInt(entry.getKey());
                byteBufferToSend.putDouble(entry.getValue());
            }

            send(byteBufferToSend.array());
        }
    }
}
