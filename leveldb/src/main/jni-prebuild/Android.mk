LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

$(shell TARGET_OS=OS_ANDROID_CROSSCOMPILE $(LOCAL_PATH)/leveldb/build_detect_platform $(LOCAL_PATH)/common.mk $(LOCAL_PATH)/leveldb)

include $(LOCAL_PATH)/common.mk

LEVELDB_SOURCES := $(foreach source, $(SOURCES), leveldb/$(source))

LOCAL_MODULE := leveldb
LOCAL_CXXFLAGS += $(PLATFORM_CXXFLAGS)

LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/leveldb $(LOCAL_PATH)/leveldb/include
LOCAL_SRC_FILES := $(LEVELDB_SOURCES) com_github_hf_leveldb_implementation_NativeLevelDB.cc com_github_hf_leveldb_implementation_NativeWriteBatch.cc com_github_hf_leveldb_implementation_NativeIterator.cc
LOCAL_LDLIBS += -llog -ldl

include $(BUILD_SHARED_LIBRARY)
