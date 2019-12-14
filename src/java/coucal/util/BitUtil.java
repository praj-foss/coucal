/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package coucal.util;

/**
 * Common bit manipulation functions.
 */
public final class BitUtil {
    public static int[] synchsafe(final int num) {
        return new int[] {
                (num & 0x0FE00000) >> 21,
                (num & 0x001FC000) >> 14,
                (num & 0x00003F80) >> 7,
                (num & 0x0000007F)
        };
    }

    public static int unsynchsafe(final int[] num) {
        return num[0] << 21 | num[1] << 14 | num[2] <<  7 | num[3];
    }
}
