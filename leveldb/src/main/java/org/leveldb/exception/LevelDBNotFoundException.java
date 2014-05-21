package org.leveldb.exception;

/**
 * Created by hermann on 5/21/14.
 */
public class LevelDBNotFoundException extends LevelDBException {
    public LevelDBNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
