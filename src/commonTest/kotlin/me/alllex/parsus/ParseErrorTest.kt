package me.alllex.parsus

import assertk.assertions.isEqualTo
import assertk.assertions.prop
import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.ParseError
import me.alllex.parsus.parser.map
import me.alllex.parsus.parser.times
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import kotlin.test.Test

class ParseErrorTest {

    @Test
    fun unmatchedTokenErrorsProvideUserFriendlyDescriptions() {
        object : Grammar<String>() {
            val ab by literalToken("ab")
            val cd by literalToken("cd")
            override val root by ab * cd map { (v1, v2) -> "${v1.text}-${v2.text}" }
        }.run {

            assertParsed("abcd").isEqualTo("ab-cd")

            assertNotParsed("abab").prop(ParseError::describe).isEqualTo(
                "Unmatched token at offset=2, when expected: LiteralToken('cd')\n" + """
                      Expected token: LiteralToken('cd')
                      | offset=2 (or after ignored tokens)
                    abab
                    ^^ Previous token: LiteralToken('ab') at offset=0
                """.trimIndent() + "\n"
            )

            assertNotParsed("cd").prop(ParseError::describe).isEqualTo(
                "Unmatched token at offset=0, when expected: LiteralToken('ab')\n" + """
                    Expected token: LiteralToken('ab')
                    | offset=0 (or after ignored tokens)
                    cd
                """.trimIndent() + "\n"
            )

            assertNotParsed("abcdab").prop(ParseError::describe).isEqualTo(
                "Unmatched token at offset=4, when expected: Token(EOF)\n" + """
                      Expected token: Token(EOF)
                      | offset=4 (or after ignored tokens)
                    cdab
                    ^^ Previous token: LiteralToken('cd') at offset=2
                """.trimIndent() + "\n"
            )
        }
    }

    @Test
    fun lastMatchDescriptionIsPresentWhenThereAreIgnoredTokensInBetween() {
        object : Grammar<String>() {
            val ws by literalToken(" ", ignored = true)
            val ab by literalToken("ab")
            val cd by literalToken("cd")
            override val root by ab * cd map { (v1, v2) -> "${v1.text}-${v2.text}" }
        }.run {
            assertParsed("ab cd").isEqualTo("ab-cd")

            assertNotParsed("ab ab").prop(ParseError::describe).isEqualTo(
                "Unmatched token at offset=2, when expected: LiteralToken('cd')\n" + """
                      Expected token: LiteralToken('cd')
                      | offset=2 (or after ignored tokens)
                    ab␣ab
                    ^^ Previous token: LiteralToken('ab') at offset=0
                """.trimIndent() + "\n"
            )
        }
    }

    @Test
    fun unprintableCharactersAreReplacedInErrors() {
        object : Grammar<String>() {
            val ws by regexToken("\\s+")
            val ab by literalToken("ab")
            @Suppress("unused")
            val cd by literalToken("cd")
            override val root by ws * ab map { (v1, v2) -> "${v1.text}-${v2.text}" }
        }.run {
            assertParsed(" \t\r\nab").isEqualTo(" \t\r\n-ab")

            assertNotParsed(" \t\r\ncd").prop(ParseError::describe).isEqualTo(
                "Unmatched token at offset=4, when expected: LiteralToken('ab')\n" + """
                        Expected token: LiteralToken('ab')
                        | offset=4 (or after ignored tokens)
                    ␣␉␍␤cd
                    ^^^^ Previous token: RegexToken(ws [\s+]) at offset=0
                """.trimIndent() + "\n"
            )
        }

    }

}
