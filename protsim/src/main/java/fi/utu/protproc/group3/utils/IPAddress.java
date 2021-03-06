package fi.utu.protproc.group3.utils;


import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class IPAddress {
    private final byte[] buf;

    public static IPAddress parse(String ip) {
        Objects.requireNonNull(ip);

        if (ip.startsWith("::")) {
            ip = "0" + ip;
        }

        if (ip.contains(":")) {
            var result = new byte[16];
            var elements = ip.split(":");
            var skipped = 8 - elements.length;
            var pos = 0;
            for (String element : elements) {
                if (element.length() == 0) {
                    pos += 2 * (skipped + 1);
                } else {
                    int val = Integer.parseInt(element, 16);
                    result[pos] = (byte) ((val & 0xff00) >> 8);
                    result[pos + 1] = (byte) (val & 0xff);
                    pos += 2;
                }
            }

            return new IPAddress(result);
        } else {
            throw new UnsupportedOperationException("IPv4 not supported yet.");
        }
    }

    public IPAddress(byte[] buf) {
        Objects.requireNonNull(buf);

        if (buf.length != 4 && buf.length != 16) {
            throw new UnsupportedOperationException("Invalid IP address");
        }

        this.buf = buf;
    }

    public byte[] toArray() {
        return Arrays.copyOf(buf, 16);
    }

    @Override
    public String toString() {
        if (buf.length == 4) {
            return String.format("%d.%d.%d.%d", buf[0], buf[1], buf[2], buf[3]);
        } else {
            var result = new StringBuilder();
            var buf = ByteBuffer.wrap(this.buf);
            for (var i = 0; i < 8; i++) {
                result.append(Integer.toHexString(buf.getShort() & 0xffff));
                if (i < 7) {
                    result.append(':');
                }
            }
            return result.toString().replaceFirst(":(0:)+", "::").replaceFirst("::0$", "::");
        }
    }

    @Override
    public int hashCode() {
        var result = buf.length;
        for (var i : buf) {
            result = result * 31 + i;
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IPAddress) {
            return Arrays.equals(buf, ((IPAddress) obj).buf);
        }

        return super.equals(obj);
    }
}
