package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.NetworkConfiguration;
import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.*;

public class NetworkImpl implements Network {
    private final int autonomousSystem;
    private final FluxSink<byte[]> input;
    private final Flux<byte[]> output;
    private final List<EthernetInterface> interfaces = new ArrayList<>();
    private final NetworkAddress networkAddress;
    private final String networkName;
    private volatile boolean isOnline;

    NetworkImpl(SimulationBuilderContext context, NetworkConfiguration configuration) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(configuration);

        this.networkAddress = context.generator().networkAddress(configuration.getAddress());
        this.networkName = context.generator().hostName(configuration.getName());
        this.autonomousSystem = configuration.getAutonomousSystem();

        var processor = DirectProcessor.<byte[]>create().serialize();
        input = processor.sink(FluxSink.OverflowStrategy.DROP);
        output = processor
                .filter(p -> isOnline)
                .publish()
                .autoConnect(0)
                .publishOn(Schedulers.elastic())
        ;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void start() {
        isOnline = true;
    }

    @Override
    public void shutdown() {
        isOnline = false;
    }

    @Override
    public NetworkAddress getNetworkAddress() {
        return networkAddress;
    }

    @Override
    public void addDevice(EthernetInterface intf) {
        Objects.requireNonNull(intf);

        interfaces.add(intf);
    }

    @Override
    public Collection<EthernetInterface> getDevices() {
        return Collections.unmodifiableCollection(interfaces);
    }

    public String getNetworkName() {
        return networkName;
    }

    @Override
    public Flux<byte[]> getFlux() {
        return output;
    }

    @Override
    public void transmit(byte[] pdu) {
        input.next(pdu);
    }

    @Override
    public int getAutonomousSystem() {
        return autonomousSystem;
    }

    @Override
    public String toString() {
        return networkName + " (" + networkAddress + ')';
    }
}
