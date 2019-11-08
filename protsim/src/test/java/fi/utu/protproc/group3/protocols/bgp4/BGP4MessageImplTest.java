package fi.utu.protproc.group3.protocols.bgp4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BGP4MessageImplTest {

    @Test
    public void serialize() {
        byte[] marker = new byte[16];
        int length = 19;
        short type = 1;
        byte[] body = null;

        var bgp4Message = BGP4MessageKeepaliveImpl.create(marker, length, type);
        assertNotNull(bgp4Message);

        var bytes = bgp4Message.serialize();
        assertNotNull(bytes);

        assertTrue(bytes.length > 0);
    }
}
