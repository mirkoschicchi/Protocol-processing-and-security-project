package fi.utu.protproc.group3.utils;

import java.util.Objects;

/**
 * Class to represent a CIDR network address in IPv4 or IPv6.
 */
public final class NetworkAddress {
    private final IPAddress address;
    private final int prefixLength;

    public final static NetworkAddress DEFAULT = parse("::/0");

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

    @Override
    public int hashCode() {
        return Objects.hash(address, prefixLength);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkAddress) {
            var other = (NetworkAddress) obj;
            return isMatch(this, other.address)
                    && isMatch(other, address);
        }

        return super.equals(obj);
    }

    private static final byte[] MASK = new byte[]
            {
                    (byte) 0x00, (byte) 0x80, (byte) 0xc0, (byte) 0xe0,
                    (byte) 0xf0, (byte) 0xf8, (byte) 0xfc, (byte) 0xfe
            };

    public static boolean isMatch(NetworkAddress networkAddress, IPAddress ipAddress) {
        var net = networkAddress.address.toArray();
        var ip = ipAddress.toArray();
        int prefixLen = networkAddress.getPrefixLength() / 8;
        for (var i = 0; i < prefixLen; i++) {
            if (net[i] != ip[i]) return false;
        }

        if (prefixLen == ip.length) return true;

        var mask = MASK[networkAddress.getPrefixLength() % 8];
        return (net[prefixLen + 1] & mask) == (ip[prefixLen + 1] & mask);
    }

    public short getRequiredBytesForPrefix() {
        short len = (short) (getPrefixLength() / 8);
        if (getPrefixLength() % 8 > 0)
            len += 1;
        return len;
    }

}
