package fi.utu.protproc.group3.protocols.bgp4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BGP4MessageNotificationTest {
    @Test
    public void createMessage() {
        var message = BGP4MessageNotification.create(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR,
                BGP4MessageNotification.ERR_SUBCODE_BAD_MESSAGE_LENGTH,
                new byte[] {1, 2, 3, 4});
        assertNotNull(message);

        var bytes = message.serialize();
        assertNotNull(bytes);
        assertTrue(bytes.length == 25);
        assertTrue(bytes[22] == 2);
    }
}
