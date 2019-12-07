package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.routing.RoutingTableImpl;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.*;
import fi.utu.protproc.group3.utils.AddressGenerator;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.Disposable;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public abstract class NetworkNodeImpl implements NetworkNode, SimpleNode {
    private static final Logger LOGGER = Logger.getLogger(NetworkNodeImpl.class.getName());
    final Simulation simulation;
    private final String hostname;
    final List<EthernetInterface> interfaces = new ArrayList<>();
    private final RoutingTable routingTable = new RoutingTableImpl();
    private final DatagramHandler tcpHandler = new DatagramHandler(this, this::sendPacket);

    NetworkNodeImpl(SimulationBuilderContext context, NodeConfiguration configuration, Network network) {
        this(context, configuration);

        Objects.requireNonNull(context);
        Objects.requireNonNull(configuration);
        Objects.requireNonNull(network);

        AddressGenerator generator = context.generator();
        interfaces.add(new EthernetInterfaceImpl(
                        this,
                        generator.ethernetAddress(null),
                        network,
                        generator.ipAddress(network.getNetworkAddress(), configuration.getAddress())
                )
        );
    }

    NetworkNodeImpl(SimulationBuilderContext context, NodeConfiguration configuration) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(configuration);

        this.simulation = context.simulation();
        this.hostname = context.generator().hostName(configuration.getName());
    }

    private List<Disposable> messageListeners;

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public Collection<EthernetInterface> getInterfaces() {
        return Collections.unmodifiableCollection(interfaces);
    }

    @Override
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    @Override
    public EthernetInterface getInterface() {
        var intfs = interfaces.iterator();
        var result = intfs.next();

        if (intfs.hasNext())
            throw new UnsupportedOperationException("Cannot call getInterface() on multi-interface node.");

        return result;
    }

    @Override
    public IPAddress getIpAddress() {
        return getInterface().getIpAddress();
    }

    @Override
    public void start() {
        if (messageListeners != null) {
            throw new UnsupportedOperationException("Node is already running.");
        }

        for (var intf : interfaces) {
            routingTable.insertRow(TableRow.create(intf.getNetwork().getNetworkAddress(), null, 0, intf));
        }

        if (interfaces.size() == 1) {
            var routers = getInterface().getNetwork().getDevices().stream()
                    .filter(i -> i.getHost() instanceof RouterNode)
                    .collect(Collectors.toList());

            if (routers.size() == 1) {
                routingTable.insertRow(TableRow.create(NetworkAddress.DEFAULT, routers.get(0).getIpAddress(), 0, interfaces.get(0)));
            }
        }

        // Subscribe to all server interfaces
        messageListeners = interfaces.stream()
                .map(i -> i.getFlux().subscribe(pdu -> this.packetReceived(i, pdu)))
                .collect(Collectors.toUnmodifiableList());

        getTcpHandler().start();
    }

    @Override
    public void shutdown() {
        getTcpHandler().stop();

        if (messageListeners != null) {
            for (var listener : messageListeners) {
                listener.dispose();
            }
            messageListeners = null;
        }
    }

    @Override
    public DatagramHandler getTcpHandler() {
        return tcpHandler;
    }

    @Override
    public boolean nodeIsRunning() {
        return messageListeners != null;
    }

    void packetReceived(EthernetInterface intf, byte[] pdu) {
        // NOP
    }

    @Override
    public String toString() {
        return hostname;
    }

    void sendPacket(IPv6Packet packet) {
        Objects.requireNonNull(packet);

        var route = getRoutingTable().getRowByDestinationAddress(packet.getDestinationIP());
        if (route == null) {
            LOGGER.warning("Cannot route packet from " + hostname + " for " + packet.getDestinationIP() + ". Dropping.");
            return;
        }

        EthernetInterface intf = route.getInterface();
        byte[] destMac;
        if (route.getNextHop() != null) {
            destMac = intf.resolveIpAddress(route.getNextHop());
        } else {
            destMac = intf.resolveIpAddress(packet.getDestinationIP());
        }

        var frame = EthernetFrame.create(destMac, intf.getAddress(), EthernetFrame.TYPE_IPV6, packet.serialize());

        intf.transmit(frame.serialize());
    }
}
