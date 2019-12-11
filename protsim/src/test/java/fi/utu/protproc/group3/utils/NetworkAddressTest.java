package fi.utu.protproc.group3.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkAddressTest {
    @Test
    void parseIPv6CidrNotation() {
        var netAddr = NetworkAddress.parse("fe80:2001::17:0:0/96");

        assertNotNull(netAddr);
        assertEquals("fe80:2001::17:0:0/96", netAddr.toString());
    }

    @Test
    void isMatch() {
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

    @Test
    void truncateAddress() {
        assertNetworkEqual(NetworkAddress.parse("fe80:2001::/32"), NetworkAddress.parse("fe80:2001:1234::1/32"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001::/33"), NetworkAddress.parse("fe80:2001:1234::1/33"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001::/34"), NetworkAddress.parse("fe80:2001:1234::1/34"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001::/35"), NetworkAddress.parse("fe80:2001:1234::1/35"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001:1000:/36"), NetworkAddress.parse("fe80:2001:1234::1/36"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001:1000:/37"), NetworkAddress.parse("fe80:2001:1234::1/37"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001:1200:/38"), NetworkAddress.parse("fe80:2001:1234::1/38"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2001:1200:/39"), NetworkAddress.parse("fe80:2001:1234::1/39"));

        assertNetworkEqual(NetworkAddress.parse("fe80:2000::/24"), NetworkAddress.parse("fe80:20ff:1234::1/24"));
        assertNetworkEqual(NetworkAddress.parse("fe80:2080::/25"), NetworkAddress.parse("fe80:20ff:1234::1/25"));
        assertNetworkEqual(NetworkAddress.parse("fe80:20c0::/26"), NetworkAddress.parse("fe80:20ff:1234::1/26"));
        assertNetworkEqual(NetworkAddress.parse("fe80:20e0::/27"), NetworkAddress.parse("fe80:20ff:1234::1/27"));
        assertNetworkEqual(NetworkAddress.parse("fe80:20f0::/28"), NetworkAddress.parse("fe80:20ff:1234::1/28"));
        assertNetworkEqual(NetworkAddress.parse("fe80:20f8::/29"), NetworkAddress.parse("fe80:20ff:1234::1/29"));
        assertNetworkEqual(NetworkAddress.parse("fe80:20fc::/30"), NetworkAddress.parse("fe80:20ff:1234::1/30"));
        assertNetworkEqual(NetworkAddress.parse("fe80:20fe::/31"), NetworkAddress.parse("fe80:20ff:1234::1/31"));
    }

    private void assertNetworkEqual(NetworkAddress a, NetworkAddress b) {
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
