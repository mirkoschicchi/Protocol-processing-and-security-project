package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.TCPDatagram;
import fi.utu.protproc.group3.scenarios.SimpleScenarioTest;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class RoutingTests extends SimpleScenarioTest {
    @Test
    public void forwardIpPacket() {
        var data = "Hello World".getBytes();

        var pdu = EthernetFrame.create(
                clientRouter.getInterfaces().iterator().next().getAddress(),
                client.getInterface().getAddress(),
                EthernetFrame.TYPE_IPV6,
                IPv6Packet.create(
                        IPv6Packet.NEXT_HEADER_TCP,
                        client.getIpAddress(),
                        server.getIpAddress(),
                        TCPDatagram.create(
                                (short) 123, (short) 123, 0, 0, (short) 0,
                                (short) 0, (short) 0,
                                data
                        ).serialize(client.getIpAddress(), server.getIpAddress(), IPv6Packet.NEXT_HEADER_TCP, (short)(20 + data.length))
                ).serialize()
        ).serialize();

        StepVerifier.create(server.getInterface().getFlux())
                .then(() -> clientNet.transmit(pdu))
                .assertNext(p -> {
                    var frame = EthernetFrame.parse(p);
                    assertNotNull(frame);
                    assertEquals(EthernetFrame.TYPE_IPV6, frame.getType());

                    var packet = IPv6Packet.parse(frame.getPayload());
                    assertNotNull(packet);
                    assertNotEquals(IPv6Packet.HOP_LIMIT_DEFAULT, packet.getHopLimit());
                    assertEquals(IPv6Packet.NEXT_HEADER_TCP, packet.getNextHeader());

                    var datagram = TCPDatagram.parse(packet.getPayload());
                    assertNotNull(datagram);
                    assertArrayEquals(data, datagram.getPayload());
                })
                .thenCancel()
                .verify(Duration.ofSeconds(1));
    }
}
