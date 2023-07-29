package me.alllex.parsus

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import me.alllex.parsus.parser.*
import me.alllex.parsus.parser.SkipParser
import me.alllex.parsus.token.LiteralToken
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.token.literalToken
import kotlin.reflect.KClass
import kotlin.test.Test

@Suppress("RemoveExplicitTypeArguments")
class TupleCombinatorsTest {

    private val tupleParserClass = TupleParser::class
    private val skipParserClass = SkipParser::class
    private val literalTokenClass = LiteralToken::class

    private val ga = object : Grammar<TokenMatch>() {
        val a by literalToken("a")
        override val root by parser { a() }
    }

    @Test
    fun test1() {
        val a = LiteralToken("a")

        check(
            parser = a and a,
            arity = 2,
            parserCount = 2,
            parserClasses = listOf(literalTokenClass, literalTokenClass),
            parses = "aa",
        )

        check(
            parser = a and a and a,
            arity = 3,
            parserCount = 3,
            parserClasses = listOf(literalTokenClass, literalTokenClass, literalTokenClass),
            parses = "aaa",
        )

        check<TokenMatch>(
            parser = a and ignored(a),
            arity = 1,
            parserCount = 2,
            parserClasses = listOf(literalTokenClass, skipParserClass),
            parses = "a",
        )

        check<TokenMatch>(
            parser = ignored(a) and a,
            arity = 1,
            parserCount = 2,
            parserClasses = listOf(skipParserClass, literalTokenClass),
            parses = "a",
        )

        check<Unit>(
            parser = ignored(a) and ignored(a),
            arity = 0,
            parserCount = 2,
            parserClasses = listOf(skipParserClass, skipParserClass),
            parses = "",
        )

        check<Tuple2<TokenMatch, TokenMatch>>(
            parser = a and a and ignored(a),
            arity = 2,
            parserCount = 3,
            parserClasses = listOf(literalTokenClass, literalTokenClass, skipParserClass),
            parses = "aa",
        )

        check<Tuple3<TokenMatch, TokenMatch, TokenMatch>>(
            parser = a and a and ignored(a) and a,
            arity = 3,
            parserCount = 4,
            parserClasses = listOf(literalTokenClass, literalTokenClass, skipParserClass, literalTokenClass),
            parses = "aaa",
        )

        check<Tuple3<TokenMatch, TokenMatch, Tuple2<TokenMatch, TokenMatch>>>(
            parser = ignored(a) and (a and ignored(a) and a) and (ignored(a) and ignored(a)) and (a and (a and ignored(a))),
            arity = 2,
            parserCount = 4,
            parserClasses = listOf(
                skipParserClass, tupleParserClass, skipParserClass, tupleParserClass
            ),
            parses = "aaaa",
        )
    }

    private fun <T> check(
        parser: Parser<T>,
        arity: Int,
        parserCount: Int,
        parserClasses: List<KClass<*>>,
        parses: String? = null
    ) {
        assertThat(parser).isInstanceOf<TupleParser<*>>().all {
            prop(TupleParser<*>::arity).isEqualTo(arity)
            prop(TupleParser<*>::parsers).all {
                prop(List<*>::size).isEqualTo(parserCount)
                prop("classes") { list -> list.map { it::class } }.isEqualTo(parserClasses)
            }
        }

        if (parses != null) {
            ga.parse(parser, parses)
        }
    }

}
