package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class IPv6PacketTest {
    private static final String host = "localhost";
    private final byte[] sourceIP = Inet6Address.getByName("fe80::1").getAddress();
    private final byte[] destinationIP = Inet6Address.getByName("fe80::2").getAddress();
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
        assertArrayEquals(original.getSourceIP(), reassembled.getSourceIP());
        assertArrayEquals(original.getDestinationIP(), reassembled.getDestinationIP());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
