package fi.utu.protproc.group3.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void longestPrefixMatch() {
        var netAddr = NetworkAddress.parse("fe80:2001::17:0:0/9");
        var ipAddr = IPAddress.parse("fe80:2001::17:1:1");
        int ris = NetworkAddress.matchLength(netAddr, ipAddr);
        assertEquals(ris, 9);
    }
}
