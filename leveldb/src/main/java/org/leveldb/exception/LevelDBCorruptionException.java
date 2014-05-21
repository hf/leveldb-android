package org.leveldb.exception;

/**
 * Created by hermann on 5/21/14.
 */
public class LevelDBCorruptionException extends LevelDBException {
    public LevelDBCorruptionException(String detailMessage) {
        super(detailMessage);
    }
}
