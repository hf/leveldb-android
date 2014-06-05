#include "com_github_hf_leveldb_Iterator.h"
#include "leveldb/iterator.h"
#include "leveldb/slice.h"
#include "leveldb/options.h"
#include "leveldb/status.h"

#include <android/log.h>

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_Iterator_nclose
(JNIEnv *env, jclass cself, jlong nit) {
  if (nit == 0) {
    return;
  }

  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld) about to close with status: %s", nit, status.ToString().data());
  }

  delete it;
}

JNIEXPORT jboolean JNICALL Java_com_github_hf_leveldb_Iterator_nvalid
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  jboolean retval = (jboolean) it->Valid();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?
  }

  return retval;
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_Iterator_nseek
(JNIEnv *env, jclass cself, jlong nit, jbyteArray key) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);
  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));

  it->Seek(keySlice);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?
  }
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_Iterator_nseekToFirst
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  it->SeekToFirst();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?
  }
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_Iterator_nseekToLast
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  it->SeekToLast();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?
  }
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_Iterator_nnext
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  it->Next();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?
  }
}

JNIEXPORT void JNICALL Java_com_github_hf_leveldb_Iterator_nprev
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  it->Prev();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?
  }
}

JNIEXPORT jbyteArray JNICALL Java_com_github_hf_leveldb_Iterator_nkey
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  if (!it->Valid()) {
    return 0;
  }

  leveldb::Slice key = it->key();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?

    return 0;
  }

  jbyteArray retval = env->NewByteArray(key.size());

  env->SetByteArrayRegion(retval, 0, key.size(), (jbyte*) key.data());

  return retval;
}

JNIEXPORT jbyteArray JNICALL Java_com_github_hf_leveldb_Iterator_nvalue
(JNIEnv *env, jclass cself, jlong nit) {
  leveldb::Iterator* it = (leveldb::Iterator*) nit;

  if (!it->Valid()) {
    return 0;
  }

  leveldb::Slice value = it->value();

  leveldb::Status status = it->status();

  if (!status.ok()) {
    __android_log_print(ANDROID_LOG_INFO, "com.github.leveldb:N", "Iterator(%lld): %s", nit, status.ToString().data());

    // TODO: Probably throw Exception here?

    return 0;
  }

  jbyteArray retval = env->NewByteArray(value.size());

  env->SetByteArrayRegion(retval, 0, value.size(), (jbyte*) value.data());

  return retval;
}
