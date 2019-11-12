package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TCPDatagramTest {
    private static final short sourcePort = 4444;
    private static final short destPort = 179;
    private static final byte dataOffset = (byte)5;
    private static final short flags = (short) (TCPDatagram.SYN | TCPDatagram.ACK);

    @Test
    void createDatagram() {
        var datagram = TCPDatagram.create(sourcePort, destPort, 0, 0, dataOffset, flags, (short)1, (short)2, (short)3, new byte[0], new byte[] { 0x01 });

        assertNotNull(datagram);

        var bytes = datagram.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void reassembleDatagram() {
        var original = TCPDatagram.create(sourcePort, destPort, 0, 0, dataOffset, flags, (short)1, (short)2, (short)3, new byte[0], new byte[] { 0x01, 0x02 });

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = TCPDatagram.parse(bytes);

        assertNotNull(reassembled);

        assertEquals(original.getSourcePort(), reassembled.getSourcePort());
        assertEquals(original.getDestinationPort(), reassembled.getDestinationPort());
        assertEquals(original.getSeqN(), reassembled.getSeqN());
        assertEquals(original.getAckN(), reassembled.getAckN());
        assertEquals(original.getDataOffset(), reassembled.getDataOffset());
        assertEquals(original.getFlags(), reassembled.getFlags());
        assertEquals(original.getWindow(), reassembled.getWindow());
        assertEquals(original.getChecksum(), reassembled.getChecksum());
        assertEquals(original.getUrgentPointer(), reassembled.getUrgentPointer());
        assertArrayEquals(original.getOptionsAndPadding(), reassembled.getOptionsAndPadding());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
