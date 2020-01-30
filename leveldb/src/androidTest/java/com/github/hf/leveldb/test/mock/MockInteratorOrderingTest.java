package com.github.hf.leveldb.test.mock;

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
import com.github.hf.leveldb.implementation.mock.MockLevelDB;
import com.github.hf.leveldb.util.Bytes;
import junit.framework.TestCase;

import static com.google.common.truth.Truth.assertThat;


/**
 * Created by hermann on 8/17/14.
 */
public class MockInteratorOrderingTest extends TestCase {

    public void testOrdering() throws Exception {
        MockLevelDB mock = new MockLevelDB();

        mock.put(new byte[] { 0, 0, 1 }, new byte[] { 1 }, false);
        mock.put(new byte[] { 0, 0, 2 }, new byte[] { 2 }, false);
        mock.put(new byte[] { 0, 0, 3 }, new byte[] { 3 }, false);

        Iterator iterator = mock.iterator(false);

        iterator.seekToFirst();

        int i = 1;

        while (iterator.isValid()) {
            byte[] key = iterator.key();
            byte[] val = iterator.value();

            assertThat(key).isNotNull();
            assertThat(val).isNotNull();

            assertThat(key[key.length - 1] & 0xFF).isEqualTo(i);
            assertThat(val[0] & 0xFF).isEqualTo(i);

            iterator.next();
            i++;
        }

        assertThat(i).isEqualTo(4);

        iterator.close();

        mock.put(new byte[] { 0, 0, 10 }, new byte[] { 10 }, false);

        iterator = mock.iterator();

        iterator.seek(new byte[] { 0, 0, 4 });

        assertThat(iterator.isValid()).isTrue();

        byte[] key = iterator.key();

        assertThat(Bytes.lexicographicCompare(new byte[] { 0, 0, 10 }, key) == 0).isTrue();

        iterator.previous();

        assertThat(iterator.isValid());

        key = iterator.key();

        assertThat(Bytes.lexicographicCompare(new byte[] { 0, 0, 3 }, key) == 0).isTrue();

        iterator.close();

        mock.close();
    }

    public void testClosed() throws Exception {
        MockLevelDB mock = new MockLevelDB();

        Iterator iterator = mock.iterator(true);

        mock.close();

        assertThat(iterator.isClosed());
    }
}
