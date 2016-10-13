![Travis CI](https://travis-ci.org/hf/leveldb-android.svg?branch=master) [![codecov](https://codecov.io/gh/hf/leveldb-android/branch/master/graph/badge.svg)](https://codecov.io/gh/hf/leveldb-android)
# LevelDB for Android

This is a Java wrapper for the amazing
[LevelDB](https://github.com/google/leveldb) by Google.

LevelDB supports only ARM and X86 ABIs. MIPS is not supported. 

Currently it does not use [Snappy](http://google.github.io/snappy/) for data
compression. (There is really no need for this in Android, i.e. it's unnecessary
overhead.)

LevelDB's native log output is tagged: `com.github.hf.leveldb:N`

## Usage

Add this to your build.gradle:

```groovy
repositories {
  maven {
    url "http://dl.bintray.com/stojan/android"
  }
}
```

And then this as a dependency:

```groovy
dependencies {
  compile 'com.github.hf:leveldb:1.7.0@aar'
}
```

## Example

### Opening, Closing, Putting, Deleting

```java
LevelDB levelDB = LevelDB.open("path/to/leveldb", LevelDB.configure().createIfMissing(true));

levelDB.put("leveldb".getBytes(), "Is awesome!".getBytes());
String value = levelDB.get("leveldb".getBytes());

leveldb.put("magic".getBytes(), new byte[] { 0, 1, 2, 3, 4 });
byte[] magic = levelDB.getBytes("magic".getBytes());

levelDB.close(); // closing is a must!
```

### WriteBatch (a.k.a. Transactions)

```java
LevelDB levelDB = LevelDB.open("path/to/leveldb"); // createIfMissing == true

levelDB.put("sql".getBytes(), "is lovely!".getBytes());

levelDB.writeBatch()
  .put("leveldb".getBytes(), "Is awesome!".getBytes())
  .put("magic".getBytes(), new byte[] { 0, 1, 2, 3, 4 })
  .del("sql".getBytes())
  .write(); // commit transaction

levelDB.close(); // closing is a must!

```

### Iteration Over Key-Value Pairs

LevelDB is a key-value store, but it has some nice iteration features.

Every key-value pair inside LevelDB is ordered. Until the comparator wrapper API
is finished you can iterate over your LevelDB in the key's lexicographical order.

```java
LevelDB levelDB = LevelDB.open("path/to/leveldb");

Iterator iterator = levelDB.iterator();

for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
  byte[] key   = iterator.key();
  byte[] value = iterator.value();
}

iterator.close(); // closing is a must!
```

#### Reverse Iteration

*It is somewhat slower than forward iteration.*

```java
LevelDB levelDB = LevelDB.open("path/to/leveldb");

Iterator iterator = levelDB.iterator();

for (iterator.seekToLast(); iterator.isValid(); iterator.previous()) {
  String key   = iterator.key();
  String value = iterator.value();
}

iterator.close(); // closing is a must!
```

#### Iterate from a Starting Position

```java
LevelDB levelDB = LevelDB.open("path/to/leveldb");

Iterator iterator = levelDB.iterator();

for (iterator.seek("leveldb".getBytes()); iterator.isValid(); iterator.next()) {
  String key   = iterator.key();
  String value = iterator.value();
}

iterator.close(); // closing is a must!
```

This will start from the key `leveldb` if it exists, or from the one that
follows (eg. `sql`, i.e. `l` < `s`).

#### Snapshots

Snapshots give you a consistent view of the data in the database at a given time.

Here's a simple example demonstrating their use:

```java
LevelDB levelDB = LevelDB.open("path/to/leveldb");

levelDB.put("hello".getBytes(), "world".getBytes());

Snapshot helloWorld = levelDB.obtainSnapshot();

levelDB.put("hello".getBytes(), "brave-new-world".getBytes());

levelDB.get("hello".getBytes(), helloWorld); // == "world"

levelDB.get("hello".getBytes()); // == "brave-new-world"

levelDB.releaseSnapshot(helloWorld); // release the snapshot

levelDB.close(); // snapshots will automatically be released after this
```

### Mock LevelDB

The implementation also supplies a mock LevelDB implementation that is an in-memory 
equivalent of the native LevelDB. It is meant to be used in testing environments,
especially non-Android ones like Robolectric.

There are a few of differences from the native implementation:

+ it is not configurable
+ it does not support properties (as in `LevelDB#getProperty()`)
+ it does not support paths, i.e. always returns `:MOCK:`

Use it like so:

```java
LevelDB.mock();
```

## Building

Until Google (or someone else) fixes the Android Gradle build tools to properly
support NDK, this is the way to build this project.

1. Install the [NDK](https://developer.android.com/tools/sdk/ndk/index.html)
. (Tested with ndk-r10d.)
2. Add NDK to `$PATH`.
3. Build with Gradle (`preBuild` depends on `preBuildLevelDB`)

## License

This wrapper library is licensed under the
[BSD 3-Clause License](http://opensource.org/licenses/BSD-3-Clause),
same as the code from Google.

See `LICENSE.txt` for the full Copyright.
