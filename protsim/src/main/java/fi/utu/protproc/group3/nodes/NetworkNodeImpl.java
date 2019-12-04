package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.simulator.*;
import fi.utu.protproc.group3.utils.AddressGenerator;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;

import java.util.*;
import java.util.stream.Collectors;


public abstract class NetworkNodeImpl implements NetworkNode, SimpleNode {
    public final Simulation simulation;
    protected final String hostname;
    protected final List<EthernetInterface> interfaces = new ArrayList<>();

    protected NetworkNodeImpl(SimulationBuilderContext context, NodeConfiguration configuration, Network network) {
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

    protected NetworkNodeImpl(SimulationBuilderContext context, NodeConfiguration configuration) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(configuration);

        this.simulation = context.simulation();
        this.hostname = context.generator().hostName(configuration != null ? configuration.getName() : null);
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

        // Subscribe to all server interfaces
        messageListeners = interfaces.stream()
                .map(i -> i.getFlux().subscribe(pdu -> this.packetReceived(i, pdu)))
                .collect(Collectors.toUnmodifiableList());

        interfaces.forEach(i -> i.getTCPHandler().start());
    }

    @Override
    public void shutdown() {
        interfaces.forEach(i -> i.getTCPHandler().stop());

        if (messageListeners != null) {
            for (var listener : messageListeners) {
                listener.dispose();
            }
            messageListeners = null;
        }
    }

    @Override
    public boolean nodeIsRunning() {
        return messageListeners != null;
    }

    protected void packetReceived(EthernetInterface intf, byte[] pdu) {
        // NOP
    }

    @Override
    public String toString() {
        return hostname;
    }

    protected void send(IPv6Packet packet) {
        Objects.requireNonNull(packet);

        var ethernetInterface = getInterface();
        var destMac = ethernetInterface.resolveIpAddress(packet.getDestinationIP());
        if (destMac == null) {
            destMac = ethernetInterface.getDefaultRouter();
        }

        var frame = EthernetFrame.create(destMac, ethernetInterface.getAddress(), EthernetFrame.TYPE_IPV6,
                packet.serialize());

        ethernetInterface.transmit(frame.serialize());
    }
}
