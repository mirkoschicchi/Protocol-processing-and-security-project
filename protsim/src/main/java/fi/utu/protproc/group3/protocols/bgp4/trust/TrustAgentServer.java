package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.protocols.tcp.Server;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.util.Collection;
import java.util.Map;

public class TrustAgentServer implements Server {
    public static final short PORT = (short) 8080;
    private final Collection<EthernetInterface> interfaces;
    private final Map<IPAddress, BGPPeerContext> peerings;
    private final RouterNode router;

    public TrustAgentServer(RouterNode router, Map<IPAddress, BGPPeerContext> peerings) {
        this.router = router;
        this.interfaces = router.getInterfaces();
        this.peerings = peerings;
    }

    @Override
    public void start() {
        for (var intf : interfaces) {
            intf.getTCPHandler().listen(PORT, this);
        }
    }

    @Override
    public Connection accept(DatagramHandler.ConnectionDescriptor descriptor) {
        // get correct ethernet interface from the local ip of the descriptor
        // and then pass it in new TrustAgentServerConnection(ethernetinterface got now, peerings);
    }

    @Override
    public void shutdown() {
        for (var intf : interfaces) {
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
            // Parse the message and answer with the score of that IP addresses
            // I have a router with a list of peerings and I match the IPaddress and it gives me the context (it is a map)
            // Then send back the scores of every IPaddress with a number of entries + map (IPaddress, score)
            super.messageReceived(message);
        }
    }
}
