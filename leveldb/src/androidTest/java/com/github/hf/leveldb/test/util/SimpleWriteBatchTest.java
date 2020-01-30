package com.github.hf.leveldb.test.util;

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

import com.github.hf.leveldb.util.SimpleWriteBatch;
import com.github.hf.leveldb.WriteBatch;
import junit.framework.TestCase;

/**
 * Created by hermann on 8/16/14.
 */
public class SimpleWriteBatchTest extends TestCase {

    public void testOperations() {
        SimpleWriteBatch writeBatch = new SimpleWriteBatch();

        writeBatch.put(new byte[]{ 1, 2, 3 }, new byte[]{ 1, 2, 3 });

        assertEquals(1, writeBatch.getAllOperations().size());

        writeBatch.del(new byte[] { 1, 2, 3 });

        assertEquals(2, writeBatch.getAllOperations().size());

        int del = 0;
        int put = 0;

        for (WriteBatch.Operation operation : writeBatch) {
            if (operation.isPut()) {
                put++;
            } else if (operation.isDel()) {
                del++;
            }

            assertNotNull(operation.key());

            if (!operation.isDel()) {
                assertNotNull(operation.value());
            }
        }

        assertEquals(1, del);
        assertEquals(1, put);

        boolean threw = false;

        try {
            writeBatch.put(null, null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);
        assertEquals(2, writeBatch.getAllOperations().size());

        threw = false;

        try {
            writeBatch.del(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);
        assertEquals(2, writeBatch.getAllOperations().size());

        threw = false;

        try {
            writeBatch.put(null, null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);
        assertEquals(2, writeBatch.getAllOperations().size());

        threw = false;

        try {
            writeBatch.del(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);
        assertEquals(2, writeBatch.getAllOperations().size());
    }
}
