package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.UnknownHostException;

import static java.net.Inet6Address.getByAddress;
import static org.junit.jupiter.api.Assertions.*;

public class IPv6PacketTest {
    private static final String host = "localhost";
    private static final byte hopLimit = 8; // 8 should be the max value
    private static final byte addr1[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2 };
    private static final byte addr2[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3 };

    private static Inet6Address sourceIP;

    static {
        try {
            sourceIP = getByAddress(host, addr1, 5);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static Inet6Address destIP;

    static {
        try {
            destIP = getByAddress(null, addr2, 6);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createPacket() {
        var packet = IPv6Packet.create(destIP, sourceIP, hopLimit, new byte[] { 0x01 });

        assertNotNull(packet);

        var bytes = packet.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void reassemblePacket() {
        var original = IPv6Packet.create(destIP, sourceIP, hopLimit, new byte[] { 0x01, 0x02 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = IPv6Packet.parse(bytes);

        assertNotNull(reassembled);

        assertEquals(original.getDestinationIP(), reassembled.getDestinationIP());
        assertEquals(original.getSourceIP(), reassembled.getSourceIP());
        assertEquals(original.getHopLimit(), reassembled.getHopLimit());
        assertEquals(original.getPayloadLength(), reassembled.getPayloadLength());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
