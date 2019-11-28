package fi.utu.protproc.group3.utils;

import java.util.Objects;
import java.util.Random;

public class AddressGenerator {
    private final Random random;

    public AddressGenerator() {
        this(new Random());
    }

    public AddressGenerator(Random random) {
        this.random = random;
    }

    public byte[] ethernetAddress(String value) {
        if (value != null) return StringUtils.parseHexStream(value);

        var buf = new byte[6];
        random.nextBytes(buf);
        buf[0] = (byte) ((buf[0] & 0x0c) | 0x22);

        return buf;
    }

    public NetworkAddress networkAddress(String value) {
        if (value != null) return NetworkAddress.parse(value);

        // Generate random /56 network
        var netAddr = new byte[16];
        random.nextBytes(netAddr);

        for (var i = 6; i < 16; i++) {
            netAddr[i] = 0;
        }

        return new NetworkAddress(new IPAddress(netAddr), 6 * 8);
    }

    public IPAddress ipAddress(NetworkAddress network, String value) {
        Objects.requireNonNull(network);

        if (value != null) return IPAddress.parse(value);

        var buf = network.getAddress().toArray();
        for (var i = network.getPrefixLength() / 8; i < buf.length; i++) {
            buf[i] = (byte) random.nextInt();
        }

        return new IPAddress(buf);
    }

    public String hostName(String value) {
        if (value != null) return value;

        var id = random.nextInt();

        return "node-" + Integer.toHexString(id);
    }
}
