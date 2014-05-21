package org.leveldb;

import org.leveldb.exception.LevelDBException;

/**
 * Created by hermann on 5/20/14.
 */
public class LevelDB {
    static {
        System.loadLibrary("leveldb");
    }

    // This is the underlying pointer. If you touch this, all hell breaks loose and everyone dies.
    private long ndb;

    public LevelDB(String path, boolean createIfMissing) throws LevelDBException {
        ndb = nopen(createIfMissing, path);
    }

    public void close() {
        nclose(ndb);
        ndb = 0;
    }

    public void put(String key, String value) throws LevelDBException {
        nput(ndb, false, key.getBytes(), value.getBytes());
    }

    public String get(String key) throws LevelDBException {
        byte[] value = nget(ndb, key.getBytes());

        if (value == null) {
            return null;
        }

        return new String(value);
    }

    public void del(String key) throws LevelDBException {
        ndelete(ndb, false, key.getBytes());
    }

    private static native long nopen(boolean createIfMissing, String path) throws LevelDBException;
    private static native void nclose(long ndb);
    private static native void nput(long ndb, boolean sync, byte[] key, byte[] value) throws LevelDBException;
    private static native void ndelete(long ndb, boolean sync, byte[] key) throws LevelDBException;
    private static native byte[] nget(long ndb, byte[] key) throws LevelDBException;

    private static native byte[] ngetProperty(long ndb, byte[] key);

    private static native void ndestroy(String path) throws LevelDBException;
    private static native void nrepair(String path) throws LevelDBException;
}
