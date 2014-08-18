package com.github.hf.leveldb.util;

/*
 * Stojan Dimitrovski
 *
 * Copyright (c) 2014, Stojan Dimitrovski <sdimitrovski@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OFz SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Comparator;

/**
 * Utility functions for working with byte arrays.
 */
public final class Bytes {
    private Bytes() {
        // No instances.
    }

    /**
     * Utility {@link java.util.Comparator} for lexicographic comparisons of byte arrays.
     *
     * @see #lexicographicCompare(byte[], byte[])
     */
    public static final Comparator<byte[]> COMPARATOR = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] a, byte[] b) {
            return lexicographicCompare(a, b);
        }
    };

    /**
     * Lexicographically compares two byte arrays, the way the default comparator in a
     * {@link com.github.hf.leveldb.implementation.NativeLevelDB} instance works.
     *
     * @param a nullable byte array
     * @param b nullable byte array
     * @return greater than 0 if a > b, less than 0 if a < b, or 0 if a = b
     */
    public static int lexicographicCompare(byte[] a, byte[] b) {
        if (a == b) {
            return 0;
        }

        if (a == null) {
            return -1;
        }

        if (b == null) {
            return 1;
        }

        final int maxlength = Math.min(a.length, b.length);

        for (int i = 0; i < maxlength; i++) {
            if ((a[i] & 0xFF) == (b[i] & 0xFF)) {
                continue;
            }

            if ((a[i] & 0xFF) > (b[i] & 0xFF)) {
                return (i + 1);
            }

            return -(i + 1);
        }

        if (a.length > maxlength) {
            for (int i = b.length; i < a.length; i++) {
                if (a[i] != 0) {
                    return i + 1;
                }
            }
        }

        for (int i = a.length; i < b.length; i++) {
            if (b[i] != 0) {
                return -(i + 1);
            }
        }

        return 0;
    }
}
