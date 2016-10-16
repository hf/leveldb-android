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
import com.github.hf.leveldb.Iterator;
import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.Snapshot;
import com.github.hf.leveldb.WriteBatch;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBException;
import com.github.hf.leveldb.exception.LevelDBSnapshotOwnershipException;

/**
 * Object for interacting with the native LevelDB implementation.
 */
public class NativeLevelDB extends LevelDB {
    static {
        System.loadLibrary("leveldb");
    }

    /**
     * @see com.github.hf.leveldb.LevelDB#destroy(String)
     */
    public static void destroy(String path) throws LevelDBException {
        ndestroy(path);
    }

    /**
     * @see com.github.hf.leveldb.LevelDB#repair(String)
     */
    public static void repair(String path) throws LevelDBException {
        nrepair(path);
    }

    // This is the underlying pointer. If you touch this, all hell breaks loose and everyone dies.
    private volatile long ndb;

    private volatile String path;

    /**
     * Opens a new LevelDB database.
     *
     * @param path          the path to the database
     * @param configuration configuration for this database
     * @throws LevelDBException
     * @see NativeLevelDB.Configuration
     */
    public NativeLevelDB(String path, Configuration configuration) throws LevelDBException {
        if (configuration == null) {
            configuration = configure();
        }

        ndb = nopen(configuration.createIfMissing(),
                configuration.paranoidChecks(),
                configuration.reuseLogs(),
                configuration.exceptionIfExists(),
                configuration.cacheSize(),
                configuration.blockSize(),
                configuration.writeBufferSize(),
                configuration.maxOpenFiles(),
                path);

        setPath(path);
    }

    /**
     * Opens a new LevelDB database, and creates it if missing.
     *
     * @param path the path to the database
     * @throws LevelDBException
     * @see NativeLevelDB#NativeLevelDB(String, LevelDB.Configuration)
     */
    public NativeLevelDB(String path) throws LevelDBException {
        this(path, null);
    }

    /**
     * Closes this database, i.e. releases nat resources. You may call this multiple times. You cannot use any other
     * method on this object after closing it.
     */
    @Override
    public void close() {
        boolean closeMultiple = false;

        synchronized (this) {
            if (ndb != 0) {
                nclose(ndb);
                ndb = 0;
            } else {
                closeMultiple = true;
            }
        }

        if (closeMultiple) {
            Log.i(NativeLevelDB.class.getName(), "Trying to close database multiple times.");
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
    @Override
    public void put(byte[] key, byte[] value, boolean sync) throws LevelDBException {
        if (value == null) {
            del(key, sync);

            return;
        }

        if (key == null) {
            throw new IllegalArgumentException("Key must not be null!");
        }

        synchronized (this) {
            checkIfClosed();

            nput(ndb, sync, key, value);
        }
    }

    /**
     * Writes a {@link com.github.hf.leveldb.WriteBatch} to the database.
     *
     * @param writeBatch the WriteBatch to write
     * @param sync       whether this is a synchronous (true) or asynchronous (false) write
     * @throws LevelDBException
     */
    @Override
    public void write(WriteBatch writeBatch, boolean sync) throws LevelDBException {
        if (writeBatch == null) {
            throw new IllegalArgumentException("Write batch must not be null.");
        }

        synchronized (this) {
            checkIfClosed();

            NativeWriteBatch nativeWriteBatch = new NativeWriteBatch(writeBatch);

            try {
                nwrite(ndb, sync, nativeWriteBatch.nativePointer());
            } finally {
                nativeWriteBatch.close();
                nativeWriteBatch = null;
            }
        }
    }

    /**
     * Gets the value associated with the key, or <tt>null</tt>.
     *
     * @param key the key
     * @param snapshot the snapshot from which to read the pair, or null
     * @return the value, or <tt>null</tt>
     * @throws LevelDBException
     */
    @Override
    public byte[] get(byte[] key, Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null!");
        }

        if (snapshot != null) {
            if (!(snapshot instanceof NativeSnapshot)) {
                throw new LevelDBSnapshotOwnershipException();
            }

            if (!((NativeSnapshot) snapshot).checkOwner(this)) {
                throw new LevelDBSnapshotOwnershipException();
            }
        }

        synchronized (this) {
            checkIfClosed();

            return nget(ndb, key, snapshot == null ? 0 : ((NativeSnapshot) snapshot).id());
        }
    }

    /**
     * Deletes the specified entry from the database. Deletion can be synchronous or asynchronous.
     *
     * @param key  the key
     * @param sync whether this is a synchronous (true) or asynchronous (false) delete
     * @throws LevelDBException
     */
    @Override
    public void del(byte[] key, boolean sync) throws LevelDBException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null.");
        }

        synchronized (this) {
            checkIfClosed();

            ndelete(ndb, sync, key);
        }
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
    @Override
    public byte[] getPropertyBytes(byte[] key) throws LevelDBClosedException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null.");
        }

        synchronized (this) {
            checkIfClosed();

            return ngetProperty(ndb, key);
        }
    }

    /**
     * Creates a new {@link com.github.hf.leveldb.Iterator} that iterates over this database.
     *
     * The returned iterator is not thread safe and must be closed with {@link com.github.hf.leveldb.Iterator#close()} before closing this
     * database.
     *
     * @param fillCache whether iterating fills the internal cache
     * @return a new iterator
     * @throws LevelDBClosedException
     */
    @Override
    public Iterator iterator(boolean fillCache, Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBClosedException {
        if (snapshot != null) {
            if (!(snapshot instanceof NativeSnapshot)) {
                throw new LevelDBSnapshotOwnershipException();
            }

            if (((NativeSnapshot) snapshot).checkOwner(this)) {
                throw new LevelDBSnapshotOwnershipException();
            }
        }

        synchronized (this) {
            checkIfClosed();

            return new NativeIterator(niterate(ndb, fillCache, snapshot == null ? 0 : ((NativeSnapshot) snapshot).id()));
        }
    }

    /**
     * The path that this database has been opened with.
     *
     * @return the path
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Set the path which opens this database.
     *
     * @param path
     */
    @Override
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * Checks whether this database has been closed.
     *
     * @return true if closed, false if not
     */
    @Override
    public boolean isClosed() {
        return ndb == 0;
    }

    @Override
    public Snapshot obtainSnapshot() throws LevelDBClosedException {
        return new NativeSnapshot(this, nsnapshot(ndb));
    }

    @Override
    public void releaseSnapshot(Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBClosedException {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot must not be null.");
        }

        if (!(snapshot instanceof NativeSnapshot)) {
            throw new LevelDBSnapshotOwnershipException();
        }

        if (!((NativeSnapshot) snapshot).checkOwner(this)) {
            throw new LevelDBSnapshotOwnershipException();
        }

        synchronized (this) {
            checkIfClosed();

            nreleaseSnapshot(ndb, ((NativeSnapshot) snapshot).release());
        }
    }

    /**
     * Checks if this database has been closed. If it has, throws a {@link com.github.hf.leveldb.exception.LevelDBClosedException}.
     *
     * Use before calling any of the nat functions that require the ndb pointer.
     *
     * Don't call this outside a synchronized context.
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
     * @return the nat structure pointer
     * @throws LevelDBException
     */
    private static native long nopen(boolean createIfMissing, boolean paranoidChecks, boolean reuseLogs, boolean exceptionIfExists, int cacheSize, int blockSize, int writeBufferSize, int maxOpenFiles, String path) throws LevelDBException;

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
    private static native byte[] nget(long ndb, byte[] key, long nsnapshot) throws LevelDBException;

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
    private static native long niterate(long ndb, boolean fillCache, long nsnapshot);

    private static native long nsnapshot(long ndb);
    private static native void nreleaseSnapshot(long ndb, long nsnapshot);
}
