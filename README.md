# Gradle API evolution demo

This is a playground repo to test out ideas around allowing code compiled against an older version of Gradle to run with a newer version of Gradle that have breaking changes.

## Structure

There are the following subprojects:

- `old-api` defines an old version of the `Server` type written in Java,
- `new-api` defines the new version of the `Server` type, also written in Java,
- `old-client` contains client code compiled against the old version of `Server`; there are clients written in Java, Kotlin and static and dynamic Groovy,
- `app` has the upgrade logic and a test that tries to run each of the clients in `old-client` against the `new-api`.

Try with:

```shell
$ ./gradlew check
```

## Property upgrade bytecode

- for Java/Kotlin/static Groovy: https://gist.github.com/lptr/36105c5dde2bbecbebf9ce802c4fbcbd
- for dynamic Groovy: https://gist.github.com/donat/6fccdc21aeca7f1267f0e7920c170065
