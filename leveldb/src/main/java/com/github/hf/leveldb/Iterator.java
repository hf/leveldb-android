package com.github.hf.leveldb;

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

import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBIteratorNotValidException;

import java.io.Closeable;

public abstract class Iterator implements Closeable {
    /**
     * Checks if there is a key-value pair over the current position of the iterator.
     *
     * @throws LevelDBClosedException
     */
    public abstract boolean isValid() throws LevelDBClosedException;

    /**
     * Moves to the first key-value pair in the database.
     *
     * @throws LevelDBClosedException
     */
    public abstract void seekToFirst() throws LevelDBClosedException;

    /**
     * Moves to the last key-value pair in the database.
     *
     * @throws LevelDBClosedException
     */
    public abstract void seekToLast() throws LevelDBClosedException;

    /**
     * Moves on top of, or just after key, in the database.
     *
     * @param key the key to seek, if null throws an {@link java.lang.IllegalArgumentException}
     * @throws LevelDBClosedException
     */
    public abstract void seek(byte[] key) throws LevelDBClosedException;

    /**
     * Moves to the next entry in the database.
     *
     * @throws LevelDBIteratorNotValidException if not {@link #isValid()}
     * @throws LevelDBClosedException
     */
    public abstract void next() throws LevelDBIteratorNotValidException, LevelDBClosedException;

    /**
     * Moves to the previous entry in the database.
     *
     * @throws LevelDBIteratorNotValidException if not {@link #isValid()}
     * @throws LevelDBClosedException
     */
    public abstract void previous() throws LevelDBIteratorNotValidException, LevelDBClosedException;

    /**
     * Returns the key under the iterator.
     *
     * @return the key
     * @throws LevelDBIteratorNotValidException if not {@link #isValid()}
     * @throws LevelDBClosedException
     */
    public abstract byte[] key() throws LevelDBIteratorNotValidException, LevelDBClosedException;

    /**
     * Returns the value under the iterator.
     *
     * @return the value
     * @throws LevelDBIteratorNotValidException if not {@link #isValid()}
     * @throws LevelDBClosedException
     */
    public abstract byte[] value() throws LevelDBClosedException;

    /**
     * Checks whether this iterator has been closed.
     */
    public abstract boolean isClosed();

    /**
     * Closes this iterator if it has not been. It is usually unusable after a call to this method.
     */
    @Override
    public abstract void close();
}
