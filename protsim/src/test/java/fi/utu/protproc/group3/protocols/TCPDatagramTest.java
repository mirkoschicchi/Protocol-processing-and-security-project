package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TCPDatagramTest {
    private static final byte[] sourceIPv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    private static final byte[] destIPv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2 };

    @Test
    public void createDatagram() {
        var datagram = TCPDatagram.create(destIPv6, sourceIPv6, new byte[] { 0x00 });

        assertNotNull(datagram);

        var bytes = datagram.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void reassembleDatagram() {
        var original = TCPDatagram.create(destIPv6, sourceIPv6, new byte[] { 0x00, 0x01 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = TCPDatagram.parse(bytes);

        assertNotNull(reassembled);

        assertArrayEquals(original.getDestination(), reassembled.getDestination());
        assertArrayEquals(original.getSource(), reassembled.getSource());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
