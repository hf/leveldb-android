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

import com.github.hf.leveldb.Iterator;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBIteratorNotValidException;

/**
 * An iterator is used to iterator over the entries in the database according to the total sort order imposed by the
 * comparator.
 */
public class NativeIterator extends Iterator {

    // Don't touch this or all hell breaks loose.
    private long nit;

    /**
     * Protected constructor used in {@link NativeLevelDB#iterator(boolean)}.
     *
     * @param nit the nat pointer
     */
    protected NativeIterator(long nit) {
        if (nit == 0) {
            throw new IllegalArgumentException("Native iterator pointer must not be NULL!");
        }

        this.nit = nit;
    }

    /**
     * Whether this pointer is valid. An iterator is valid iff it is positioned over a key-value pair.
     *
     * @return whether the iterator is valid
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public boolean isValid() throws LevelDBClosedException {
        checkIfClosed();

        return nativeValid(this.nit);
    }

    /**
     * Seeks to the first key-value pair in the database.
     *
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public void seekToFirst() throws LevelDBClosedException {
        checkIfClosed();

        nativeSeekToFirst(this.nit);
    }

    /**
     * Seeks to the last key-value pair in the database.
     *
     * NB: Reverse iteration is somewhat slower than forward iteration.
     *
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public void seekToLast() throws LevelDBClosedException {
        checkIfClosed();

        nativeSeekToLast(this.nit);
    }

    /**
     * Seek to the given key, or right after it.
     *
     * @param key the key, never <tt>null</tt>
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public void seek(byte[] key) throws LevelDBClosedException {
        checkIfClosed();

        if (key == null) {
            throw new IllegalArgumentException("Seek key must never be null!");
        }

        nativeSeek(nit, key);
    }

    /**
     * Advance the iterator forward.
     *
     * Requires: {@link #isValid()}
     *
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public void next() throws LevelDBIteratorNotValidException, LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        nativeNext(nit);
    }

    /**
     * Advance the iterator backward.
     *
     * Requires: {@link #isValid()}
     *
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public void previous() throws LevelDBIteratorNotValidException, LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        nativePrev(nit);
    }

    /**
     * Get the key under the iterator.
     *
     * Requires: {@link #isValid()}
     *
     * @return the key under the iterator, <tt>null</tt> if invalid
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public byte[] key() throws LevelDBIteratorNotValidException, LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        return nativeKey(nit);
    }

    /**
     * Get the value under the iterator.
     *
     * Requires: {@link #isValid()}
     *
     * @return the value under the iterator, <tt>null</tt> if invalid
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    @Override public byte[] value() throws LevelDBIteratorNotValidException, LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        return nativeValue(nit);
    }

    /**
     * Whether this iterator has been closed.
     *
     * @return
     */
    @Override public boolean isClosed() {
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
            nativeClose(nit);
        }

        nit = 0;
    }

    /**
     * Checks if this iterator has been closed.
     *
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    private void checkIfClosed() throws LevelDBClosedException {
        if (isClosed()) {
            throw new LevelDBClosedException("Iterator has been closed.");
        }
    }

    private static native void nativeClose(long nit);

    private static native boolean nativeValid(long nit);

    private static native void nativeSeek(long nit, byte[] key);

    private static native void nativeSeekToFirst(long nit);

    private static native void nativeSeekToLast(long nit);

    private static native void nativeNext(long nit);

    private static native void nativePrev(long nit);

    private static native byte[] nativeKey(long nit);

    private static native byte[] nativeValue(long nit);
}
