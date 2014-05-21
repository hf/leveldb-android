#include "org_leveldb_WriteBatch_Native.h"

#include "leveldb/options.h"
#include "leveldb/write_batch.h"

JNIEXPORT jlong JNICALL Java_org_leveldb_WriteBatch_00024Native_ncreate
(JNIEnv *env, jclass cself) {

  leveldb::WriteBatch* writeBatch = new leveldb::WriteBatch();

  return (jlong) writeBatch;
}

JNIEXPORT void JNICALL Java_org_leveldb_WriteBatch_00024Native_nput
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

JNIEXPORT void JNICALL Java_org_leveldb_WriteBatch_00024Native_ndelete
(JNIEnv *env, jclass cself, jlong nwb, jbyteArray key) {

  leveldb::WriteBatch* wb = (leveldb::WriteBatch*) nwb;

  const char* keyData = (char*) env->GetByteArrayElements(key, 0);
  leveldb::Slice keySlice (keyData, (size_t) env->GetArrayLength(key));

  wb->Delete(keySlice);

  env->ReleaseByteArrayElements(key, (jbyte*) keyData, 0);
}

JNIEXPORT void JNICALL Java_org_leveldb_WriteBatch_00024Native_nclose
(JNIEnv *env, jclass cself, jlong nwb) {
  if (nwb != 0) {
    delete ((leveldb::WriteBatch*) nwb);
  }
}
