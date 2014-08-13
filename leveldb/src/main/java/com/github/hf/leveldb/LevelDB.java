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
import com.github.hf.leveldb.exception.LevelDBException;
import com.github.hf.leveldb.implementation.NativeLevelDB;

import java.io.Closeable;

/**
 * Created by hermann on 8/13/14.
 */
public abstract class LevelDB implements Closeable {
    /**
     * Destroys the contents of a LevelDB database.
     *
     * @see com.github.hf.leveldb.implementation.NativeLevelDB#destroy(String)
     *
     * @param path the path to the database
     * @throws com.github.hf.leveldb.exception.LevelDBException
     */
    public static void destroy(String path) throws LevelDBException {
        NativeLevelDB.destroy(path);
    }

    /**
     * If a DB cannot be opened, you may attempt to call this method to resurrect as much of the contents of the
     * database as possible. Some data may be lost, so be careful when calling this function on a database that contains
     * important information.
     *
     * @see com.github.hf.leveldb.implementation.NativeLevelDB#repair(String)
     *
     * @param path the path to the database
     * @throws com.github.hf.leveldb.exception.LevelDBException
     */
    public static void repair(String path) throws LevelDBException {
        NativeLevelDB.repair(path);
    }

    public static Configuration configure() {
        return new Configuration();
    }

    public static LevelDB open(String path) throws LevelDBException {
        return open(path, configure());
    }

    /**
     * Opens a new database.
     *
     * @see com.github.hf.leveldb.implementation.NativeLevelDB(String, com.github.hf.leveldb.LevelDB.Configuration)
     *
     * @param path
     * @param configuration
     * @return
     * @throws LevelDBException
     */
    public static LevelDB open(String path, Configuration configuration) throws LevelDBException {
        return new NativeLevelDB(path, configuration);
    }

    @Override
    public abstract void close();

    public abstract void put(byte[] key, byte[] value, boolean sync) throws LevelDBException;

    /**
     * Convenience function.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @param sync  whether this is a synchronous (true) or asynchronous (false) write
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, byte[] value, boolean sync) throws LevelDBException {
        put(key.getBytes(), value, sync);
    }

    /**
     * Convenience function. Writes are async.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, byte[] value) throws LevelDBException {
        put(key, value, false);
    }

    /**
     * Convenience function.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @param sync  whether this is a synchronous (true) or asynchronous (false) write
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, String value, boolean sync) throws LevelDBException {
        put(key.getBytes(), value.getBytes(), sync);
    }

    /**
     * Convenience function. Writes are async.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, String value) throws LevelDBException {
        put(key, value, false);
    }

    public abstract void write(WriteBatch writeBatch, boolean sync) throws LevelDBException;

    /**
     * Writes a {@link WriteBatch} to the database, asynchronously.
     *
     * @param writeBatch the WriteBatch to write
     * @throws com.github.hf.leveldb.exception.LevelDBException
     */
    public void write(WriteBatch writeBatch) throws LevelDBException {
        write(writeBatch, false);
    }

    public abstract byte[] getBytes(byte[] key) throws LevelDBException;

    /**
     * Gets the value associated with the key, or <tt>null</tt>.
     *
     * @param key the key, encoded with the system's encoding
     * @return the value, or <tt>null</tt>
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#getBytes(String)
     */
    public byte[] getBytes(String key) throws LevelDBException {
        return getBytes(key.getBytes());
    }

    /**
     * Gets the string value associated with the key, or <tt>null</tt>.
     *
     * @param key the key, encoded with the system's encoding
     * @return the value, decoded with the system's default encoding, or <tt>null</tt>
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#getBytes(byte[])
     */
    public String get(String key) throws LevelDBException {
        byte[] value = getBytes(key);

        if (value == null) {
            return null;
        }

        return new String(value);
    }

    public abstract void del(byte[] key, boolean sync) throws LevelDBException;

    /**
     * Convenience function.
     *
     * @param key  the key, encoded with the system's encoding
     * @param sync whether this is a synchronous (true) or asynchronous (false) delete
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.LevelDB#del(byte[], boolean)
     */
    public void del(String key, boolean sync) throws LevelDBException {
        del(key.getBytes(), sync);
    }

    /**
     * Convenience function. Deletion is asynchronous.
     *
     * @param key the key, encoded with the system's encoding
     * @throws com.github.hf.leveldb.exception.LevelDBException
     */
    public void del(String key) throws LevelDBException {
        del(key.getBytes(), false);
    }

    public abstract byte[] getPropertyBytes(byte[] key) throws LevelDBClosedException;

    /**
     * Convenience function.
     *
     * @param key the key
     * @return property data encoded with the system's default encoding, or <tt>null</tt>
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     * @see com.github.hf.leveldb.LevelDB#getPropertyBytes(byte[])
     */
    public String getProperty(byte[] key) throws LevelDBClosedException {
        byte[] value = getPropertyBytes(key);

        if (value == null) {
            return null;
        }

        return new String(value);
    }

    /**
     * Convenience function.
     *
     * @param key the key, encoded with the system's encoding
     * @return property data encoded with the system's default encoding, or <tt>null</tt>
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     */
    public String getProperty(String key) throws LevelDBClosedException {
        return getProperty(key.getBytes());
    }

    public abstract Iterator iterator(boolean fillCache) throws LevelDBClosedException;

    /**
     * Creates a new iterator that fills the cache.
     *
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     * @returna new iterator
     * @see #iterator(boolean)
     */
    public Iterator iterator() throws LevelDBClosedException {
        return iterator(true);
    }

    public abstract String getPath();

    protected abstract void setPath(String path);

    public abstract boolean isClosed();

    /**
     * Specifies a configuration to open the database with.
     */
    public static final class Configuration {
        private boolean createIfMissing;
        private int cacheSize;
        private int blockSize;
        private int writeBufferSize;

        private Configuration() {
            createIfMissing = true;
        }

        public boolean createIfMissing() {
            return createIfMissing;
        }

        public Configuration createIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;

            return this;
        }

        public int cacheSize() {
            return cacheSize;
        }

        public Configuration cacheSize(int cacheSize) {
            this.cacheSize = Math.abs(cacheSize);

            return this;
        }

        public int blockSize() {
            return this.blockSize;
        }

        public Configuration blockSize(int blockSize) {
            this.blockSize = Math.abs(blockSize);

            return this;
        }

        public int writeBufferSize() {
            return writeBufferSize;
        }

        public Configuration writeBufferSize(int writeBufferSize) {
            this.writeBufferSize = writeBufferSize;

            return this;
        }
    }
}
