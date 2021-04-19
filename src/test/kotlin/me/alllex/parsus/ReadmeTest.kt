package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.parser
import me.alllex.parsus.token.literalToken
import org.junit.Test

class ReadmeTest {

    @Test
    fun `Simplest grammar`() {
        val g1 = object : Grammar<String>() {
            val tokenA by literalToken("a")
            override val root by parser { tokenA().text }
        }

        assertThat(g1.parseToEnd("a")).isEqualTo("a")
    }
}
