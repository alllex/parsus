package me.alllex.parsus.bench

import kotlin.test.Test
import kotlin.test.assertEquals


class Tests {

    private val smallInput = JsonSamples.jsonSampleSmall
    private val largeInput = JsonSamples.jsonSample1K

    @Test
    fun naiveJsonGrammarSmallInput() {
        val json = NaiveJsonGrammar.parseOrThrow(smallInput)
        val jsonString = jsonToString(json)
        assertEquals(smallInput, jsonString)
    }

    @Test
    fun naiveJsonGrammarLargeInput() {
        val json = NaiveJsonGrammar.parseOrThrow(largeInput)
        val jsonString = jsonToString(json)
        assertEquals(largeInput, jsonString)
    }

    @Test
    fun fasterJsonGrammarSmallInput() {
        val json = FasterJsonGrammar.parseOrThrow(smallInput)
        val jsonString = jsonToString(json)
        assertEquals(smallInput, jsonString)
    }

    @Test
    fun fasterJsonGrammarLargeInput() {
        val json = FasterJsonGrammar.parseOrThrow(largeInput)
        val jsonString = jsonToString(json)
        assertEquals(largeInput, jsonString)
    }
}
