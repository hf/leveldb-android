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

#include "com_github_hf_leveldb_implementation_NativeWriteBatch.h"

#include "leveldb/options.h"
#include "leveldb/write_batch.h"

JNIEXPORT jlong JNICALL Java_com_github_hf_leveldb_implementation_NativeWriteBatch_ncreate
(JNIEnv *env, jclass cself) {

  leveldb::WriteBatch* WriteBatchImplementation = new leveldb::WriteBatch();

  return (jlong) WriteBatchImplementation;
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_implementation_NativeWriteBatch_nput
(JNIEnv *env, jclass cself, jlong nwb, jbyteArray key, jbyteArray value) {

  leveldb::WriteBatch* wb = (leveldb::WriteBatch*) nwb;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);
  const char* valueData = (char*) env->GetByteArrayElements(value, 0);

  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));
  leveldb::Slice valueSlice (valueData, (size_t) env->GetArrayLength(value));

  wb->Put(keySlice, valueSlice);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);
  env->ReleaseByteArrayElements(value, (jbyte*) valueData, 0);
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_implementation_NativeWriteBatch_ndelete
(JNIEnv *env, jclass cself, jlong nwb, jbyteArray key) {

  leveldb::WriteBatch* wb = (leveldb::WriteBatch*) nwb;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);
  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));

  wb->Delete(keySlice);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_implementation_NativeWriteBatch_nclose
(JNIEnv *env, jclass cself, jlong nwb) {
  if (nwb != 0) {
    delete ((leveldb::WriteBatch*) nwb);
  }
}
