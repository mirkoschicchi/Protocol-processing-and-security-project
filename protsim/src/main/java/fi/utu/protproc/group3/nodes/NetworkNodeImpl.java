package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.simulator.*;
import fi.utu.protproc.group3.utils.AddressGenerator;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.net.UnknownHostException;
import java.util.*;


public abstract class NetworkNodeImpl implements NetworkNode, SimpleNode {
    protected final Simulation simulation;
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

    private Disposable messageListener;

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
        if (messageListener != null) {
            throw new UnsupportedOperationException("Node is already running.");
        }

        // Subscribe to all server interfaces
        messageListener = Flux
                .merge(
                        (Iterable<Flux<byte[]>>) interfaces.stream()
                                .map(i -> i.getFlux().doOnEach(pdu -> {
                                    try {
                                        packetReceived(i, pdu.get());
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }
                                }))::iterator
                )
                .subscribe();

        interfaces.forEach(i -> i.getTCPHandler().start());
    }

    @Override
    public void shutdown() {
        interfaces.forEach(i -> i.getTCPHandler().stop());

        if (messageListener != null) {
            messageListener.dispose();
            messageListener = null;
        }
    }

    @Override
    public boolean nodeIsRunning() {
        return messageListener != null;
    }

    protected void packetReceived(EthernetInterface intf, byte[] pdu) throws UnknownHostException {
        // NOP
    }

    @Override
    public String toString() {
        return hostname;
    }
}
