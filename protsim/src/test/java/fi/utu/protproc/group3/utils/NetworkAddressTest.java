package fi.utu.protproc.group3.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkAddressTest {
//    @Test
//    public void parseIPv4CidrNotation() throws UnknownHostException {
//        var netAddr = NetworkAddress.parse("127.0.0.0/8");
//
//        assertNotNull(netAddr);
//        assertEquals("127.0.0.0/8", netAddr.toString());
//    }

    @Test
    public void parseIPv6CidrNotation() {
        var netAddr = NetworkAddress.parse("fe80:2001::17:0:0/96");

        assertNotNull(netAddr);
        assertEquals("fe80:2001::17:0:0/96", netAddr.toString());
    }

    @Test
    public void isMatch() {
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/16"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/17"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/18"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/19"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/20"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/21"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/22"), IPAddress.parse("fe80:1::1")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/23"), IPAddress.parse("fe80:1::1")));
        assertFalse(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/24"), IPAddress.parse("fe80:1::1")));

        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/16"), IPAddress.parse("fe80:2::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/17"), IPAddress.parse("fe80:2::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/18"), IPAddress.parse("fe80:2::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/19"), IPAddress.parse("fe80:2::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/20"), IPAddress.parse("fe80:2::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/21"), IPAddress.parse("fe80:2::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/22"), IPAddress.parse("fe80:2::2")));
        assertFalse(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/23"), IPAddress.parse("fe80:2::2")));
        assertFalse(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::/24"), IPAddress.parse("fe80:2::2")));

        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::1/128"), IPAddress.parse("fe80:100::1")));
        assertFalse(NetworkAddress.isMatch(NetworkAddress.parse("fe80:100::1/128"), IPAddress.parse("fe80:100::2")));

        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("::/0"), IPAddress.parse("fe80:100::2")));
        assertTrue(NetworkAddress.isMatch(NetworkAddress.parse("::/0"), IPAddress.parse("::1")));
    }
}
