package org.leveldb.exception;

/**
 * Created by hermann on 5/21/14.
 */
public class LevelDBClosedException extends LevelDBException {
    public LevelDBClosedException() {
        this("This database has been closed!");
    }

    public LevelDBClosedException(String detailMessage) {
        super(detailMessage);
    }
}
