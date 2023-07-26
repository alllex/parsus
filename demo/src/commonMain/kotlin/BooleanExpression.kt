@file:Suppress("MemberVisibilityCanBePrivate", "PackageDirectoryMismatch")

package me.alllex.parsus.demo.bool

import me.alllex.parsus.demo.bool.BooleanExpression.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

sealed class BooleanExpression {
    object TRUE : BooleanExpression()
    object FALSE : BooleanExpression()
    data class Var(val name: String) : BooleanExpression()
    data class Not(val body: BooleanExpression) : BooleanExpression()
    data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
    data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
    data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
}

object BooleanGrammar : Grammar<BooleanExpression>() {
    init { regexToken("\\s+", ignored = true) }
    val tru by literalToken("true")
    val fal by literalToken("false")
    val id by regexToken("\\w+")
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val not by literalToken("!")
    val and by literalToken("&")
    val or by literalToken("|")
    val impl by literalToken("->")

    val negation by -not * ref(::term) map { Not(it) }
    val braced by -lpar * ref(::expr) * -rpar

    val term: Parser<BooleanExpression> by
        (tru map TRUE) or (fal map FALSE) or (id map { Var(it.text) }) or negation or braced

    val andChain by leftAssociative(term, and, ::And)
    val orChain by leftAssociative(andChain, or, ::Or)
    val implChain by rightAssociative(orChain, impl, ::Impl)

    val expr by implChain
    override val root by expr
}

fun main() {
    val expr = "a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"
    println(BooleanGrammar.parseOrThrow(expr))
}
