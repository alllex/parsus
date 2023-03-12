package me.alllex.parsus.bench

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.serialization.json.Json
import me.alllex.parsus.bench.JsonSamples.jsonSample1K

@Suppress("unused", "JSON_FORMAT_REDUNDANT_DEFAULT")
@State(Scope.Benchmark)
class JsonGrammarBenchmark {

    @Benchmark
    fun jsonNaiveParser(bh: Blackhole) {
        bh.consume(NaiveJsonGrammar.parseEntireOrThrow(jsonSample1K))
    }

    @Benchmark
    fun jsonFasterParser(bh: Blackhole) {
        bh.consume(FasterJsonGrammar.parseEntireOrThrow(jsonSample1K))
    }

    @Benchmark
    fun jsonKotlinxDeserializer(bh: Blackhole) {
        bh.consume(Json {}.parseToJsonElement(jsonSample1K))
    }
}
