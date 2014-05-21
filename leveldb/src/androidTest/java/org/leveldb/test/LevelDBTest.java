package org.leveldb.test;

import android.test.InstrumentationTestCase;
import org.leveldb.LevelDB;
import org.leveldb.WriteBatch;
import org.leveldb.exception.LevelDBException;
import org.leveldb.exception.LevelDBIOException;

import java.io.File;

/**
 * Created by hermann on 5/21/14.
 */
public class LevelDBTest extends InstrumentationTestCase {

    public String getPath(String name) {
        return new File(getInstrumentation().getContext().getFilesDir(), name).getAbsolutePath();
    }

    public void testOpening() throws Exception {
        boolean threwException = false;

        try {
            new LevelDB(getPath("leveldb-test"), false);
        } catch (LevelDBException e) {
            threwException = true;
        }

        assertTrue(threwException);
    }

    public void testOpenAndDestroy() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"), true);

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));

        boolean threwException = false;

        try {
            new LevelDB(getPath("leveldb-test"), false);
        } catch (LevelDBException e) {
            threwException = true;
        }

        assertTrue(threwException);
    }

    public void testGetPutDeleteString() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"), true);

        levelDB.put("key", "value");

        String value = levelDB.get("key");

        assertEquals("value", value);

        levelDB.del("key");

        assertNull(levelDB.get("key"));

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));
    }

    public void testWriteBatch() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"), true);

        levelDB.write(new WriteBatch().put("key", "value").put("data", "store"));

        assertEquals("value", levelDB.get("key"));
        assertEquals("store", levelDB.get("data"));

        levelDB.write(new WriteBatch().put("key", "value1").del("data"));

        assertEquals("value1", levelDB.get("key"));
        assertNull(levelDB.get("data"));

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));
    }
}
