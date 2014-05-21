package org.leveldb;

import android.util.Log;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds a batch write operation. (Something like a transaction.)
 */
public class WriteBatch {

    /**
     * Native object a-la <tt>leveldb::WriteBatch</tt>.
     *
     * Make sure after use you call {@link org.leveldb.WriteBatch.Native#close()}.
     */
    protected static class Native implements Closeable {
        static {
            System.loadLibrary("leveldb");
        }

        // Don't touch this. If you do, something somewhere dies.
        private long nwb;

        protected Native(List<Operation> operations) {
            nwb = ncreate();

            for (Operation operation : operations) {
                switch (operation.type) {
                    case Operation.PUT:
                        nput(nwb, operation.key, operation.value);

                        break;

                    case Operation.DELETE:
                        ndelete(nwb, operation.key);

                        break;
                }
            }
        }

        /**
         * Returns the native object's pointer, to be used when calling a native function.
         *
         * @return the native pointer
         */
        protected long nativePointer() {
            return nwb;
        }

        /**
         * Close this object. You may call this multiple times.
         *
         * Use of this object is illegal after calling this.
         */
        @Override
        public void close() {
            if (!isClosed()) {
                nclose(nwb);
                nwb = 0;
            } else {
                Log.i("org.leveldb", "Native WriteBatch is already closed.");
            }
        }

        /**
         * Whether this object is closed.
         *
         * @return
         */
        public boolean isClosed() {
            return nwb == 0;
        }

        /**
         * Native create. Corresponds to: <tt>new leveldb::WriteBatch()</tt>
         *
         * @return pointer to native structure
         */
        private static native long ncreate();

        /**
         * Native WriteBatch put. Pointer is unchecked.
         *
         * @param nwb   native structure pointer
         * @param key
         * @param value
         */
        private static native void nput(long nwb, byte[] key, byte[] value);

        /**
         * Native WriteBatch delete. Pointer is unchecked.
         *
         * @param nwb native structure pointer
         * @param key
         */
        private static native void ndelete(long nwb, byte[] key);

        /**
         * Native close. Releases all memory. Pointer is unchecked.
         *
         * @param nwb native structure pointer
         */
        private static native void nclose(long nwb);
    }

    /**
     * An operation on the database (put or delete).
     */
    private static class Operation {
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
    }

    private LinkedList<Operation> operations;

    /**
     * Creates a new empty WriteBatch. Use {@link org.leveldb.LevelDB#write(WriteBatch, boolean)} or variants to write
     * it to the database.
     */
    public WriteBatch() {
        operations = new LinkedList<Operation>();
    }

    /**
     * Writes a key-value pair to the database.
     *
     * @param key   the key to write
     * @param value the value to write
     * @return the WriteBatch object for chaining
     * @see org.leveldb.LevelDB#put(byte[], byte[], boolean)
     */
    public WriteBatch put(byte[] key, byte[] value) {
        operations.add(Operation.put(key, value));

        return this;
    }

    /**
     * Writes a key-value pair to the database.
     *
     * @param key   the key to write
     * @param value the value to write
     * @return the WriteBatch object for chaining
     * @see org.leveldb.LevelDB#put(String, byte[])
     */
    public WriteBatch put(String key, byte[] value) {
        return put(key.getBytes(), value);
    }

    /**
     * Writes a key-value pair to the database.
     *
     * @param key   the key to write
     * @param value the value to write
     * @return the WriteBatch object for chaining
     * @see org.leveldb.LevelDB#put(String, String)
     */
    public WriteBatch put(String key, String value) {
        return put(key, value.getBytes());
    }

    /**
     * Deletes key from database.
     *
     * @param key
     * @return the WriteBatch object for chaining
     * @see org.leveldb.LevelDB#del(byte[], boolean)
     */
    public WriteBatch del(byte[] key) {
        operations.add(Operation.del(key));

        return this;
    }

    /**
     * Removes key-value pair
     *
     * @param key
     * @return the WriteBatch object for chaining
     */
    public WriteBatch del(String key) {
        return del(key.getBytes());
    }

    /**
     * Creates a new {@link org.leveldb.WriteBatch.Native} object for internal use.
     *
     * @return a new native object with the specified operations
     */
    protected Native toNative() {
        return new Native(operations);
    }
}
