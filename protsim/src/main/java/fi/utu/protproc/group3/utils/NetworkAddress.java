package fi.utu.protproc.group3.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Class to represent a CIDR network address in IPv4 or IPv6.
 */
public final class NetworkAddress {
    private final byte[] address;
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

        byte[] addr = InetAddress.getByName(cidr.substring(0, lengthPos)).getAddress();
        var prefixLength = Integer.parseInt(cidr.substring(lengthPos + 1));

        return new NetworkAddress(addr, prefixLength);
    }

    public NetworkAddress(byte[] address, int prefixLength) {
        this.address = address;
        this.prefixLength = prefixLength;
    }

    public byte[] getAddress() {
        return address;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    @Override
    public String toString() {
        StringBuilder addressString = new StringBuilder();
        for (byte b : address) {
            addressString.append(String.format("%02x", b));
        }
        addressString.append("/" + prefixLength);

        return addressString.toString();
    }
}
