package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.RouterConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.protocols.bgp4.BGPServer;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.routing.RoutingTableImpl;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.EthernetInterfaceImpl;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;
import fi.utu.protproc.group3.utils.IPAddress;

import java.net.UnknownHostException;
import java.util.*;

public class RouterNodeImpl extends NetworkNodeImpl implements RouterNode {
    private final int autonomousSystem;
    private final int inheritTrust;
    private final Map<IPAddress, BGPPeerContext> peerings = new HashMap<>();
    private final BGPServer bgpServer = new BGPServer(this, Collections.unmodifiableMap(peerings));

    public RouterNodeImpl(SimulationBuilderContext context, RouterConfiguration configuration) {
        super(context, configuration);
        
        this.autonomousSystem = configuration.getAutonomousSystem();
        for (var intf : configuration.getInterfaces()) {
            var network = context.network(intf.getNetwork());
            interfaces.add(
                    new EthernetInterfaceImpl(
                            this,
                            context.generator().ethernetAddress(null),
                            network,
                            context.generator().ipAddress(network.getNetworkAddress(), intf.getAddress())
                    )
            );
        }
        this.inheritTrust = configuration.getInheritTrust();
    }

    private final RoutingTable routingTable = new RoutingTableImpl();

    @Override
    public Collection<EthernetInterface> getInterfaces() {
        return Collections.unmodifiableCollection(interfaces);
    }

    @Override
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    @Override
    public int getAutonomousSystem() {
        return autonomousSystem;
    }

    @Override
    public int getInheritTrust() {
        return inheritTrust;
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) throws UnknownHostException {
        super.packetReceived(intf, pdu);

        // Parse the bytes into an Ethernet frame object
        EthernetFrame frame = EthernetFrame.parse(pdu);
        if (frame.getType() == EthernetFrame.TYPE_IPV6) {
            IPv6Packet packet = IPv6Packet.parse(frame.getPayload());

            if (packet.getHopLimit() == 0) {
                // TODO : Error handling
                return;
            }

            // Get the MAC address of the next hop
            TableRow row = this.routingTable.getRowByDestinationAddress(packet.getDestinationIP());

            // If we found a valid routing entry
            if (row != null) {
                // Get the exit interface
                var exitIntf = row.getEInterface();

                // Get the MAC address of the interface to which to forward the packet
                IPAddress nextHop = row.getNextHop();
                if (nextHop == null) nextHop = packet.getDestinationIP();

                byte[] nextHopMac = exitIntf.resolveIpAddress(nextHop);

                if (nextHopMac == null) {
                    // TODO: Error handling
                    return;
                }

                // Reassemble the IPv6 packet
                IPv6Packet newPacket = IPv6Packet.create(packet.getVersion(), packet.getTrafficClass(), packet.getFlowLabel(),
                        packet.getNextHeader(), (byte) (packet.getHopLimit() - 1),
                        packet.getSourceIP(), packet.getDestinationIP(), packet.getPayload());

                EthernetFrame newFrame = EthernetFrame.create(nextHopMac, exitIntf.getAddress(), frame.getType(), newPacket.serialize());

                // Forward the frame
                exitIntf.transmit(newFrame.serialize());
            }
        }
    }

    @Override
    public void start() {
        super.start();

        bgpServer.start();

        if (peerings.size() == 0) {
            createPeerings();
        }

        for (var peering : peerings.values()) {
            peering.start();
        }
    }

    @Override
    public void shutdown() {
        for (var peering : peerings.values()) {
            peering.stop();
        }

        bgpServer.shutdown();

        super.shutdown();
    }

    private void createPeerings() {
        for (var intf : interfaces) {
            for (var peerDev : intf.getNetwork().getDevices()) {
                if (peerDev != intf && peerDev.getHost() instanceof RouterNode) {
                    var peer = (RouterNode) peerDev.getHost();
                    if (peer.getAutonomousSystem() != getAutonomousSystem()) {
                        var context = new BGPPeerContext(this, intf, peerDev.getIpAddress());
                        peerings.put(peerDev.getIpAddress(), context);
                    }
                }
            }
        }
    }
}
