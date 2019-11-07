package fi.utu.protproc.group3.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Class to represent a CIDR network address in IPv4 or IPv6.
 */
public final class NetworkAddress {
    private final InetAddress address;
    private final int prefixLength;

    /**
     * Parses a network address in CIDR notation.
     */
    public static NetworkAddress parse(String cidr) throws UnknownHostException {
        Objects.requireNonNull(cidr);

        var lengthPos = cidr.indexOf('/');
        if (lengthPos == -1) {
            throw new IllegalArgumentException("Error in CIDR notation: Prefix length missing.");
        }

        var inetAddr = InetAddress.getByName(cidr.substring(0, lengthPos));
        var prefixLength = Integer.parseInt(cidr.substring(lengthPos + 1));

        return new NetworkAddress(inetAddr, prefixLength);
    }

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

    @Override
    public String toString() {
        return address.toString().substring(1) + "/" + prefixLength;
    }
}
