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
 * Created by hermann on 8/13/14.
 */
public abstract class Iterator implements Closeable {
    public abstract boolean isValid() throws LevelDBClosedException;

    public abstract void seekToFirst() throws LevelDBClosedException;

    public abstract void seekToLast() throws LevelDBClosedException;

    public abstract void seek(byte[] key) throws LevelDBClosedException;

    public abstract void seek(String key) throws LevelDBClosedException;

    public abstract void next() throws LevelDBClosedException;

    public abstract void previous() throws LevelDBClosedException;

    public abstract byte[] keyBytes() throws LevelDBClosedException;

    /**
     * Get the key under the iterator as a {@link String}.
     *
     * Requires: {@link #isValid()}
     *
     * @return the key under the iterator, <tt>null</tt> if invalid
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    public String key() throws LevelDBClosedException {
        byte[] key = keyBytes();

        if (key != null) {
            return new String(key);
        }

        return null;
    }

    public abstract byte[] valueBytes() throws LevelDBClosedException;

    /**
     * Gets the value under the iterator as a {@link String}.
     *
     * @return the value under the iterator, <tt>null</tt> if invalid
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    public String value() throws LevelDBClosedException {
        byte[] value = valueBytes();

        if (value != null) {
            return new String(value);
        }

        return null;
    }

    public abstract boolean isClosed();

    @Override
    public abstract void close();
}
