package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.protocols.tcp.Server;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TrustAgentServer implements Server {
    public static final short PORT = (short) 8080;
    private final Map<IPAddress, EthernetInterface> interfaces;
    private final Map<IPAddress, BGPPeerContext> peerings;
    private final RouterNode router;

    public TrustAgentServer(RouterNode router, Map<IPAddress, BGPPeerContext> peerings) {
        this.router = router;
        this.interfaces = router.getInterfaces().stream().collect(Collectors.toMap(EthernetInterface::getIpAddress, i -> i));
        this.peerings = peerings;
    }

    @Override
    public void start() {
        for (var intf : interfaces.values()) {
            intf.getTCPHandler().listen(PORT, this);
        }
    }

    @Override
    public Connection accept(DatagramHandler.ConnectionDescriptor descriptor) {
        EthernetInterface ethernetInterface = interfaces.get(descriptor.getLocalIp());

        if (ethernetInterface != null) {
            return new TrustAgentServerConnection(ethernetInterface, peerings);
        } else {
            return null;
        }
    }

    @Override
    public void shutdown() {
        for (var intf : interfaces.values()) {
            intf.getTCPHandler().close(this);
        }
    }

    static class TrustAgentServerConnection extends Connection {
        private final Map<IPAddress, BGPPeerContext> peerings;

        public TrustAgentServerConnection(EthernetInterface ethernetInterface, Map<IPAddress, BGPPeerContext> peerings) {
            super(ethernetInterface);

            this.peerings = peerings;
        }

        @Override
        public void messageReceived(byte[] message) {
            super.messageReceived(message);

            ByteBuffer byteBufferReceived = ByteBuffer.wrap(message);
            byte listLength = byteBufferReceived.get();

            Map<IPAddress, Double> mapPeerAndObservedTrust = new HashMap<>();

            for (var i = 0; i < listLength; i++) {
                byte[] bytes = new byte[16];
                byteBufferReceived.get(bytes);
                IPAddress ipAddress = new IPAddress(bytes);
                BGPPeerContext peering = peerings.get(ipAddress);
                if (peering != null) {
                    mapPeerAndObservedTrust.put(ipAddress, peering.getObservedTrust());
                }
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
