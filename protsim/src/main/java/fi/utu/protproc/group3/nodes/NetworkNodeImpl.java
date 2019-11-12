package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Simulation;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public abstract class NetworkNodeImpl implements NetworkNode {
    protected final Simulation simulation;
    private final Collection<EthernetInterface> interfaces;

    protected NetworkNodeImpl(Simulation simulation, EthernetInterface[] interfaces) {
        Objects.requireNonNull(simulation);
        Objects.requireNonNull(interfaces);

        this.simulation = simulation;
        this.interfaces = Arrays.asList(interfaces);
    }

    @Override
    public Collection<EthernetInterface> getInterfaces() {
        return Collections.unmodifiableCollection(interfaces);
    }

    private Disposable messageListener;

    @Override
    public void start() {
        if (messageListener != null) {
            throw new UnsupportedOperationException("Node is already running.");
        }

        // Subscribe to all server interfaces
        messageListener = Flux
                .merge(
                        (Iterable<Flux<byte[]>>) interfaces.stream()
                                .map(i -> i.getFlux().doOnEach(pdu -> packetReceived(i, pdu.get())))::iterator
                )
                .subscribe();
    }

    @Override
    public void shutdown() {
        if (messageListener != null) {
            messageListener.dispose();
            messageListener = null;
        }
    }

    protected void packetReceived(EthernetInterface intf, byte[] pdu) {
        // NOP
    }
}
