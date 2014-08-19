package com.github.hf.leveldb.test.common;

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

import com.github.hf.leveldb.Iterator;
import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.util.SimpleWriteBatch;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBIteratorNotValidException;
import com.github.hf.leveldb.util.Bytes;

import static org.assertj.core.api.Assertions.*;

public abstract class IterationTest extends DatabaseTestCase {

    public void testIteration() throws Exception {
        LevelDB db = obtainLevelDB();

        SimpleWriteBatch wb = new SimpleWriteBatch(db);

        wb.put(new byte[] { 0, 0, 1 }, new byte[] { 1 });
        wb.put(new byte[] { 0, 0, 2 }, new byte[] { 2 });
        wb.put(new byte[] { 0, 0, 3 }, new byte[] { 3 });

        wb.write();

        Iterator iterator = db.iterator();

        db.put(new byte[] { 0, 0, 0 }, new byte[] { 0 });

        iterator.seekToFirst();

        byte i = 1;
        while (iterator.isValid()) {

            byte[] key = iterator.key();
            byte[] val = iterator.value();

            assertThat(key).isNotNull();
            assertThat(Bytes.lexicographicCompare(key, new byte[] { 0, 0, i })).isEqualTo(0);
            assertThat(Bytes.lexicographicCompare(val, new byte[] { i })).isEqualTo(0);

            iterator.next();
            i++;
        }

        assertThat(i & 0xFF).isEqualTo(4);

        iterator.close();
        iterator.close();

        iterator = db.iterator();

        iterator.seekToLast();

        assertThat(iterator.isValid()).isTrue();

        i = 3;
        while (iterator.isValid()) {

            byte[] key = iterator.key();
            byte[] val = iterator.value();

            assertThat(key).isNotNull();
            assertThat(Bytes.lexicographicCompare(key, new byte[] { 0, 0, i })).isEqualTo(0);
            assertThat(Bytes.lexicographicCompare(val, new byte[] { i })).isEqualTo(0);

            iterator.previous();
            i--;
        }

        assertThat(i & 0xFF).isEqualTo(255);

        boolean threw = false;

        try {
            iterator.next();
        } catch (LevelDBIteratorNotValidException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        threw = false;

        try {
            iterator.previous();
        } catch (LevelDBIteratorNotValidException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        iterator.close();

        db.close();
    }

    public void testClosed() throws Exception {
        LevelDB db = obtainLevelDB();

        Iterator iterator = db.iterator(true);

        iterator.close();

        boolean threw = false;

        try {
            iterator.isValid();
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        threw = false;

        try {
            iterator.next();
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        threw = false;

        try {
            iterator.previous();
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        threw = false;

        try {
            iterator.key();
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        threw = false;

        try {
            iterator.value();
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        db.close();
    }
}
