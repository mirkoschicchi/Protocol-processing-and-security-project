package fi.utu.protproc.group3.protocols.bgp4;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BGP4MessageKeepaliveTest {
    @Test
    public void createMessage() {
        var message = BGP4MessageKeepalive.create();

        assertNotNull(message);

        var bytes = message.serialize();

        assertNotNull(bytes);
        assertTrue(bytes.length == 19);

        var parsedMsg = BGP4Message.parse(bytes);
        assertTrue(Arrays.equals(message.getMarker(), parsedMsg.getMarker()));
        assertSame(message.getType(), parsedMsg.getType());
        assertSame(message.getLength(), parsedMsg.getLength());
    }
}
