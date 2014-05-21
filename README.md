# LevelDB for Android

This is a Java wrapper for the amazing
[LevelDB](https://code.google.com/p/leveldb/) by Google.

LevelDB supports only ARM and X86 ABIs. MIPS is not supported. (Someone
implement [port/atomic_pointer.h]
(https://code.google.com/p/leveldb/source/browse/port/atomic_pointer.h) for
MIPS.)

Currently it does not use [Snappy](https://code.google.com/p/snappy/) for data
compression. (There is really no need for this in Android, i.e. it's unnecessary
overhead.)

Only the basic API is supported.

LevelDB's native log output is tagged: `org.leveldb:N`

## Example

```java
LevelDB levelDB = new LevelDB("path/to/leveldb", true);

levelDB.put("leveldb", "Is awesome!");
String value = levelDB.get("leveldb");

leveldb.put("magic", new byte[] { 0, 1, 2, 3, 4 });
byte[] magic = levelDB.getBytes("magic");

levelDB.close(); // closing is a must!
```

... or using WriteBatches.

```java
LevelDB levelDB = new LevelDB("path/to/leveldb", true);

levelDB.put("sql", "is lovely!");

levelDB.write(new WriteBatch()
  .put("leveldb", "Is awesome!")
  .put("magic", new byte[] { 0, 1, 2, 3, 4 })
  .del("sql"));

levelDB.close(); // closing is a must!

```

## Building

Until Google (or someone else) fixes the Android Gradle build tools to properly
support NDK, this is the way to build this project.

1. Install the [NDK](https://developer.android.com/tools/sdk/ndk/index.html)
. (Tested with ndk-r9d.)
2. Add NDK to `$PATH`.
3. Run `prebuild` from `leveldb/src/main/jni-prebuild`
4. Use with Gradle

## TODO

* ~~WriteBatch API~~
* Publish on Maven
* Iteration API
* Compaction API
* Snapshot API
* Greater configurability
* Snappy compression
* Support Table API
* `LevelDB#close()` in `Object#finalize()` (probably a very bad idea)
