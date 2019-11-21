package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.NetworkConfiguration;
import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.*;

public class NetworkImpl implements Network {
    private FluxSink<byte[]> input;
    private final Flux<byte[]> output;
    private final List<EthernetInterface> interfaces = new ArrayList<>();
    private final Simulation simulation;
    private final NetworkAddress networkAddress;

    NetworkImpl(SimulationBuilderContext context, NetworkConfiguration configuration) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(configuration);

        this.simulation = context.simulation();
        this.networkAddress = context.generator().networkAddress(configuration.getAddress());

        var processor = DirectProcessor.<byte[]>create().serialize();
        input = processor.sink(FluxSink.OverflowStrategy.DROP);
        output = processor.publish(5)
                .autoConnect(0)
        //        .publishOn(Schedulers.elastic())
        //        .subscribeOn(Schedulers.elastic())
        ;
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
    public void removeDevice(EthernetInterface intf) {
        Objects.requireNonNull(intf);

        interfaces.remove(intf);
    }

    @Override
    public Collection<EthernetInterface> getDevices() {
        return Collections.unmodifiableCollection(interfaces);
    }

    @Override
    public Flux<byte[]> getFlux() {
        return output;
    }

    @Override
    public void transmit(byte[] pdu) {
        input.next(pdu);
    }
}
