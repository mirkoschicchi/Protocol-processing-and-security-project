package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.protocols.tcp.Server;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

import java.util.Collection;
import java.util.Map;

public class BGPServer implements Server {
    public static final short PORT = (short) 179;
    private final Collection<EthernetInterface> interfaces;
    private final Map<IPAddress, BGPPeerContext> peerings;
    private final RouterNode router;

    public BGPServer(RouterNode router, Map<IPAddress, BGPPeerContext> peerings) {
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
        var peering = peerings.get(descriptor.getRemoteIp());
        if (peering != null) {
            // I'm glad we have our own TCP/IP stack which supports some really ugly stuff...
            // Like (re-)using a previously created TCP connection to match with an incoming packet... 😂
            return peering.getConnection();
        }

        return null;
    }

    @Override
    public void shutdown() {
        for (var intf : interfaces) {
            intf.getTCPHandler().close(this);
        }
    }
}
