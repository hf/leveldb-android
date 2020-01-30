package com.github.hf.leveldb.implementation;

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
        System.loadLibrary("leveldb-android");
    }

    // Don't touch this. If you do, something somewhere dies.
    private long nwb;

    public NativeWriteBatch(WriteBatch writeBatch) {
        nwb = nativeCreate();

        for (WriteBatch.Operation operation : writeBatch) {

            if (operation.isPut()) {
                nativePut(nwb, operation.key(), operation.value());
            } else {
                nativeDelete(nwb, operation.key());
            }
        }
    }

    /**
     * Returns the nat object's pointer, to be used when calling a nat function.
     *
     * @return the nat pointer
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
            nativeClose(nwb);
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
     * @return pointer to nat structure
     */
    private static native long nativeCreate();

    /**
     * Native SimpleWriteBatch put. Pointer is unchecked.
     *
     * @param nwb   nat structure pointer
     * @param key
     * @param value
     */
    private static native void nativePut(long nwb, byte[] key, byte[] value);

    /**
     * Native SimpleWriteBatch delete. Pointer is unchecked.
     *
     * @param nwb nat structure pointer
     * @param key
     */
    private static native void nativeDelete(long nwb, byte[] key);

    /**
     * Native close. Releases all memory. Pointer is unchecked.
     *
     * @param nwb nat structure pointer
     */
    private static native void nativeClose(long nwb);
}
