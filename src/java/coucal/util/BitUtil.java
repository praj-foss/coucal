/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package coucal.util;

/**
 * Common bit manipulation functions.
 */
public final class BitUtil {
    public static int synchsafe(int num) {
        return    (num & 0x0000007F)
                | (num & 0x00003F80) << 1
                | (num & 0x001FC000) << 2
                | (num & 0x0FE00000) << 3;
    }

    public static int unsynchsafe(int[] num) {
        return num[0] << 21 | num[1] << 14 | num[2] <<  7 | num[3];
    }
}
