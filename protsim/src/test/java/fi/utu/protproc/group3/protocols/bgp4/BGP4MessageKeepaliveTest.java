package fi.utu.protproc.group3.protocols.bgp4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BGP4MessageKeepaliveTest {
    @Test
    public void createMessage() {
        var message = BGP4MessageKeepalive.create((short) 19, BGP4Message.TYPE_KEEPALIVE);

        assertNotNull(message);

        var bytes = message.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length == 19);
    }
}
