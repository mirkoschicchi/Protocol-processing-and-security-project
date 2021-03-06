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

        var subnet = new byte[4];
        random.nextBytes(subnet);

        var result = new byte[] {
                (byte) 0xfe, (byte) 0x80, // local address
                (byte) 0xbe, (byte) 0xef, // our prefix
                subnet[0], subnet[1], subnet[2], subnet[3],
                0, 0, 0, 0, 0, 0, 0, 0
        };

        return new NetworkAddress(new IPAddress(result), 64);
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
