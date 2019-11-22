package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.TCPDatagram;
import fi.utu.protproc.group3.utils.StringUtils;
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
        assertEquals(21, bytes.length);
    }

    @Test
    void reassembleDatagram() {
        var original = TCPDatagram.create(sourcePort, destPort, 5, 6, dataOffset, flags, (short)1, (short)2, (short)3, null, new byte[] { 0x01, 0x02 });

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

    @Test
    void parseTCPDatagram() {
        var pdu = StringUtils.parseHexStream("00000000000000000000000086dd6005796e019d0640fddf5af267a400000000000000000a4ffddf5af267a400000000000000000a4f85761f90292e663bfd11106a80180200972e00000101080a1e678b161e678b16474554202f20485454502f312e310d0a486f73743a205b666464663a356166323a363761343a3a6134665d3a383038300d0a557365722d4167656e743a204d6f7a696c6c612f352e3020285831313b204c696e7578207838365f36343b2072763a37312e3029204765636b6f2f32303130303130312046697265666f782f37312e300d0a4163636570743a20746578742f68746d6c2c6170706c69636174696f6e2f7868746d6c2b786d6c2c6170706c69636174696f6e2f786d6c3b713d302e392c2a2f2a3b713d302e380d0a4163636570742d4c616e67756167653a20656e2d55532c656e3b713d302e382c64652d43483b713d302e352c64653b713d302e330d0a4163636570742d456e636f64696e673a20677a69702c206465666c6174650d0a444e543a20310d0a436f6e6e656374696f6e3a206b6565702d616c6976650d0a557067726164652d496e7365637572652d52657175657374733a20310d0a43616368652d436f6e74726f6c3a206d61782d6167653d300d0a0d0a");

        var frame = EthernetFrame.parse(pdu);
        var packet = IPv6Packet.parse(frame.getPayload());
        var datagram = TCPDatagram.parse(packet.getPayload());

        assertEquals((short) 34166, datagram.getSourcePort());
        assertEquals((short) 8080, datagram.getDestinationPort());
        assertEquals(0x292e663b, datagram.getSeqN());
        assertEquals(0xfd11106a, datagram.getAckN());
        assertEquals(TCPDatagram.PSH | TCPDatagram.ACK, datagram.getFlags());
        assertEquals(512, datagram.getWindow());
        assertEquals((short) 0x972e, datagram.getChecksum());
        assertEquals(0, datagram.getUrgentPointer());
        assertArrayEquals(StringUtils.parseHexStream("0101080a1e678b161e678b16"), datagram.getOptionsAndPadding());
        assertNotNull(datagram.getPayload());
        assertEquals(381, datagram.getPayload().length);

        var rebuilt = EthernetFrame.create(
                frame.getDestination(), frame.getSource(), frame.getType(),
                IPv6Packet.create(packet.getVersion(), packet.getTrafficClass(), packet.getFlowLabel(),
                        packet.getNextHeader(), packet.getHopLimit(),
                        packet.getSourceIP(), packet.getDestinationIP(),
                        TCPDatagram.create(datagram.getSourcePort(), datagram.getDestinationPort(), datagram.getSeqN(),
                                datagram.getAckN(), datagram.getDataOffset(), datagram.getFlags(), datagram.getWindow(),
                                (short) datagram.getChecksum(), (short) datagram.getUrgentPointer(),
                                datagram.getOptionsAndPadding(), datagram.getPayload()
                        ).serialize()
                ).serialize()
        ).serialize();

        assertArrayEquals(pdu, rebuilt);
    }
}
