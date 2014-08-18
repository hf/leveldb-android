package com.github.hf.leveldb.implementation.mock;

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
import com.github.hf.leveldb.util.Bytes;

import java.util.*;

/**
 * Created by hermann on 8/16/14.
 */
public class MockIterator extends Iterator {

    protected boolean closed;

    protected final SortedMap<byte[], byte[]> snapshot;
    protected final ArrayList<byte[]> keys;

    int position = 0;

    public MockIterator(SortedMap<byte[], byte[]> map) {
        this.snapshot = Collections.unmodifiableSortedMap(map);
        this.keys = new ArrayList<byte[]>(map.keySet());

        Collections.sort(this.keys, Bytes.COMPARATOR);
    }

    @Override
    public boolean isValid() throws LevelDBClosedException {
        checkIfClosed();

        return position > -1 && position < keys.size();
    }

    @Override
    public void seekToFirst() throws LevelDBClosedException {
        checkIfClosed();

        position = 0;
    }

    @Override
    public void seekToLast() throws LevelDBClosedException {
        checkIfClosed();

        position = keys.size() - 1;
    }

    @Override
    public void seek(byte[] key) throws LevelDBClosedException {
        checkIfClosed();

        position = Collections.binarySearch(keys, key, Bytes.COMPARATOR);

        if (position < 0) {
            position = -position - 1;
        }
    }

    @Override
    public void next() throws LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        position++;
    }

    @Override
    public void previous() throws LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        position--;
    }

    @Override
    public byte[] key() throws LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        return keys.get(position);
    }

    @Override
    public byte[] value() throws LevelDBClosedException {
        checkIfClosed();

        if (!isValid()) {
            throw new LevelDBIteratorNotValidException();
        }

        return snapshot.get(keys.get(position));
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
    }

    protected void checkIfClosed() throws LevelDBClosedException {
        if (closed) {
            throw new LevelDBClosedException("Iterator has been closed.");
        }
    }
}
