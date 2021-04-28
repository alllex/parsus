package me.alllex.parsus.pattern

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

@Suppress("RemoveRedundantBackticks")
class PatternTest {

    @Test
    fun `Single literal`() {
        assertThat(
            PatternGrammar.parseToEnd("a")
        ).isEqualTo(
            AlternativePattern(SequencePattern(LiteralPattern('a')))
        )
    }

    @Test
    fun `Escaped char`() {
        assertThat(
            PatternGrammar.parseToEnd("\\)")
        ).isEqualTo(
            AlternativePattern(SequencePattern(LiteralPattern(')')))
        )

        assertThat(
            PatternGrammar.parseToEnd("\\\\")
        ).isEqualTo(
            AlternativePattern(SequencePattern(LiteralPattern('\\')))
        )
    }

    @Test
    fun `Wildcard`() {
        assertThat(
            PatternGrammar.parseToEnd(".")
        ).isEqualTo(
            AlternativePattern(SequencePattern(WildcardPattern))
        )
    }

    @Test
    fun `Range`() {
        assertThat(
            PatternGrammar.parseToEnd("[a]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(RangePattern('a'..'a')))
        )

        assertThat(
            PatternGrammar.parseToEnd("[a-b]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(RangePattern('a'..'b')))
        )

        assertThat(
            PatternGrammar.parseToEnd("[_a-b]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(RangePattern('_'..'_', 'a'..'b')))
        )

        assertThat(
            PatternGrammar.parseToEnd("[a-bz]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(RangePattern('a'..'b', 'z'..'z')))
        )

        assertThat(
            PatternGrammar.parseToEnd("[A-Ba-b]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(RangePattern('A'..'B', 'a'..'b')))
        )

        assertThat(
            PatternGrammar.parseToEnd("[\\^]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(RangePattern('^'..'^')))
        )
    }

    @Test
    fun `Sequences`() {
        assertThat(
            PatternGrammar.parseToEnd("ab")
        ).isEqualTo(
            AlternativePattern(SequencePattern(LiteralPattern('a'), LiteralPattern('b')))
        )

        assertThat(
            PatternGrammar.parseToEnd("\\(\\)")
        ).isEqualTo(
            AlternativePattern(SequencePattern(LiteralPattern('('), LiteralPattern(')')))
        )

        assertThat(
            PatternGrammar.parseToEnd(".[a-b]")
        ).isEqualTo(
            AlternativePattern(SequencePattern(LiteralPattern('('), LiteralPattern(')')))
        )
    }
}
