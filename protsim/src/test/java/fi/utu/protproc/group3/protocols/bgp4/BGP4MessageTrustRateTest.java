package fi.utu.protproc.group3.protocols.bgp4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BGP4MessageTrustRateTest {

    @Test
    public void createMessage() {
        var message = BGP4MessageTrustRate.create((short) 1);
        assertNotNull(message);


        var bytes = message.serialize();
        assertNotNull(bytes);
        assertEquals(23, bytes.length);

        var parsedMsg = (BGP4MessageTrustRate) BGP4Message.parse(bytes);
        assertArrayEquals(message.getMarker(), parsedMsg.getMarker());
        assertSame(message.getType(), parsedMsg.getType());
        assertSame(message.getLength(), parsedMsg.getLength());
        assertSame(message.getInheritTrust(), parsedMsg.getInheritTrust());
    }
}
