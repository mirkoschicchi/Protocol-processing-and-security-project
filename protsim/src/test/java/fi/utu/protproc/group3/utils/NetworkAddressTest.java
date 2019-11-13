package fi.utu.protproc.group3.utils;

import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

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
        assertEquals("fe80:2001:0:0:0:17:0:0/96", netAddr.toString());
    }
}
