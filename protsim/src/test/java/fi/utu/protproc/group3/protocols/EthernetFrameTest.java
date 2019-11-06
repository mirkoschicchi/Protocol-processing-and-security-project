package fi.utu.protproc.group3.protocols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class EthernetFrameTest {
    private static final byte[] sourceMac = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    private static final byte[] destMac = {0x10, 0x11, 0x12, 0x13, 0x14, 0x15};

    @Test
    public void createFrame() {
        var frame = EthernetFrame.create(destMac, sourceMac, EthernetFrame.TYPE_IPV4, new byte[]{0x00});

        assertNotNull(frame);

        var bytes = frame.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void reassembleFrame() {
        var original = EthernetFrame.create(destMac, sourceMac, EthernetFrame.TYPE_IPV4, new byte[]{0x00, 0x01});

        assertNotNull(original);

        var bytes = original.serialize();
        var reassembled = EthernetFrame.parse(bytes);

        assertNotNull(reassembled);

        assertArrayEquals(original.getDestination(), reassembled.getDestination());
        assertArrayEquals(original.getSource(), reassembled.getSource());
        assertEquals(original.getType(), reassembled.getType());
        assertArrayEquals(original.getPayload(), reassembled.getPayload());
    }
}
