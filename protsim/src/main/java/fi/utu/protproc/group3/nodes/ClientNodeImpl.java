package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Simulation;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class ClientNodeImpl extends NetworkNodeImpl implements ClientNode {
    ClientNodeImpl(Simulation simulation, EthernetInterface intf) {
        super(simulation, new EthernetInterface[]{intf});
    }

    private Disposable messageFlux;

    @Override
    public void start() {
        super.start();

        if (messageFlux != null) {
            throw new UnsupportedOperationException("Client is already running.");
        }

        messageFlux = Flux.interval(Duration.ofSeconds(3))
                .subscribe(this::sendMessage);
    }

    @Override
    public void shutdown() {
        super.shutdown();

        if (messageFlux != null) {
            messageFlux.dispose();
            messageFlux = null;
        }
    }

    private void sendMessage(long messageId) {
        var dest = simulation.getRandomServer();

        // TODO: Remove code below and send correct TCP packet to the destination

        var srcIntf = getInterfaces().iterator().next();
        var frame = EthernetFrame.create(
                dest.getInterfaces().iterator().next().getAddress(),
                srcIntf.getAddress(),
                EthernetFrame.TYPE_IPV6,
                ("This is a test message to " + dest.getInterfaces().iterator().next().getInetAddresses()).getBytes()
        );

        srcIntf.getNetwork().transmit(frame.serialize());
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) {
        super.packetReceived(intf, pdu);

        // TODO: Handle reply
    }
}
