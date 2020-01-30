cmake_minimum_required(VERSION 3.9)

set(levelDB_src_DIR "src/main/cpp/leveldb")

set(levelDB_build_DIR "${PROJECT_SOURCE_DIR}/src/main/cpp/leveldb/out/${ANDROID_ABI}")
file(MAKE_DIRECTORY ${levelDB_build_DIR})

file(GLOB list src/main/cpp/jni/*.cc)

add_subdirectory(
        ${levelDB_src_DIR}
        ${levelDB_build_DIR}
)

add_library( leveldb-lib STATIC IMPORTED )

set_target_properties( leveldb-lib
        PROPERTIES IMPORTED_LOCATION
        ${levelDB_build_DIR}/libleveldb.a )

add_library(leveldb-android SHARED
    ${list}
)

include_directories( ${levelDB_src_DIR}/include )
include_directories( ${levelDB_build_DIR}/include )
include_directories(src/main/cpp)

target_link_libraries(leveldb-android
        leveldb-lib
        android
        log)