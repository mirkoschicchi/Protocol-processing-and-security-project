package fi.utu.protproc.group3.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public byte[] ethernetAddress() {
        // See https://honeywellaidc.force.com/supportppr/s/article/Locally-Administered-MAC-addresses
        var buf = new byte[6];
        random.nextBytes(buf);
        buf[0] = (byte) (buf[0] & 0x0f | 0x20);

        return buf;
    }

    public NetworkAddress networkAddress() {
        // Generate random /56 network
        var netAddr = new byte[16];
        random.nextBytes(netAddr);

        for (var i = 7; i < 16; i++) {
            netAddr[i] = 0;
        }

        try {
            return new NetworkAddress(InetAddress.getByAddress(netAddr).getAddress(), 7);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InetAddress inetAddress(NetworkAddress network) {
        Objects.requireNonNull(network);

        var buf = network.getAddress();
        for (var i = network.getPrefixLength() / 8; i < buf.length; i++) {
            buf[i] = (byte) random.nextInt();
        }

        try {
            return InetAddress.getByAddress(buf);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
