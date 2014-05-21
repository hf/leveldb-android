package org.leveldb.test;

import android.test.InstrumentationTestCase;
import org.leveldb.LevelDB;

import java.io.File;

/**
 * Created by hermann on 5/21/14.
 */
public class LevelDBTest extends InstrumentationTestCase {

    public void testGeneral() throws Exception {

        File filesDir = getInstrumentation().getContext().getFilesDir();

        LevelDB levelDB = new LevelDB(new File(filesDir, "leveldb").getAbsolutePath(), true);

        levelDB.put("key", "value");

        assertTrue("value".equals(levelDB.get("key")));

        levelDB.del("key");

        assertNull(levelDB.get("key"));

        levelDB.close();
    }
}
