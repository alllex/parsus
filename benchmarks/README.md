# Parsus Benchmarks

This project contains benchmarks to compare Parsus performance to
`kotlinx.serialization` in the task of parsing a large JSON.
The benchmarks are handled by [kotlinx.benchmark] library.
The benchmarks in this project are JVM-specific and are executed under [JMH].

Run benchmarks with
```
./gradlew :benchmarks:benchmark
```

[kotlinx.benchmark]: https://github.com/Kotlin/kotlinx-benchmark
[JMH]: https://openjdk.java.net/projects/code-tools/jmh/
