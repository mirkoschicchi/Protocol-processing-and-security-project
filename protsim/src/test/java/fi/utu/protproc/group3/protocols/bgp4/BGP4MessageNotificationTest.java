package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.TCPDatagram;
import fi.utu.protproc.group3.utils.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BGP4MessageNotificationTest {
    @Test
    void createMessage() {
        var message = BGP4MessageNotification.create(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR,
                BGP4MessageNotification.ERR_SUBCODE_BAD_MESSAGE_LENGTH,
                new byte[] {1, 2, 3, 4});
        assertNotNull(message);

        var bytes = message.serialize();
        assertNotNull(bytes);
        assertEquals(25, bytes.length);
        assertEquals(2, bytes[22]);

        var parsedMsg = (BGP4MessageNotification) BGP4Message.parse(bytes);
        assertArrayEquals(message.getMarker(), parsedMsg.getMarker());
        assertSame(message.getType(), parsedMsg.getType());
        assertSame(message.getLength(), parsedMsg.getLength());
        assertSame(message.getErrorCode(), parsedMsg.getErrorCode());
        assertSame(message.getErrorSubCode(), parsedMsg.getErrorSubCode());
        assertArrayEquals(message.getData(), parsedMsg.getData());
    }

    @Test
    void parseMessage() {
        var pdu = StringUtils.parseHexStream("216ba8349d402f202a295c8686dd60000000002a0680200300040000000026ae89569c37dd812003000300000000e6c4a65b959c0758303900b30760cd4b0000000050000000a4dd0000ffffffffffffffffffffffffffffffff00160305026f");
        var frame = EthernetFrame.parse(pdu);
        var packet = IPv6Packet.parse(frame.getPayload());
        var datagram = TCPDatagram.parse(packet.getPayload());
        var message = BGP4Message.parse(datagram.getPayload());
        assertNotNull(message);
        assertEquals(22, message.getLength());
        assertTrue(message instanceof BGP4MessageNotification);
        var notification = (BGP4MessageNotification) message;
        assertEquals(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR, notification.getErrorCode());
        assertEquals(BGP4MessageNotification.ERR_SUBCODE_BAD_MESSAGE_LENGTH, notification.getErrorSubCode());
        assertArrayEquals(new byte[] {0x6f}, notification.getData());
    }
}
