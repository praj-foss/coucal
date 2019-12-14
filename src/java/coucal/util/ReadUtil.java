package coucal.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Common utilities for reading from {@link InputStream}.
 */
public final class ReadUtil {
    public static long unsignedInt(final InputStream stream) throws IOException {
        return    stream.read() << 24
                | stream.read() << 16
                | stream.read() << 8
                | stream.read();
    }

    public static long synchsafeInt(final InputStream stream) throws IOException {
        return    stream.read() << 21
                | stream.read() << 14
                | stream.read() << 7
                | stream.read();
    }
}
