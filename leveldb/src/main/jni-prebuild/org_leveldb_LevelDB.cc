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

#include "org_leveldb_LevelDB.h"
#include <iostream>
#include "leveldb/db.h"
#include "leveldb/write_batch.h"
#include "leveldb/env.h"
#include <android/log.h>

// Redirects leveldb's logging to the Android logger.
class AndroidLogger : public leveldb::Logger {
public:
  virtual void Logv(const char* format, va_list ap) {
    __android_log_vprint(ANDROID_LOG_INFO, "org.leveldb:N", format, ap);
  }
};

// Holds references to heap-allocated native objects so that they can be
// closed in Java_org_leveldb_LevelDB_nclose.
class NDBHolder {
public:
  NDBHolder(leveldb::DB* ldb, AndroidLogger* llogger) : db(ldb), logger(llogger) {}

  leveldb::DB* db;
  AndroidLogger* logger;
};

// Throws the appropriate Java exception for the given status. Make sure you
// check IsNotFound() and similar possible non-exception statuses before calling
// this. Please release all Java references before calling this.
void throwExceptionFromStatus(JNIEnv *env, leveldb::Status &status) {
  if (status.ok()) {
    return;
  }

  if (status.IsIOError()) {
    jclass ioExceptionClass = env->FindClass("org/leveldb/exception/LevelDBIOException");

    env->ThrowNew(ioExceptionClass, status.ToString().data());
  } else if (status.IsCorruption()) {
    jclass corruptionExceptionClass = env->FindClass("org/leveldb/exception/LevelDBCorruptionException");

    env->ThrowNew(corruptionExceptionClass, status.ToString().data());
  } else if (status.IsNotFound()) {
    jclass notFoundExceptionClass = env->FindClass("org/leveldb/exception/LevelDBNotFoundException");

    env->ThrowNew(notFoundExceptionClass, status.ToString().data());
  } else {
    jclass exceptionClass = env->FindClass("org/leveldb/exception/LevelDBException");

    env->ThrowNew(exceptionClass, status.ToString().data());
  }
}

JNIEXPORT jlong JNICALL Java_org_leveldb_LevelDB_nopen
(JNIEnv *env, jclass cself, jboolean createIfMissing, jstring path) {

  const char *nativePath = env->GetStringUTFChars(path, 0);

  leveldb::DB *db;

  AndroidLogger* logger = new AndroidLogger();

  leveldb::Options options;
  options.create_if_missing = createIfMissing == JNI_TRUE;
  options.info_log = logger;

  leveldb::Status status = leveldb::DB::Open(options, nativePath, &db);

  env->ReleaseStringUTFChars(path, nativePath);

  if (status.ok()) {
    NDBHolder* holder = new NDBHolder(db, logger);

    return (jlong) holder;
  } else {
    delete logger;
  }

  throwExceptionFromStatus(env, status);

  return 0;
}

JNIEXPORT void JNICALL Java_org_leveldb_LevelDB_nclose
(JNIEnv *env, jclass cself, jlong ndb) {
  if (ndb != 0) {
    NDBHolder* holder = (NDBHolder*) ndb;

    delete holder->db;
    delete holder->logger;
    delete holder;
  }
}

JNIEXPORT void JNICALL Java_org_leveldb_LevelDB_nput
(JNIEnv *env, jclass cself, jlong ndb, jboolean sync, jbyteArray key, jbyteArray value) {

  NDBHolder* holder = (NDBHolder*) ndb;

  leveldb::DB* db = holder->db;

  leveldb::WriteOptions writeOptions;
  writeOptions.sync = sync == JNI_TRUE;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);
  const char* valueData = (char*) env->GetByteArrayElements(value, 0);

  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));
  leveldb::Slice valueSlice (valueData, (size_t) env->GetArrayLength(value));

  leveldb::Status status = db->Put(writeOptions, keySlice, valueSlice);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);
  env->ReleaseByteArrayElements(value, (jbyte*) valueData, 0);

  throwExceptionFromStatus(env, status);
}

JNIEXPORT void JNICALL Java_org_leveldb_LevelDB_nwrite
  (JNIEnv *env, jclass cself, jlong ndb, jboolean sync, jlong nwb) {

  NDBHolder* holder = (NDBHolder*) ndb;

  leveldb::DB* db = holder->db;

  leveldb::WriteOptions options;
  options.sync = sync == JNI_TRUE;

  leveldb::WriteBatch* wb = (leveldb::WriteBatch*) nwb;

  leveldb::Status status = db->Write(options, wb);

  throwExceptionFromStatus(env, status);
}

JNIEXPORT jbyteArray JNICALL Java_org_leveldb_LevelDB_nget
(JNIEnv *env, jclass cself, jlong ndb, jbyteArray key) {

  NDBHolder* holder = (NDBHolder*) ndb;

  leveldb::DB* db = holder->db;

  leveldb::ReadOptions readOptions;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);

  leveldb::Slice keySlice (keyData, env->GetArrayLength(key));

  std::string value;

  leveldb::Status status = db->Get(readOptions, keySlice, &value);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);

  if (status.ok()) {
    if (value.length() < 1) {
      return 0;
    }

    jbyteArray retval = env->NewByteArray(value.length());

    env->SetByteArrayRegion(retval, 0, value.length(), (jbyte*) value.data());

    return retval;
  } else if (status.IsNotFound()) {
    return 0;
  }

  throwExceptionFromStatus(env, status);

  return 0;
}

JNIEXPORT void JNICALL Java_org_leveldb_LevelDB_ndelete
(JNIEnv *env, jclass cself, jlong ndb, jboolean sync, jbyteArray key) {

  NDBHolder* holder = (NDBHolder*) ndb;

  leveldb::DB* db = holder->db;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);

  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));

  leveldb::WriteOptions writeOptions;
  writeOptions.sync = sync == JNI_TRUE;

  leveldb::Status status = db->Delete(writeOptions, keySlice);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);

  throwExceptionFromStatus(env, status);
}

JNIEXPORT jbyteArray JNICALL Java_org_leveldb_LevelDB_ngetProperty
(JNIEnv *env, jclass cself, jlong ndb, jbyteArray key) {

  NDBHolder* holder = (NDBHolder*) ndb;

  leveldb::DB* db = holder->db;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);

  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));

  leveldb::ReadOptions readOptions;

  std::string value;

  bool ok = db->GetProperty(keySlice, &value);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);

  if (ok) {
    if (value.length() < 1) {
      return 0;
    }

    jbyteArray retval = env->NewByteArray(value.length());

    env->SetByteArrayRegion(retval, 0, value.length(), (jbyte*) value.data());

    return retval;
  }

  return 0;
}

JNIEXPORT void JNICALL Java_org_leveldb_LevelDB_ndestroy
(JNIEnv *env, jclass cself, jstring path) {

  const char *nativePath = env->GetStringUTFChars(path, 0);

  leveldb::Status status = leveldb::DestroyDB(nativePath, leveldb::Options());

  env->ReleaseStringUTFChars(path, nativePath);

  throwExceptionFromStatus(env, status);
}

JNIEXPORT void JNICALL Java_org_leveldb_LevelDB_nrepair
(JNIEnv *env, jclass cself, jstring path) {

  const char *nativePath = env->GetStringUTFChars(path, 0);

  leveldb::Status status = leveldb::RepairDB(nativePath, leveldb::Options());

  env->ReleaseStringUTFChars(path, nativePath);

  throwExceptionFromStatus(env, status);
}
