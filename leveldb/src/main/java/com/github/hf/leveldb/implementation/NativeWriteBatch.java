package com.github.hf.leveldb.implementation;

/*
 * Stojan Dimitrovski
 *
 * 2014
 *
 * In the original BSD license, the occurrence of "copyright holder" in the 3rd
 * clause read "ORGANIZATION", placeholder for "University of California". In the
 * original BSD license, both occurrences of the phrase "COPYRIGHT HOLDERS AND
 * CONTRIBUTORS" in the disclaimer read "REGENTS AND CONTRIBUTORS".
 *
 * Here is the license template:
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
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.util.Log;
import com.github.hf.leveldb.WriteBatch;

import java.io.Closeable;

/**
 * Native object a-la <tt>leveldb::WriteBatch</tt>.
 *
 * Make sure after use you call {@link NativeWriteBatch#close()}.
 */
public class NativeWriteBatch implements Closeable {
    static {
        System.loadLibrary("leveldb");
    }

    // Don't touch this. If you do, something somewhere dies.
    private long nwb;

    public NativeWriteBatch(WriteBatch writeBatch) {
        nwb = ncreate();

        for (WriteBatch.Operation operation : writeBatch) {

            if (operation.isPut()) {
                nput(nwb, operation.getKey(), operation.getValue());
            } else {
                ndelete(nwb, operation.getKey());
            }
        }
    }

    /**
     * Returns the native object's pointer, to be used when calling a native function.
     *
     * @return the native pointer
     */
    protected long nativePointer() {
        return nwb;
    }

    /**
     * Close this object. You may call this multiple times.
     *
     * Use of this object is illegal after calling this.
     */
    @Override
    public void close() {
        if (!isClosed()) {
            nclose(nwb);
            nwb = 0;
        } else {
            Log.i("org.leveldb", "Native WriteBatch is already closed.");
        }
    }

    /**
     * Whether this object is closed.
     *
     * @return
     */
    public boolean isClosed() {
        return nwb == 0;
    }

    /**
     * Native create. Corresponds to: <tt>new leveldb::SimpleWriteBatch()</tt>
     *
     * @return pointer to native structure
     */
    private static native long ncreate();

    /**
     * Native SimpleWriteBatch put. Pointer is unchecked.
     *
     * @param nwb   native structure pointer
     * @param key
     * @param value
     */
    private static native void nput(long nwb, byte[] key, byte[] value);

    /**
     * Native SimpleWriteBatch delete. Pointer is unchecked.
     *
     * @param nwb native structure pointer
     * @param key
     */
    private static native void ndelete(long nwb, byte[] key);

    /**
     * Native close. Releases all memory. Pointer is unchecked.
     *
     * @param nwb native structure pointer
     */
    private static native void nclose(long nwb);
}
