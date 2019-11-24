package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.scenarios.SimpleScenarioTest;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EthernetInterfaceTest extends SimpleScenarioTest {
    @Test
    public void transmitAndReceiveFrame() throws IOException {
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
    public void resolveAddress() {
        var mac = client.getInterface().resolveIpAddress(client.getIpAddress());

        assertArrayEquals(client.getInterface().getAddress(), mac);
    }
}
