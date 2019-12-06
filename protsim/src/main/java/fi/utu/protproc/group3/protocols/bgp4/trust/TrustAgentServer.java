package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.protocols.tcp.Server;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

        public TrustAgentServerConnection(NetworkNode node, Collection<BGPPeerContext> peerings) {
            super(node);

            this.peerings = peerings;
        }

        @Override
        public void messageReceived(byte[] message) {
            super.messageReceived(message);

            ByteBuffer byteBufferReceived = ByteBuffer.wrap(message);
            byte listLength = byteBufferReceived.get();

            Map<IPAddress, Double> mapPeerAndObservedTrust = new HashMap<>();

            for (var i = 0; i < listLength; i++) {
                var peer = byteBufferReceived.getInt();
                var peering = peerings.stream().filter(p -> p.getBgpIdentifier() == peer).findAny();
                peering.ifPresent(bgpPeerContext -> mapPeerAndObservedTrust.put(bgpPeerContext.getPeer(), bgpPeerContext.getObservedTrust()));
            }

            ByteBuffer byteBufferToSend = ByteBuffer.allocate(1 + mapPeerAndObservedTrust.size() * (16 + 8));
            byteBufferToSend.put((byte) mapPeerAndObservedTrust.size());
            for (Map.Entry<IPAddress, Double> entry : mapPeerAndObservedTrust.entrySet()) {
                byteBufferToSend.put(entry.getKey().toArray());
                byteBufferToSend.putDouble(entry.getValue());
            }

            send(byteBufferToSend.array());
        }
    }
}
