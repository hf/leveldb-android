package com.github.hf.leveldb.implementation.mock;

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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public final class MockSnapshot extends Snapshot {
    private WeakReference<LevelDB> owner;

    private volatile boolean released;
    private volatile SortedMap<byte[], byte[]> snapshot;

    public MockSnapshot(MockLevelDB mockLevelDB) {
        this.owner = new WeakReference<LevelDB>(mockLevelDB);

        this.snapshot = Collections.unmodifiableSortedMap(new TreeMap<byte[], byte[]>(mockLevelDB.map));

        this.released = false;
    }

    @Override
    public synchronized boolean isReleased() {
        LevelDB owner = this.owner.get();

        return released || owner == null || owner.isClosed();
    }

    protected boolean checkOwnership(LevelDB owner) {
        return this.owner.get() == owner;
    }

    protected synchronized void release() {
        released = true;
        snapshot = null;
    }

    protected synchronized SortedMap<byte[], byte[]> getSnapshot() {
        return snapshot;
    }
}
