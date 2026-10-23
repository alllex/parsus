package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.*
import kotlin.test.Test

class NonAdvancingRepetitionTest {

    @Test
    fun unboundedRepeatRejectsParserThatConsumesNothing() {
        val empty = parser { 1 }
        val grammar = object : Grammar<List<Int>>() {
            override val root = parser { repeatZeroOrMore(empty) }
        }

        assertThat(grammar.parse("")).isEqualTo(NonAdvancingRepetition(0))
    }

    @Test
    fun boundedRepeatMayUseParserThatConsumesNothing() {
        val empty = parser { 1 }
        val grammar = object : Grammar<List<Int>>() {
            override val root = parser { repeat(empty, atLeast = 2, atMost = 2) }
        }

        assertThat(grammar.parse("")).isEqualTo(ParsedValue(listOf(1, 1)))
    }

    @Test
    fun leftAssociativeReductionRejectsZeroProgressLoop() {
        val empty = parser { 1 }
        val grammar = object : Grammar<Int>() {
            override val root = parser { reduce(empty, empty) { left, _, right -> left + right } }
        }

        assertThat(grammar.parse("")).isEqualTo(NonAdvancingRepetition(0))
    }

    @Test
    fun rightAssociativeReductionRejectsZeroProgressLoop() {
        val empty = parser { 1 }
        val grammar = object : Grammar<Int>() {
            override val root = parser { reduceRight(empty, empty) { left, _, right -> left + right } }
        }

        assertThat(grammar.parse("")).isEqualTo(NonAdvancingRepetition(0))
    }
}
