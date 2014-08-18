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

import android.util.Log;
import com.github.hf.leveldb.Iterator;
import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.WriteBatch;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBException;
import com.github.hf.leveldb.util.Bytes;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by hermann on 8/16/14.
 */
public class MockLevelDB extends LevelDB {

    protected volatile boolean closed;

    protected final SortedMap<byte[], byte[]> map = new TreeMap<byte[], byte[]>(Bytes.COMPARATOR);

    @Override
    public void close() {
        boolean multipleClose = false;

        synchronized (this) {
            if (closed) {
                multipleClose = true;
            } else {
                closed = true;
            }
        }

        if (multipleClose) {
            Log.i(MockLevelDB.class.getName(), "Trying to close Mock LevelDB multiple times.");
        }
    }

    @Override
    public synchronized void put(byte[] key, byte[] value, boolean sync) throws LevelDBException {
        if (value == null) {
            del(key, sync);

            return;
        }

        if (key == null) {
            throw new IllegalArgumentException("Key must not be null.");
        }

        checkIfClosed();

        map.put(key, value);
    }

    @Override
    public synchronized void write(WriteBatch writeBatch, boolean sync) throws LevelDBException {
        if (writeBatch == null) {
            throw new IllegalArgumentException("Write batch must not be null.");
        }

        checkIfClosed();

        for (WriteBatch.Operation operation : writeBatch.getAllOperations()) {
            if (operation.isDel()) {
                map.remove(operation.key());
            } else {
                map.put(operation.key(), operation.value());
            }
        }
    }

    @Override
    public synchronized byte[] get(byte[] key) throws LevelDBException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null.");
        }

        checkIfClosed();

        return map.get(key);
    }

    @Override
    public synchronized void del(byte[] key, boolean sync) throws LevelDBException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null.");
        }

        checkIfClosed();

        map.remove(key);
    }

    @Override
    public byte[] getPropertyBytes(byte[] key) throws LevelDBClosedException {
        throw new UnsupportedOperationException("Mock LevelDB does not support properties.");
    }

    @Override
    public synchronized Iterator iterator(boolean fillCache) throws LevelDBClosedException {
        return new MockIterator(map);
    }

    @Override
    public String getPath() {
        return ":MOCK:";
    }

    @Override
    protected void setPath(String path) {
        // No-op.
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    protected void checkIfClosed()throws LevelDBClosedException {
        if (closed) {
            throw new LevelDBClosedException("Mock LevelDB has been closed.");
        }
    }
}
