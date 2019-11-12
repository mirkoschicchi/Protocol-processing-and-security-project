package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TCPDatagramTest {
    private static final int sourcePort = 4444;
    private static final int destPort = 179;

    @Test
    void createDatagram() {
        var datagram = TCPDatagram.create(destPort, sourcePort, TCPDatagram.SYN, 0, 0, new byte[] { 0x01 });

        assertNotNull(datagram);

        var bytes = datagram.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void reassembleDatagram() {
        var original = TCPDatagram.create(destPort, sourcePort, TCPDatagram.SYN, 0, 0, new byte[] { 0x01, 0x02 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = original.parse(bytes);

        assertNotNull(reassembled);

        assertEquals(original.getDestinationPort(), reassembled.getDestinationPort());
        assertEquals(original.getSourcePort(), reassembled.getSourcePort());
        assertEquals(original.getAckN(), reassembled.getAckN());
        assertEquals(original.getSeqN(), reassembled.getSeqN());
        assertEquals(original.getFlags(), reassembled.getFlags());
        assertEquals(original.getChecksum(), reassembled.getChecksum());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
