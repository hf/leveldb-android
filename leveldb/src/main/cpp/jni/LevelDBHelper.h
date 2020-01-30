//
// Created by todor on 28.06.19.
//

#ifndef LEVELDB_ANDROID_LEVELDBHELPER_H
#define LEVELDB_ANDROID_LEVELDBHELPER_H

#include <leveldb/env.h>
#include <leveldb/db.h>

#include <android/log.h>

// Redirects leveldb's logging to the Android logger.
class AndroidLogger : public leveldb::Logger {
public:
    void Logv(const char *format, va_list ap) override {
        __android_log_vprint(ANDROID_LOG_INFO, "com.github.hf.leveldb:N", format, ap);
    }
};

// Holds references to heap-allocated native objects so that they can be
// closed in Java_com_github_hf_leveldb_implementation_NativeLevelDB_nativeClose.
class NDBHolder {
public:
    NDBHolder(leveldb::DB *ldb, AndroidLogger *llogger, leveldb::Cache *lcache) : db(ldb), logger(llogger),
                                                                                  cache(lcache) {}

    leveldb::DB *db;
    AndroidLogger *logger;

    leveldb::Cache *cache;
};

// Throws the appropriate Java exception for the given status. Make sure you
// check IsNotFound() and similar possible non-exception statuses before calling
// this. Please release all Java references before calling this.
void throwExceptionFromStatus(JNIEnv *env, leveldb::Status &status) {
    if (status.ok()) {
        return;
    }

    if (status.IsIOError()) {
        jclass ioExceptionClass = env->FindClass("com/github/hf/leveldb/exception/LevelDBIOException");

        env->ThrowNew(ioExceptionClass, status.ToString().data());
    } else if (status.IsCorruption()) {
        jclass corruptionExceptionClass = env->FindClass("com/github/hf/leveldb/exception/LevelDBCorruptionException");

        env->ThrowNew(corruptionExceptionClass, status.ToString().data());
    } else if (status.IsNotFound()) {
        jclass notFoundExceptionClass = env->FindClass("com/github/hf/leveldb/exception/LevelDBNotFoundException");

        env->ThrowNew(notFoundExceptionClass, status.ToString().data());
    } else {
        jclass exceptionClass = env->FindClass("com/github/hf/leveldb/exception/LevelDBException");

        env->ThrowNew(exceptionClass, status.ToString().data());
    }
}

#endif //LEVELDB_ANDROID_LEVELDBHELPER_H
