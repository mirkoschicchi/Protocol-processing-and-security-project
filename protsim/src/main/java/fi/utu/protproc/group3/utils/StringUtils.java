package fi.utu.protproc.group3.utils;

import java.nio.ByteBuffer;
import java.util.Objects;

public class StringUtils {
    public static byte[] parseHexStream(String hexStream) {
        Objects.requireNonNull(hexStream);

        var len = hexStream.length() / 2;
        var buf = ByteBuffer.allocate(hexStream.length() / 2);
        for (var i = 0; i < len; i++) {
            buf.put((byte) Integer.parseInt(hexStream, i * 2, i * 2 + 2, 16));
        }

        return buf.array();
    }
}
