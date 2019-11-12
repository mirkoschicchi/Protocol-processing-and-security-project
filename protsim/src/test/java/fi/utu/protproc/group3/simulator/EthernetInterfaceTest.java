package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EthernetInterfaceTest {
    @Test
    public void transmitAndReceiveFrame() {
        var sim = Simulation.create();
        var net = sim.createNetwork();
        var s1 = sim.createServer(net);
        var i1 = s1.getInterfaces().iterator().next();
        var s2 = sim.createServer(net);
        var i2 = s2.getInterfaces().iterator().next();

        sim.start(null);
        try {
            var frame = EthernetFrame.create(
                    i1.getAddress(),
                    i2.getAddress(),
                    EthernetFrame.TYPE_IPV6, new byte[]{0x00});

            var buf = frame.serialize();
            var flux = i1.getFlux();

            StepVerifier.create(flux)
                    .then(() -> net.transmit(buf))
                    .assertNext(pdu -> assertArrayEquals(buf, pdu))
                    .thenCancel()
                    .verify(Duration.ofSeconds(1));
        } finally {
            sim.stop();
        }
    }

    @Test
    public void resolveAddress() {
        var sim = Simulation.create();
        var net = sim.createNetwork();

        var s1 = sim.createServer(net);
        var i1 = s1.getInterfaces().iterator().next();

        var s2 = sim.createServer(net);
        var i2 = s2.getInterfaces().iterator().next();

        var addr = i1.getInetAddresses().iterator().next();
        var mac = i2.resolveInetAddress(addr);

        assertArrayEquals(i1.getAddress(), mac);
    }
}
