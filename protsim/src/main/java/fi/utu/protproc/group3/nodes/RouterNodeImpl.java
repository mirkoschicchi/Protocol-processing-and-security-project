package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.RouterConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.protocols.bgp4.BGPServer;
import fi.utu.protproc.group3.protocols.bgp4.trust.TrustAgentServer;
import fi.utu.protproc.group3.protocols.bgp4.trust.TrustManager;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.EthernetInterfaceImpl;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RouterNodeImpl extends NetworkNodeImpl implements RouterNode, RouterNode.Configurator {
    private final int autonomousSystem;
    private static int nextBgpIdentifier = 1;
    private final int bgpIdentifier;
    private final Map<IPAddress, BGPPeerContext> peerings = new HashMap<>();
    private final BGPServer bgpServer = new BGPServer(this, Collections.unmodifiableMap(peerings));
    private TrustAgentServer trustAgentServer;
    private boolean configurationFinalized;
    private TrustManager trustManager;

    public RouterNodeImpl(SimulationBuilderContext context, RouterConfiguration configuration) {
        super(context, configuration);
        
        this.autonomousSystem = configuration.getAutonomousSystem();
        bgpIdentifier = nextBgpIdentifier++;

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
    }


    @Override
    public Collection<EthernetInterface> getInterfaces() {
        return Collections.unmodifiableCollection(interfaces);
    }

    @Override
    public Configurator getConfigurator() {
        if (configurationFinalized) {
            throw new UnsupportedOperationException("Configuration has been finalized.");
        }

        return this;
    }

    @Override
    public int getAutonomousSystem() {
        return autonomousSystem;
    }

    protected void packetReceived(EthernetInterface intf, byte[] pdu) {
        super.packetReceived(intf, pdu);

        // Parse the bytes into an Ethernet frame object
        EthernetFrame frame = EthernetFrame.parse(pdu);
        if (frame.getType() == EthernetFrame.TYPE_IPV6) {
            IPv6Packet packet = IPv6Packet.parse(frame.getPayload());
            if (packet.getDestinationIP().equals(intf.getIpAddress())) {
                // Packet is for local consumption, no forwarding
                return;
            }

            if (packet.getHopLimit() == 0) {
                // TODO : Error handling
                return;
            }

            // Get the MAC address of the next hop
            TableRow row = getRoutingTable().getRowByDestinationAddress(packet.getDestinationIP());

            // If we found a valid routing entry
            if (row != null) {
                // Get the exit interface
                var exitIntf = row.getInterface();

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
        if (!configurationFinalized) {
            throw new UnsupportedOperationException("Configuration has not been finalized.");
        }

        super.start();

        bgpServer.start();
        trustAgentServer.start();
        trustManager.start();

        // Delay starting the peerings by 1s to ensure all the BGP servers have been started. Otherwise we'll have to
        // wait for the first timeout, which takes an additional 30s of startup time.
        var ref = new Object() {
            Disposable startPeerings = null;
        };
        ref.startPeerings = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(5))
                .subscribe(i -> {
                    for (var peering : peerings.values()) {
                        peering.start();
                    }
                    if (ref.startPeerings != null) {
                        ref.startPeerings.dispose();
                        ref.startPeerings = null;
                    }
                });
    }

    @Override
    public void shutdown() {
        for (var peering : peerings.values()) {
            peering.stop();
        }

        trustManager.shutdown();
        trustAgentServer.shutdown();
        bgpServer.shutdown();

        super.shutdown();
    }

    @Override
    public int getBGPIdentifier() {
        return bgpIdentifier;
    }

    @Override
    public void createPeering(EthernetInterface ethernetInterface, IPAddress neighbor, Collection<IPAddress> secondDegreeNeighbors) {
        if (configurationFinalized) {
            throw new UnsupportedOperationException("Configuration has been finalized.");
        }

        peerings.put(neighbor, new BGPPeerContext(this, ethernetInterface, neighbor, secondDegreeNeighbors));
    }

    @Override
    public void createStaticRoute(NetworkAddress networkAddress, EthernetInterface intf, IPAddress nextHop, int metric) {
        if (configurationFinalized) {
            throw new UnsupportedOperationException("Configuration has been finalized.");
        }

        getRoutingTable().insertRow(TableRow.create(networkAddress, nextHop, metric, intf));
    }

    @Override
    public void finalizeConfiguration() {
        if (configurationFinalized) {
            throw new UnsupportedOperationException("Configuration has been finalized.");
        }

        for (var peer : peerings.keySet()) {
            var peering = peerings.get(peer);
            for (var neighbor : peerings.values()) {
                if (!neighbor.getPeer().equals(peer)) {
                    peering.getDistributionList().add(neighbor);
                }
            }
        }

        trustAgentServer = new TrustAgentServer(this, peerings.values());
        trustManager = new TrustManager(this, Collections.unmodifiableMap(peerings));

        configurationFinalized = true;
    }
}
