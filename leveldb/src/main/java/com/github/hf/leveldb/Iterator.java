package com.github.hf.leveldb;

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

import com.github.hf.leveldb.exception.LevelDBClosedException;

import java.io.Closeable;

/**
 * An iterator is used to iterator over the entries in the database according to the total sort order imposed by the
 * comparator.
 */
public class Iterator implements Closeable {

    // Don't touch this or all hell breaks loose.
    private long nit;

    /**
     * Protected constructor used in {@link com.github.hf.leveldb.LevelDB#iterator(boolean)}.
     *
     * @param nit the native pointer
     */
    protected Iterator(long nit) {
        if (nit == 0) {
            throw new IllegalArgumentException("Native iterator pointer must not be NULL!");
        }

        this.nit = nit;
    }

    /**
     * Whether this pointer is valid. An iterator is valid iff it is positioned over a key-value pair.
     *
     * @return whether the iterator is valid
     * @throws LevelDBClosedException
     */
    public boolean isValid() throws LevelDBClosedException {
        checkIfClosed();

        return nvalid(this.nit);
    }

    /**
     * Seeks to the first key-value pair in the database.
     *
     * @throws LevelDBClosedException
     */
    public void seekToFirst() throws LevelDBClosedException {
        checkIfClosed();

        nseekToFirst(this.nit);
    }

    /**
     * Seeks to the last key-value pair in the database.
     *
     * NB: Reverse iteration is somewhat slower than forward iteration.
     *
     * @throws LevelDBClosedException
     */
    public void seekToLast() throws LevelDBClosedException {
        checkIfClosed();

        nseekToLast(this.nit);
    }

    /**
     * Seek to the given key, or right after it.
     *
     * @param key the key, never <tt>null</tt>
     * @throws LevelDBClosedException
     */
    public void seek(byte[] key) throws LevelDBClosedException {
        checkIfClosed();

        if (key == null) {
            throw new IllegalArgumentException("Seek key must never be null!");
        }

        nseek(nit, key);
    }

    /**
     * @param key
     * @throws LevelDBClosedException
     * @see #seek(byte[])
     */
    public void seek(String key) throws LevelDBClosedException {
        if (key == null) {
            throw new IllegalArgumentException("Seek key must never be null!");
        }

        seek(key.getBytes());
    }

    /**
     * Advance the iterator forward.
     *
     * Requires: {@link #isValid()}
     *
     * @throws LevelDBClosedException
     */
    public void next() throws LevelDBClosedException {
        checkIfClosed();

        nnext(nit);
    }

    /**
     * Advance the iterator backward.
     *
     * Requires: {@link #isValid()}
     *
     * @throws LevelDBClosedException
     */
    public void previous() throws LevelDBClosedException {
        checkIfClosed();

        nprev(nit);
    }

    /**
     * Get the key under the iterator.
     *
     * Requires: {@link #isValid()}
     *
     * @return the key under the iterator, <tt>null</tt> if invalid
     * @throws LevelDBClosedException
     */
    public byte[] keyBytes() throws LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            return null;
        }

        return nkey(nit);
    }

    /**
     * Get the key under the iterator as a {@link java.lang.String}.
     *
     * Requires: {@link #isValid()}
     *
     * @return the key under the iterator, <tt>null</tt> if invalid
     * @throws LevelDBClosedException
     */
    public String key() throws LevelDBClosedException {
        byte[] key = keyBytes();

        if (key != null) {
            return new String(key);
        }

        return null;
    }

    /**
     * Get the value under the iterator.
     *
     * Requires: {@link #isValid()}
     *
     * @return the value under the iterator, <tt>null</tt> if invalid
     * @throws LevelDBClosedException
     */
    public byte[] valueBytes() throws LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            return null;
        }

        return nvalue(nit);
    }

    /**
     * Gets the value under the iterator as a {@link java.lang.String}.
     *
     * @return the value under the iterator, <tt>null</tt> if invalid
     * @throws LevelDBClosedException
     */
    public String value() throws LevelDBClosedException {
        byte[] value = valueBytes();

        if (value != null) {
            return new String(value);
        }

        return null;
    }

    /**
     * Whether this iterator has been closed.
     *
     * @return
     */
    public boolean isClosed() {
        return nit == 0;
    }

    /**
     * Closes this iterator. It will be almost unusable after.
     *
     * Always close the iterator before closing the database.
     */
    @Override
    public void close() {
        if (!isClosed()) {
            nclose(nit);
        }

        nit = 0;
    }

    /**
     * Checks if this iterator has been closed.
     *
     * @throws LevelDBClosedException
     */
    private void checkIfClosed() throws LevelDBClosedException {
        if (isClosed()) {
            throw new LevelDBClosedException("Iterator has been closed.");
        }
    }

    private static native void nclose(long nit);

    private static native boolean nvalid(long nit);

    private static native void nseek(long nit, byte[] key);

    private static native void nseekToFirst(long nit);

    private static native void nseekToLast(long nit);

    private static native void nnext(long nit);

    private static native void nprev(long nit);

    private static native byte[] nkey(long nit);

    private static native byte[] nvalue(long nit);
}
