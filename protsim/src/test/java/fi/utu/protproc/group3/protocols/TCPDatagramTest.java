package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TCPDatagramTest {
    private static final int sourcePort = 4444;
    private static final int destPort = 8888;

    @Test
    public void createDatagram() {
        var datagram = TCPDatagram.create(destPort, sourcePort, false, false, true, false, new byte[] { 0x01 });

        assertNotNull(datagram);

        var bytes = datagram.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void reassembleDatagram() {
        var original = TCPDatagram.create(destPort, sourcePort, false, false, true, false, new byte[] { 0x01, 0x02 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = TCPDatagram.parse(bytes);

        assertNotNull(reassembled);

        assertEquals(original.getDestinationPort(), reassembled.getDestinationPort());
        assertEquals(original.getSourcePort(), reassembled.getSourcePort());
        assertEquals(original.getAckN(), reassembled.getAckN());
        assertEquals(original.getSeqN(), reassembled.getSeqN());
        assertEquals(original.getChecksum(), reassembled.getChecksum());
        assertEquals(original.getHeaderLength(), reassembled.getHeaderLength());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
