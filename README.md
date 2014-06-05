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

LevelDB's native log output is tagged: `com.github.hf.leveldb:N`

## Example

### Opening, Closing, Putting, Deleting

```java
LevelDB levelDB =
    new LevelDB("path/to/leveldb", LevelDB.configure().createIfMissing(true));

levelDB.put("leveldb", "Is awesome!");
String value = levelDB.get("leveldb");

leveldb.put("magic", new byte[] { 0, 1, 2, 3, 4 });
byte[] magic = levelDB.getBytes("magic");

levelDB.close(); // closing is a must!
```

### WriteBatch (a.k.a. Transactions)

```java
LevelDB levelDB = new LevelDB("path/to/leveldb"); // createIfMissing == true

levelDB.put("sql", "is lovely!");

levelDB.writeBatch()
  .put("leveldb", "Is awesome!")
  .put("magic", new byte[] { 0, 1, 2, 3, 4 })
  .del("sql")
  .write(levelDB);

levelDB.close(); // closing is a must!

```

### Iteration Over Key-Value Pairs

LevelDB is a key-value store, but it has some nice iteration features.

Every key-value pair inside LevelDB is ordered. Until the comparator wrapper API
is finished you can iterate over your LevelDB in the key's lexicographical order.

```java
LevelDB levelDB = new LevelDB("path/to/leveldb");

Iterator iterator = levelDB.iterator();

for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
  byte[] key = iterator.key();
  byte[] value = iterator.value();
}

iterator.close(); // closing is a must!
```

#### Reverse Iteration

*It is somewhat slower than forward iteration.*

```java
LevelDB levelDB = new LevelDB("path/to/leveldb");

Iterator iterator = levelDB.iterator();

for (iterator.seekToLast(); iterator.isValid(); iterator.previous()) {
  byte[] key = iterator.key();
  byte[] value = iterator.value();
}

iterator.close(); // closing is a must!
```

#### Iterate from a Staring Position

```java
LevelDB levelDB = new LevelDB("path/to/leveldb");

Iterator iterator = levelDB.iterator();

for (iterator.seek("leveldb".getBytes()); iterator.isValid(); iterator.next()) {
  byte[] key = iterator.key();
  byte[] value = iterator.value();
}

iterator.close(); // closing is a must!
```

This will start from the key `leveldb` if it exists, or from the one that
follows (eg. `sql`, i.e. `l` < `s`).

**Yes, the API needs more thought.**

## Building

Until Google (or someone else) fixes the Android Gradle build tools to properly
support NDK, this is the way to build this project.

1. Install the [NDK](https://developer.android.com/tools/sdk/ndk/index.html)
. (Tested with ndk-r9d.)
2. Add NDK to `$PATH`.
3. Build with Gradle (`preBuild` depends on `preBuildLevelDB`)

## TODO

* ~~WriteBatch API~~
* Publish on Maven
* ~~Iteration API~~
* Snapshot API
* Comparator API
* Greater configurability
* Compaction API
* Snappy compression
* Support Table API
* `LevelDB#close()` in `Object#finalize()` (probably a very bad idea)

## License

This wrapper library is licensed under the
[BSD 3-Clause License](http://opensource.org/licenses/BSD-3-Clause),
same as the code from Google.

See `LICENSE.txt` for the full Copyright.
