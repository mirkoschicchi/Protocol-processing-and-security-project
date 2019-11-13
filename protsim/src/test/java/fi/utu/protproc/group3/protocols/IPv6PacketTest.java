package fi.utu.protproc.group3.protocols;

import fi.utu.protproc.group3.TestUtils;
import fi.utu.protproc.group3.utils.IPAddress;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class IPv6PacketTest {
    private static final String host = "localhost";
    private final IPAddress sourceIP = IPAddress.parse("fe80::1");
    private final IPAddress destinationIP = IPAddress.parse("fe80::2");
    private static final byte hopLimit = 8;

    IPv6PacketTest() throws UnknownHostException {

    }

    @Test
    void createPacket() {
        var packet = IPv6Packet.create((byte) 6, (byte) 0, 0, (short) 1,
        (byte) 0, hopLimit, sourceIP, destinationIP, new byte[] { 0x01 });

        assertNotNull(packet);

        var bytes = packet.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void reassemblePacket() {
        var original = IPv6Packet.create((byte) 6, (byte) 0, 0, (short) 1,
                (byte) 0, hopLimit, sourceIP, destinationIP, new byte[] { 0x01, 0x02 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = IPv6Packet.parse(bytes);

        assertNotNull(reassembled);

        assertEquals(original.getVersion(), reassembled.getVersion());
        assertEquals(original.getTrafficClass(), reassembled.getTrafficClass());
        assertEquals(original.getFlowLabel(), reassembled.getFlowLabel());
        assertEquals(original.getPayloadLength(), reassembled.getPayloadLength());
        assertEquals(original.getNextHeader(), reassembled.getNextHeader());
        assertEquals(original.getHopLimit(), reassembled.getHopLimit());
        assertEquals(original.getSourceIP(), reassembled.getSourceIP());
        assertEquals(original.getDestinationIP(), reassembled.getDestinationIP());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }

    @Test
    void parseIcmpPacket() {
        var pdu = TestUtils.parseHexStream("3333ff000a4a6045cb9e8d8386dd6000000000203afffddf5af267a400000000000000000a4fff0200000000000000000001ff000a4a8700206700000000fddf5af267a400000000000000000a4a01016045cb9e8d83");

        var frame = EthernetFrame.parse(pdu);
        var packet = IPv6Packet.parse(frame.getPayload());

        assertEquals(6, packet.getVersion());
        assertEquals(0, packet.getTrafficClass());
        assertEquals(58, packet.getNextHeader());
        assertEquals(new IPAddress(TestUtils.parseHexStream("fddf5af267a400000000000000000a4f")), packet.getSourceIP());
        assertEquals(new IPAddress(TestUtils.parseHexStream("ff0200000000000000000001ff000a4a")), packet.getDestinationIP());
        assertEquals(32, packet.getPayloadLength());
        assertNotNull(packet.getPayload());
        assertEquals(32, packet.getPayload().length);
    }
}
