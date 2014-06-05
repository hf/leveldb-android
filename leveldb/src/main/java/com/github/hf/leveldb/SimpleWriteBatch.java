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

import com.github.hf.leveldb.exception.LevelDBException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simple implementation of {@link com.github.hf.leveldb.WriteBatch}.
 */
public class SimpleWriteBatch implements WriteBatch {

    /**
     * A simple implementation of {@link com.github.hf.leveldb.WriteBatch.Operation}.
     */
    private static class Operation implements WriteBatch.Operation {
        public static final int PUT = 0;
        public static final int DELETE = 1;

        private int type;
        private byte[] key;
        private byte[] value;

        public static Operation put(byte[] key, byte[] value) {
            return new Operation(PUT, key, value);
        }

        public static Operation del(byte[] key) {
            return new Operation(DELETE, key, null);
        }

        private Operation(int type, byte[] key, byte[] value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }

        @Override
        public byte[] getKey() {
            return key;
        }

        @Override
        public byte[] getValue() {
            return value;
        }

        @Override
        public boolean isPut() {
            return type == PUT;
        }

        @Override
        public boolean isDel() {
            return type == DELETE;
        }
    }

    private LinkedList<WriteBatch.Operation> operations;

    /**
     * Creates a new empty SimpleWriteBatch.
     *
     * Use {@link LevelDB#write(com.github.hf.leveldb.WriteBatch, boolean)} or {@link
     * com.github.hf.leveldb.SimpleWriteBatch#write(LevelDB, boolean)} to write it to the database.
     */
    public SimpleWriteBatch() {
        operations = new LinkedList<WriteBatch.Operation>();
    }

    @Override
    public SimpleWriteBatch put(byte[] key, byte[] value) {
        operations.add(Operation.put(key, value));

        return this;
    }

    @Override
    public SimpleWriteBatch del(byte[] key) {
        operations.add(Operation.del(key));

        return this;
    }

    @Override
    public SimpleWriteBatch insert(WriteBatch.Operation operation) {
        operations.add(operation);

        return this;
    }

    @Override
    public Iterator<WriteBatch.Operation> iterator() {
        return operations.iterator();
    }

    @Override
    public Collection<WriteBatch.Operation> getAllOperations() {
        return new ArrayList<WriteBatch.Operation>(operations);
    }

    @Override
    public void write(LevelDB levelDB, boolean sync) throws LevelDBException {
        levelDB.write(this, sync);
    }

    @Override
    public void write(LevelDB levelDB) throws LevelDBException {
        write(levelDB, false);
    }

    public SimpleWriteBatch put(String key, byte[] value) {
        put(key.getBytes(), value);

        return this;
    }

    public SimpleWriteBatch put(String key, String value) {
        return put(key, value.getBytes());
    }

    public SimpleWriteBatch del(String key) {
        return del(key.getBytes());
    }
}
