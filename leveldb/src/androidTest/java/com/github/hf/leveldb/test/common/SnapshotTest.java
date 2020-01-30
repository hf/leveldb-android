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

import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.Snapshot;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBSnapshotOwnershipException;
import com.github.hf.leveldb.implementation.mock.MockLevelDB;
import com.github.hf.leveldb.implementation.mock.MockSnapshot;
import com.github.hf.leveldb.util.Bytes;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public abstract class SnapshotTest extends DatabaseTestCase {

    @Test
    public void testObtainReleaseSnapshot() throws Exception {
        LevelDB db = obtainLevelDB();

        Snapshot snapshot = db.obtainSnapshot();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.isReleased()).isFalse();

        db.releaseSnapshot(snapshot);

        assertThat(snapshot.isReleased()).isTrue();

        snapshot = db.obtainSnapshot();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.isReleased()).isFalse();

        db.releaseSnapshot(snapshot);

        boolean threw = false;

        try {
            db.releaseSnapshot(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        threw = false;

        try {
            db.releaseSnapshot(new MockSnapshot(new MockLevelDB()));
        } catch (LevelDBSnapshotOwnershipException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        db.close();

        threw = false;

        try {
            db.releaseSnapshot(snapshot);
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertThat(threw).isTrue();

        assertThat(snapshot.isReleased()).isTrue();

        db = null;

        System.gc();

        assertThat(snapshot.isReleased()).isTrue();
    }

    @Test
    public void testGet() throws Exception {
        LevelDB db = obtainLevelDB();

        db.put(new byte[] { 1, 2, 3 }, new byte[] { 1, 2, 3 });
        db.put(new byte[] { 3, 4, 5 }, new byte[] { 3, 4, 5 });

        Snapshot snapshotA = db.obtainSnapshot();

        db.put(new byte[] { 5, 6, 7 }, new byte[] { 5, 6, 7 });

        assertThat(db.get(new byte[] { 5, 6, 7 })).isNotNull();

        Snapshot snapshotB = db.obtainSnapshot();

        byte[] value = db.get(new byte[] { 1, 2, 3 }, snapshotA);

        assertThat(value).isNotNull();
        assertThat(Bytes.lexicographicCompare(value, new byte[] { 1, 2, 3 })).isEqualTo(0);

        value = db.get(new byte[] { 1, 2, 3 }, snapshotB);

        assertThat(value).isNotNull();
        assertThat(Bytes.lexicographicCompare(value, new byte[] { 1, 2, 3 })).isEqualTo(0);

        value = db.get(new byte[] { 3, 4, 5 }, snapshotA);

        assertThat(value).isNotNull();
        assertThat(Bytes.lexicographicCompare(value, new byte[] { 3, 4, 5 })).isEqualTo(0);

        value = db.get(new byte[] { 3, 4, 5 }, snapshotB);

        assertThat(value).isNotNull();
        assertThat(Bytes.lexicographicCompare(value, new byte[] { 3, 4, 5 })).isEqualTo(0);

        value = db.get(new byte[] { 5, 6, 7 }, snapshotA);

        assertThat(value).isNull();

        value = db.get(new byte[] { 5, 6, 7 }, snapshotB);

        assertThat(value).isNotNull();
        assertThat(Bytes.lexicographicCompare(value, new byte[] { 5, 6, 7 })).isEqualTo(0);

        db.releaseSnapshot(snapshotA);
        db.releaseSnapshot(snapshotB);

        db.close();
    }

    @Test
    public void testIteration() throws Exception {


    }
}
