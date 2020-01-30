package com.github.hf.leveldb.test.nat;

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

import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.exception.LevelDBException;
import com.github.hf.leveldb.implementation.NativeLevelDB;
import com.github.hf.leveldb.test.common.DatabaseTestCase;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
/**
 * Created by hermann on 8/16/14.
 */
public class NativeOpenCloseTest extends DatabaseTestCase {

    @Override
    protected LevelDB obtainLevelDB() {
        throw new UnsupportedOperationException("This is a nat-only test case. Shouldn't use this method.");
    }

    @Test
    public void testCreateAndOpenNonExistingDatabase() throws Exception {
        assertThat(dbFile.exists()).isFalse();

        NativeLevelDB ndb = new NativeLevelDB(dbFile.getAbsolutePath(), LevelDB.configure().createIfMissing(true));

        ndb.close();

        assertThat(dbFile.exists()).isTrue();
    }

    @Test
    public void testOpenAnExistingDatabase() throws Exception {
        assertThat(dbFile.exists()).isFalse();

        NativeLevelDB ndb = new NativeLevelDB(dbFile.getAbsolutePath(), LevelDB.configure().createIfMissing(true));

        ndb.close();

        assertThat(dbFile.exists()).isTrue();

        ndb = new NativeLevelDB(dbFile.getAbsolutePath(), LevelDB.configure().createIfMissing(false));

        ndb.close();

        assertThat(dbFile.exists()).isTrue();
    }

    @Test
    public void testTwiceOpenADatabase() throws Exception {
        assertThat(dbFile.exists()).isFalse();

        boolean threw = false;

        NativeLevelDB ndbA = new NativeLevelDB(dbFile.getAbsolutePath(), LevelDB.configure().createIfMissing(true));

        try {
            NativeLevelDB ndbB = new NativeLevelDB(dbFile.getAbsolutePath(), LevelDB.configure().createIfMissing(true));
        } catch (LevelDBException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        ndbA.close();
        ndbA.close();

        assertThat(dbFile.exists()).isTrue();
    }
}
