package com.github.hf.leveldb.test;

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

import android.test.InstrumentationTestCase;
import android.util.Log;
import com.github.hf.leveldb.Iterator;
import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.SimpleWriteBatch;
import com.github.hf.leveldb.exception.LevelDBException;

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
            new LevelDB(getPath("leveldb-test"), LevelDB.configure().createIfMissing(false));
        } catch (LevelDBException e) {
            threwException = true;
        }

        assertTrue(threwException);
    }

    public void testOpenAndDestroy() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"));

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));

        boolean threwException = false;

        try {
            new LevelDB(getPath("leveldb-test"), LevelDB.configure().createIfMissing(false));
        } catch (LevelDBException e) {
            threwException = true;
        }

        assertTrue(threwException);
    }

    public void testGetPutDeleteString() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"));

        long start = System.currentTimeMillis();
        levelDB.put("key", "value");
        long end = System.currentTimeMillis();

        Log.d(LevelDBTest.class.getName(), String.format("Time: %d", end - start));

        String value = levelDB.get("key");

        assertEquals("value", value);

        levelDB.del("key");

        assertNull(levelDB.get("key"));

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));
    }

    public void testWriteBatch() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"));

        levelDB.writeBatch().put("key", "value").put("data", "store").write(levelDB);

        assertEquals("value", levelDB.get("key"));
        assertEquals("store", levelDB.get("data"));

        levelDB.writeBatch().put("key", "value1").del("data").write(levelDB);

        assertEquals("value1", levelDB.get("key"));
        assertNull(levelDB.get("data"));

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));
    }

    public static final String[] ITERATION = {
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h"
    };

    public void testIteration() throws Exception {
        LevelDB levelDB = new LevelDB(getPath("leveldb-test"));

        SimpleWriteBatch writeBatch = levelDB.writeBatch();

        for (String i : ITERATION) {
            writeBatch.put(i, i);
        }

        writeBatch.write(levelDB);

        Iterator iterator = levelDB.iterator();

        assertFalse(iterator.isValid());

        assertNull(iterator.key());
        assertNull(iterator.value());

        iterator.seekToFirst();

        for (int i = 0; i < ITERATION.length; i++) {
            assertTrue(iterator.isValid());

            byte[] key = iterator.keyBytes();
            byte[] value = iterator.valueBytes();

            String keyStringI = iterator.key();
            String valueStringI = iterator.value();

            assertNotNull(key);
            assertNotNull(value);

            String keyString = new String(key);
            String valueString = new String(value);

            assertEquals(keyString, ITERATION[i]);
            assertEquals(valueString, ITERATION[i]);

            assertEquals(keyString, keyStringI);
            assertEquals(valueString, valueStringI);

            iterator.next();
        }

        assertFalse(iterator.isValid());

        assertNull(iterator.key());
        assertNull(iterator.value());

        iterator.close();

        levelDB.close();

        LevelDB.destroy(getPath("leveldb-test"));
    }
}
