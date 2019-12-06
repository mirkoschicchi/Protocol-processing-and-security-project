package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.scenarios.SimpleScenarioTest;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EthernetInterfaceTest extends SimpleScenarioTest {
    @Test
    void transmitAndReceiveFrame() {
        var frame = EthernetFrame.create(
                client.getInterface().getAddress(),
                client.getInterface().getAddress(),
                (short) 0x00, new byte[]{0x00});

        var buf = frame.serialize();
        var flux = client.getInterface().getFlux();

        StepVerifier.create(flux)
                .then(() -> clientNet.transmit(buf))
                .assertNext(pdu -> assertArrayEquals(buf, pdu))
                .thenCancel()
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void resolveAddress() {
        var mac = client.getInterface().resolveIpAddress(client.getIpAddress());

        assertArrayEquals(client.getInterface().getAddress(), mac);
    }
}
