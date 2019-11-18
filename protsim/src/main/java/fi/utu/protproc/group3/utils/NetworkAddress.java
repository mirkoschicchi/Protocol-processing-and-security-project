package fi.utu.protproc.group3.utils;

import java.util.BitSet;
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

    /**
     * Get the length in bits of an ip address inside a network address
     * @param networkAddress
     * @param ipAddress
     * @return The number of bits that match
     */
    public static int matchLength(NetworkAddress networkAddress, IPAddress ipAddress) {
        int ris = 0;

        byte[] addressArray = networkAddress.getAddress().toArray();

        String mask = IPAddress.createMask((networkAddress.getPrefixLength()));
        byte[] maskByte = new byte[16];
        for(int i = 0; i < 16; i++) {
            String portion = mask.substring(i*8, i*8 + 8);
            maskByte[i] = (byte)Integer.parseInt(portion, 2);
        }
        byte[] masked = new byte[16];
        byte[] maskedIpAddr = new byte[16];
        for(int i = 0; i < 16; i++) {
            masked[i] = (byte) ((ipAddress.toArray()[i] & 0xff) & (maskByte[i] & 0xff));
            maskedIpAddr[i] = (byte) ((addressArray[i] & 0xff) & (maskByte[i] & 0xff));
            String s1 = String.format("%8s", Integer.toBinaryString(masked[i] & 0xFF)).replace(' ', '0');
            String s2 = String.format("%8s", Integer.toBinaryString(maskedIpAddr[i] & 0xFF)).replace(' ', '0');
            // System.out.println("S1: " + i + " " + s1 + " : " + masked[i] + " : " + (ipAddress.toArray()[i]&0xff) + " : " + maskByte[i]);
            // System.out.println("S2: " + i + " " + s2 + " : " + maskedIpAddr[i]);
            for(int j = 0; j < 8; j++) {
                if(s1.charAt(j) == s2.charAt(j)) {
                    ris++;
                } else {
                    return ris;
                }
            }
        }
        if(ris >= networkAddress.getPrefixLength()) {
            ris = networkAddress.getPrefixLength();
        }
        return ris;
    }
}
