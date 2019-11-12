package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;


public class EthernetFrameTest {
    private static final byte[] sourceMac = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    private static final byte[] destMac = {0x10, 0x11, 0x12, 0x13, 0x14, 0x15};

    @Test
    public void createFrame() {
        var frame = EthernetFrame.create(destMac, sourceMac, EthernetFrame.TYPE_IPV6, new byte[]{0x00});

        assertNotNull(frame);

        var bytes = frame.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void reassembleFrame() {
        var payload = new byte[50];
        for (int i = 0; i < payload.length; i++) payload[i] = (byte) (i + 10);
        var original = EthernetFrame.create(destMac, sourceMac, EthernetFrame.TYPE_IPV6, payload);

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = EthernetFrame.parse(bytes);

        assertNotNull(reassembled);

        assertArrayEquals(original.getDestination(), reassembled.getDestination());
        assertArrayEquals(original.getSource(), reassembled.getSource());
        assertEquals(original.getType(), reassembled.getType());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }

    @Test
    public void pcapFile() throws IOException {
        var original = EthernetFrame.create(destMac, sourceMac, EthernetFrame.TYPE_IPV6, new byte[]{0x00, 0x01});
        var pdu = original.serialize();

        var time = System.currentTimeMillis();
        var buf = ByteBuffer.allocate(1000);

        buf
                .putInt(0xa1b2c3d4)
                .putShort((short) 2).putShort((short) 4)
                .putInt(0)
                .putInt(0)
                .putInt(65535)
                .putInt(1)
                .putInt((int) time / 1000)
                .putInt((int) (time % 1000) * 1000)
                .putInt(pdu.length)
                .putInt(pdu.length)
                .put(pdu);

        var arr = buf.array();

        var fos = new FileOutputStream("/tmp/test.pcap");
        fos.write(arr, 0, buf.position());
        fos.close();
    }
}
