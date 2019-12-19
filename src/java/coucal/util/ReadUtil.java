package coucal.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static java.lang.Byte.toUnsignedLong;

/**
 * Common utilities for reading from {@link InputStream}.
 */
public final class ReadUtil {
    public static long unsignedInt(final InputStream stream) throws IOException {
        final byte[] bytes = readBytes(stream, 4);
        return    toUnsignedLong(bytes[0]) << 24
                | toUnsignedLong(bytes[1]) << 16
                | toUnsignedLong(bytes[2]) << 8
                | toUnsignedLong(bytes[3]);
    }

    public static long synchsafeInt(final InputStream stream) throws IOException {
        final byte[] bytes = readBytes(stream, 4);
        return    toUnsignedLong(bytes[0]) << 21
                | toUnsignedLong(bytes[1]) << 14
                | toUnsignedLong(bytes[2]) << 7
                | toUnsignedLong(bytes[3]);
    }

    public static String text(final InputStream stream, final int size) throws IOException {
        final byte[] bytes = new byte[size];
        if (stream.read(bytes) == -1) {
            return null;
        }
        final int len = bytes.length;
        switch (bytes[0]) {
            case 0x00: return new String(
                    bytes, 1,
                    len - ((len > 1 && bytes[len-1] == 0) ? 2 : 1),
                    StandardCharsets.ISO_8859_1);
            case 0x01: return new String(
                    bytes, 1,
                    len - ((bytes[len-1] == 0 && bytes[len-2] == 0) ? 3 : 1),
                    StandardCharsets.UTF_16);
            default: return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }

    public static String frameId(final InputStream stream) throws IOException {
        final byte[] bytes = new byte[4];
        return (stream.read(bytes) != -1 && bytes[0] != 0)
                ? new String(bytes, StandardCharsets.ISO_8859_1)
                : null;
    }

    public static void skip(final InputStream stream, final long bytes) throws IOException {
        long left = bytes;
        while (left > 0)
            left -= stream.skip(left);
    }

    private static byte[] readBytes(final InputStream stream, final int size) throws IOException {
        final byte[] bytes = new byte[size];
        stream.read(bytes);
        return bytes;
    }
}
