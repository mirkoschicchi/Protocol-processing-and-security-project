package fi.utu.protproc.group3.utils;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class IPAddressTest {
    @Test
    void ipv6Address() {
        var addr = new byte[]{(byte) 0xfe, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};

        var parsed = new IPAddress(addr);

        assertNotNull(parsed);
        assertArrayEquals(addr, parsed.toArray());
        assertEquals("fe80::1", parsed.toString());
    }

    @Test
    void parseIp() {
        Consumer<String> test = s -> assertEquals(s, IPAddress.parse(s).toString());

        test.accept("fe80::1");
    }
}
