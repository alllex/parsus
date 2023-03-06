@file:Suppress("MemberVisibilityCanBePrivate", "PackageDirectoryMismatch")

package me.alllex.parsus.demo.arithmetic

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import kotlin.math.pow

sealed class Expr {
    data class Con(val value: Int) : Expr()
    data class Neg(val expr: Expr) : Expr()
    data class Pow(val left: Expr, val right: Expr) : Expr()
    data class Mul(val left: Expr, val right: Expr) : Expr()
    data class Div(val left: Expr, val right: Expr) : Expr()
    data class Add(val left: Expr, val right: Expr) : Expr()
    data class Sub(val left: Expr, val right: Expr) : Expr()
}

abstract class AbstractArithmeticGrammar<T> : Grammar<T>() {
    init { register(regexToken("\\s+", ignored = true)) }
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val pow by literalToken("^")
    val times by literalToken("*")
    val div by literalToken("/")
    val plus by literalToken("+")
    val minus by literalToken("-")
    val int by regexToken("\\d+")

    val number by parser { int() } map { it.text.toInt() }
    val braced by parser { -lpar * expr() * -rpar }

    abstract val expr: Parser<T>
    override val root by parser { expr() }
}

object ExprParser : AbstractArithmeticGrammar<Expr>() {

    val const by number map { Expr.Con(it) }

    val term by parser {
        val neg = +minus
        val v = choose(const, braced)
        if (neg) Expr.Neg(v) else v
    }

    val powExpr by parser {
        reduce(term, pow) { l, _, r -> Expr.Pow(l, r) }
    }

    val mulExpr by parser {
        reduce(powExpr, times or div) { l, o, r ->
            if (o.token == times) Expr.Mul(l, r) else Expr.Div(l, r)
        }
    }

    val addExpr by parser {
        reduce(mulExpr, plus or minus) { l, o, r ->
            if (o.token == plus) Expr.Add(l, r) else Expr.Sub(l, r)
        }
    }

    override val expr by addExpr
}

object ExprCalculator : AbstractArithmeticGrammar<Int>() {

    val const by number

    val term by parser {
        val neg = +minus
        val v = choose(const, braced)
        if (neg) -v else v
    }

    val powExpr by parser {
        reduce(term, pow) { l, _, r ->
            l.toDouble().pow(r.toDouble()).toInt()
        }
    }

    val mulExpr by parser {
        reduce(powExpr, times or div) { l, o, r ->
            if (o.token == times) l * r else l / r
        }
    }

    val addExpr by parser {
        reduce(mulExpr, plus or minus) { l, o, r ->
            if (o.token == plus) l + r else l - r
        }
    }

    override val expr by addExpr
}

fun main() {
    val exprs = listOf(
        "1",
        "-1 + 2 * 3 + 4 ^ 5",
        buildString {
            repeat(1000) { append("(") }
            append("1")
            repeat(1000) { append(")") }
        }
    )

    for (expr in exprs) {
        println("Parsing expr: $expr")
        println()
        val parsedExpr = ExprParser.parseEntireOrThrow(expr)
        println("Parsed tree:")
        println(parsedExpr)
        println()
        val computedValue = ExprCalculator.parseEntireOrThrow(expr)
        println("Computed value:")
        println(computedValue)
        println()
    }
}
