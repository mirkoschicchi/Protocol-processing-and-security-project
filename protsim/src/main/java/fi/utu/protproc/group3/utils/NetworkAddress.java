package fi.utu.protproc.group3.utils;

import java.net.InetAddress;

/**
 * Class to represent a CIDR network address in IPv4 or IPv6.
 */
public final class NetworkAddress {
    private final InetAddress address;
    private final int prefixLength;

    public NetworkAddress(InetAddress address, int prefixLength) {
        this.address = address;
        this.prefixLength = prefixLength;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPrefixLength() {
        return prefixLength;
    }
}
