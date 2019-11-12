package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class IPv6PacketTest {
    private static final String host = "localhost";
    private final byte[] addr1 = Inet6Address.getByName("fe80::1").getAddress();
    private final byte[] addr2 = Inet6Address.getByName("fe80::2").getAddress();

    private final Inet6Address destIP = Inet6Address.getByAddress(host, addr1, 5);
    private final Inet6Address sourceIP = Inet6Address.getByAddress(null, addr2, 6);

    private static final byte hopLimit = 8;
    IPv6PacketTest() throws UnknownHostException {

    }

    @Test
    void createPacket() {
        var packet = IPv6Packet.create(destIP, sourceIP, hopLimit, new byte[] { 0x01 });

        assertNotNull(packet);

        var bytes = packet.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void reassemblePacket() throws UnknownHostException {
        var original = IPv6Packet.create(destIP, sourceIP, hopLimit, new byte[] { 0x01, 0x02 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = original.parse(bytes);

        assertNotNull(reassembled);

        assertArrayEquals(original.getDestinationIP().getAddress(), reassembled.getDestinationIP().getAddress());
        assertArrayEquals(original.getSourceIP().getAddress(), reassembled.getSourceIP().getAddress());
        assertEquals(original.getHopLimit(), reassembled.getHopLimit());
        assertEquals(original.getPayloadLength(), reassembled.getPayloadLength());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
