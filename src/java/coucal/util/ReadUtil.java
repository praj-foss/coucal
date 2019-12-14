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

    public static String string(final InputStream stream, final int size) throws IOException {
        return new String(readBytes(stream, size), StandardCharsets.ISO_8859_1);
    }

    private static byte[] readBytes(final InputStream stream, final int size) throws IOException{
        final byte[] bytes = new byte[size];
        stream.read(bytes);
        return bytes;
    }
}
