package fi.utu.protproc.group3.utils;

import java.util.Objects;

/**
 * Class to represent a CIDR network address in IPv4 or IPv6.
 */
public final class NetworkAddress {
    private final IPAddress address;
    private final int prefixLength;

    /**
     * Parses a network address in CIDR notation.
     */
    public static NetworkAddress parse(String cidr) {
        Objects.requireNonNull(cidr);

        var lengthPos = cidr.indexOf('/');
        if (lengthPos == -1) {
            throw new IllegalArgumentException("Error in CIDR notation: Prefix length missing.");
        }

        IPAddress addr = IPAddress.parse(cidr.substring(0, lengthPos));
        var prefixLength = Integer.parseInt(cidr.substring(lengthPos + 1));

        return new NetworkAddress(addr, prefixLength);
    }

    public NetworkAddress(IPAddress address, int prefixLength) {
        this.address = address;
        this.prefixLength = prefixLength;
    }

    public IPAddress getAddress() {
        return address;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    @Override
    public String toString() {
        return address.toString() + "/" + prefixLength;
    }
}
