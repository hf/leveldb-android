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

import android.util.Log;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBException;

import java.io.Closeable;

/**
 * Object for interacting with the native LevelDB.
 */
public class LevelDB implements Closeable {
    static {
        System.loadLibrary("leveldb");
    }

    // This is the underlying pointer. If you touch this, all hell breaks loose and everyone dies.
    private long ndb;

    private String path;

    /**
     * Destroys the contents of a LevelDB database.
     *
     * @param path the path to the database
     * @throws LevelDBException
     */
    public static void destroy(String path) throws LevelDBException {
        ndestroy(path);
    }

    /**
     * If a DB cannot be opened, you may attempt to call this method to resurrect as much of the contents of the
     * database as possible. Some data may be lost, so be careful when calling this function on a database that contains
     * important information.
     *
     * @param path the path to the database
     * @throws LevelDBException
     */
    public static void repair(String path) throws LevelDBException {
        nrepair(path);
    }

    /**
     * Opens a new LevelDB database.
     *
     * @param path            the path to the database
     * @param createIfMissing whether the database should be created if missing
     * @throws LevelDBException
     */
    public LevelDB(String path, boolean createIfMissing) throws LevelDBException {
        ndb = nopen(createIfMissing, 2 * (2 << 10), 0, 0, path);

        setPath(path);
    }

    /**
     * Opens a new LevelDB database, and creates it if missing.
     *
     * @param path the path to the database
     * @throws LevelDBException
     * @see LevelDB#LevelDB(String, boolean)
     */
    public LevelDB(String path) throws LevelDBException {
        this(path, true);
    }

    /**
     * Closes this database, i.e. releases native resources. You may call this multiple times. You cannot use any other
     * method on this object after closing it.
     */
    @Override
    public void close() {
        if (ndb != 0) {
            nclose(ndb);
            ndb = 0;
        } else {
            Log.i("org.leveldb", "Trying to close database multiple times.");
        }
    }

    /**
     * Writes a key-value record to the database. Wirting can be synchronous or asynchronous.
     *
     * Asynchronous writes will be buffered to the kernel before this function returns. This guarantees data consistency
     * even if the process crashes or is killed, but not if the system crashes.
     *
     * Synchronous writes block everything until data gets written to disk. Data is secure even if the system crashes.
     *
     * @param key   the key (usually a string, but bytes are the way LevelDB stores things)
     * @param value the value
     * @param sync  whether this is a synchronous (true) or asynchronous (false) write
     * @throws LevelDBException
     */
    public void put(byte[] key, byte[] value, boolean sync) throws LevelDBException {
        checkIfClosed();

        nput(ndb, sync, key, value);
    }

    /**
     * Convenience function.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @param sync  whether this is a synchronous (true) or asynchronous (false) write
     * @throws LevelDBException
     * @see LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, byte[] value, boolean sync) throws LevelDBException {
        put(key.getBytes(), value, sync);
    }

    /**
     * Convenience function. Writes are async.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @throws LevelDBException
     * @see LevelDB#put(byte[], byte[], boolean)
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
     * @throws LevelDBException
     * @see LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, String value, boolean sync) throws LevelDBException {
        put(key.getBytes(), value.getBytes(), sync);
    }

    /**
     * Convenience function. Writes are async.
     *
     * @param key   key to use, converted to byte using the system's default encoding
     * @param value the value
     * @throws LevelDBException
     * @see LevelDB#put(byte[], byte[], boolean)
     */
    public void put(String key, String value) throws LevelDBException {
        put(key, value, false);
    }

    /**
     * Writes a {@link com.github.hf.leveldb.WriteBatch} to the database.
     *
     * @param writeBatch the WriteBatch to write
     * @param sync whether this is a synchronous (true) or asynchronous (false) write
     * @throws LevelDBException
     */
    public void write(WriteBatch writeBatch, boolean sync) throws LevelDBException {
        checkIfClosed();

        WriteBatch.Native nativeWriteBatch = new WriteBatch.Native(writeBatch);

        nwrite(ndb, sync, nativeWriteBatch.nativePointer());

        nativeWriteBatch.close();

        nativeWriteBatch = null;
    }

    /**
     * Writes a {@link com.github.hf.leveldb.WriteBatch} to the database, asynchronously.
     *
     * @param writeBatch the WriteBatch to write
     * @throws LevelDBException
     */
    public void write(WriteBatch writeBatch) throws LevelDBException {
        write(writeBatch, false);
    }

    /**
     * Creates a new {@link com.github.hf.leveldb.SimpleWriteBatch}.
     *
     * @return
     */
    public SimpleWriteBatch writeBatch() {
        return new SimpleWriteBatch();
    }

    /**
     * Gets the value associated with the key, or <tt>null</tt>.
     *
     * @param key the key
     * @return the value, or <tt>null</tt>
     * @throws LevelDBException
     */
    public byte[] getBytes(byte[] key) throws LevelDBException {
        checkIfClosed();

        return nget(ndb, key);
    }

    /**
     * Gets the value associated with the key, or <tt>null</tt>.
     *
     * @param key the key, encoded with the system's encoding
     * @return the value, or <tt>null</tt>
     * @throws LevelDBException
     * @see LevelDB#getBytes(String)
     */
    public byte[] getBytes(String key) throws LevelDBException {
        return getBytes(key.getBytes());
    }

    /**
     * Gets the string value associated with the key, or <tt>null</tt>.
     *
     * @param key the key, encoded with the system's encoding
     * @return the value, decoded with the system's default encoding, or <tt>null</tt>
     * @throws LevelDBException
     * @see LevelDB#getBytes(byte[])
     */
    public String get(String key) throws LevelDBException {
        byte[] value = getBytes(key);

        if (value == null) {
            return null;
        }

        return new String(value);
    }

    /**
     * Deletes the specified entry from the database. Deletion can be synchronous or asynchronous.
     *
     * @param key  the key
     * @param sync whether this is a synchronous (true) or asynchronous (false) delete
     * @throws LevelDBException
     */
    public void del(byte[] key, boolean sync) throws LevelDBException {
        checkIfClosed();

        ndelete(ndb, sync, key);
    }

    /**
     * Convenience function.
     *
     * @param key  the key, encoded with the system's encoding
     * @param sync whether this is a synchronous (true) or asynchronous (false) delete
     * @throws LevelDBException
     * @see LevelDB#del(byte[], boolean)
     */
    public void del(String key, boolean sync) throws LevelDBException {
        del(key.getBytes(), sync);
    }

    /**
     * Convenience function. Deletion is asynchronous.
     *
     * @param key the key, encoded with the system's encoding
     * @throws LevelDBException
     */
    public void del(String key) throws LevelDBException {
        del(key.getBytes(), false);
    }

    /**
     * Get a property of LevelDB, or null.
     *
     * Valid property names include:
     *
     * <ul> <li>"leveldb.num-files-at-level<N>" - return the number of files at level <N>, where <N> is an ASCII
     * representation of a level number (e.g. "0").</li>
     *
     * <li>"leveldb.stats" - returns a multi-line string that describes statistics about the internal operation of the
     * DB.</li>
     *
     * <li>"leveldb.sstables" - returns a multi-line string that describes all of the sstables that make up the db
     * contents.</li>
     *
     * </ul>
     *
     * @param key the key
     * @return property data, or <tt>null</tt>
     * @throws LevelDBClosedException
     */
    public byte[] getPropertyBytes(byte[] key) throws LevelDBClosedException {
        checkIfClosed();

        return ngetProperty(ndb, key);
    }

    /**
     * Convenience function.
     *
     * @param key the key
     * @return property data encoded with the system's default encoding, or <tt>null</tt>
     * @throws LevelDBClosedException
     * @see LevelDB#getPropertyBytes(byte[])
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
     * @throws LevelDBClosedException
     */
    public String getProperty(String key) throws LevelDBClosedException {
        return getProperty(key.getBytes());
    }

    /**
     * Creates a new {@link com.github.hf.leveldb.Iterator} that iterates over this database.
     *
     * The returned iterator is not thread safe and must be closed with {@link Iterator#close()}
     * before closing this database.
     *
     * @param fillCache whether iterating fills the internal cache
     * @return a new iterator
     * @throws LevelDBClosedException
     */
    public Iterator iterator(boolean fillCache) throws LevelDBClosedException {
        checkIfClosed();

        return new Iterator(niterate(ndb, fillCache));
    }

    /**
     * Creates a new iterator that fills the cache.
     *
     * @see #iterator(boolean)
     *
     * @returna new iterator
     * @throws LevelDBClosedException
     */
    public Iterator iterator() throws LevelDBClosedException {
        return iterator(true);
    }

    /**
     * The path that this database has been opened with.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path which opens this database.
     *
     * @param path
     */
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * Checks whether this database has been closed.
     *
     * @return true if closed, false if not
     */
    public boolean isClosed() {
        return ndb == 0;
    }

    /**
     * Checks if this database has been closed. If it has, throws a {@link com.github.hf.leveldb.exception.LevelDBClosedException}.
     *
     * Use before calling any of the native functions that require the ndb pointer.
     *
     * @throws LevelDBClosedException
     */
    protected void checkIfClosed() throws LevelDBClosedException {
        if (isClosed()) {
            throw new LevelDBClosedException();
        }
    }

    /**
     * Natively opens the database.
     *
     * @param createIfMissing
     * @param path
     * @return the native structure pointer
     * @throws LevelDBException
     */
    private static native long nopen(boolean createIfMissing, int cacheSize, int blockSize, int writeBufferSize, String path) throws LevelDBException;

    /**
     * Natively closes pointers and memory. Pointer is unchecked.
     *
     * @param ndb
     */
    private static native void nclose(long ndb);

    /**
     * Natively writes key-value pair to the database. Pointer is unchecked.
     *
     * @param ndb
     * @param sync
     * @param key
     * @param value
     * @throws LevelDBException
     */
    private static native void nput(long ndb, boolean sync, byte[] key, byte[] value) throws LevelDBException;

    /**
     * Natively deletes key-value pair from the database. Pointer is unchecked.
     *
     * @param ndb
     * @param sync
     * @param key
     * @throws LevelDBException
     */
    private static native void ndelete(long ndb, boolean sync, byte[] key) throws LevelDBException;

    private static native void nwrite(long ndb, boolean sync, long nwb) throws LevelDBException;

    /**
     * Natively retrieves key-value pair from the database. Pointer is unchecked.
     *
     * @param ndb
     * @param key
     * @return
     * @throws LevelDBException
     */
    private static native byte[] nget(long ndb, byte[] key) throws LevelDBException;

    /**
     * Natively gets LevelDB property. Pointer is unchecked.
     *
     * @param ndb
     * @param key
     * @return
     */
    private static native byte[] ngetProperty(long ndb, byte[] key);

    /**
     * Natively destroys a database. Corresponds to: <tt>leveldb::DestroyDB()</tt>
     *
     * @param path
     * @throws LevelDBException
     */
    private static native void ndestroy(String path) throws LevelDBException;

    /**
     * Natively repairs a database. Corresponds to: <tt>leveldb::RepairDB()</tt>
     *
     * @param path
     * @throws LevelDBException
     */
    private static native void nrepair(String path) throws LevelDBException;

    /**
     * Natively creates a new iterator. Corresponds to <tt>leveldb::DB->NewIterator()</tt>.
     *
     * @param ndb
     * @param fillCache
     * @return
     */
    private static native long niterate(long ndb, boolean fillCache);
}
