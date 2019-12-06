package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.TCPDatagram;
import fi.utu.protproc.group3.utils.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BGP4MessageKeepaliveTest {
    @Test
    void createMessage() {
        var message = BGP4MessageKeepalive.create();

        assertNotNull(message);

        var bytes = message.serialize();

        assertNotNull(bytes);
        assertEquals(19, bytes.length);

        var parsedMsg = BGP4Message.parse(bytes);
        assertArrayEquals(message.getMarker(), parsedMsg.getMarker());
        assertSame(message.getType(), parsedMsg.getType());
        assertSame(message.getLength(), parsedMsg.getLength());
    }

    @Test
    void parseMessage() {
        var pdu = StringUtils.parseHexStream("216ba8349d402f202a295c8686dd6000000000270680200300040000000026ae89569c37dd812003000300000000e6c4a65b959c0758303900b30760cd4b0000000050000000a6540000ffffffffffffffffffffffffffffffff001304");
        var frame = EthernetFrame.parse(pdu);
        var packet = IPv6Packet.parse(frame.getPayload());
        var datagram = TCPDatagram.parse(packet.getPayload());
        var message = BGP4Message.parse(datagram.getPayload());
        assertNotNull(message);
        assertEquals(19, message.getLength());
        assertTrue(message instanceof BGP4MessageKeepalive);
    }
}
