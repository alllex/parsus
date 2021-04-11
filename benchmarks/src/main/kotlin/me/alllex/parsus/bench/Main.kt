package me.alllex.parsus.bench

import kotlin.system.measureTimeMillis


fun main() {
    val repeat = 10 * 1000
    runRepeatedly(repeat)
}

fun runRepeatedly(repeat: Int) {
    var s = 0 // consuming the results of each execution
    val elapsed = measureTimeMillis {
        repeat(repeat) {
            val t = FasterJsonGrammar().parseToEnd(jsonSample1K)
            s += t.hashCode()
        }
    }
    println(s)
    println("Time: ${elapsed / 1000}s")
}
