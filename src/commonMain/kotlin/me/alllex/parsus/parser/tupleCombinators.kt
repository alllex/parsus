@file:Suppress("UNCHECKED_CAST")

package me.alllex.parsus.parser

import kotlin.jvm.JvmName


/**
 * Creates a parser from a pair of parsers, returning a pair of their results.
 */
@JvmName("parserAndParser")
infix fun <A, B> Parser<A>.and(p: Parser<B>): Parser<Tuple2<A, B>> =
    retuple(this, p) {
        Tuple2(it[0] as A, it[1] as B)
    }

@JvmName("parserAndIgnored")
infix fun <A> Parser<A>.and(p: Parser<Unit>): Parser<A> =
    retuple(this, ignored(p)) {
        it[0] as A
    }

@JvmName("ignoredAndParser")
infix fun <B> Parser<Unit>.and(p: Parser<B>): Parser<B> =
    retuple(ignored(this), p) {
        it[0] as B
    }

@JvmName("ignoredAndIgnored")
infix fun Parser<Unit>.and(p: Parser<Unit>): Parser<Unit> =
    retuple(ignored(this), ignored(p)) {}

@JvmName("tuple2AndParser")
infix fun <T1, T2, T3> Parser<Tuple2<T1, T2>>.and(p: Parser<T3>): Parser<Tuple3<T1, T2, T3>> =
    retuple(this, p) {
        Tuple3(it[0] as T1, it[1] as T2, it[2] as T3)
    }

@JvmName("tuple2AndIgnored")
infix fun <T1, T2> Parser<Tuple2<T1, T2>>.and(p: Parser<Unit>): Parser<Tuple2<T1, T2>> =
    retuple(this, ignored(p)) {
        Tuple2(it[0] as T1, it[1] as T2)
    }

@JvmName("tuple3AndParser")
infix fun <T1, T2, T3, T4> Parser<Tuple3<T1, T2, T3>>.and(p: Parser<T4>): Parser<Tuple4<T1, T2, T3, T4>> =
    retuple(this, p) {
        Tuple4(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4)
    }

@JvmName("tuple3AndIgnored")
infix fun <T1, T2, T3> Parser<Tuple3<T1, T2, T3>>.and(p: Parser<Unit>): Parser<Tuple3<T1, T2, T3>> =
    retuple(this, ignored(p)) {
        Tuple3(it[0] as T1, it[1] as T2, it[2] as T3)
    }

@JvmName("tuple4AndParser")
infix fun <T1, T2, T3, T4, T5> Parser<Tuple4<T1, T2, T3, T4>>.and(p: Parser<T5>): Parser<Tuple5<T1, T2, T3, T4, T5>> =
    retuple(this, p) {
        Tuple5(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5)
    }

@JvmName("tuple4AndIgnored")
infix fun <T1, T2, T3, T4> Parser<Tuple4<T1, T2, T3, T4>>.and(p: Parser<Unit>): Parser<Tuple4<T1, T2, T3, T4>> =
    retuple(this, ignored(p)) {
        Tuple4(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4)
    }

@JvmName("tuple5AndParser")
infix fun <T1, T2, T3, T4, T5, T6> Parser<Tuple5<T1, T2, T3, T4, T5>>.and(p: Parser<T6>): Parser<Tuple6<T1, T2, T3, T4, T5, T6>> =
    retuple(this, p) {
        Tuple6(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6)
    }

@JvmName("tuple5AndIgnored")
infix fun <T1, T2, T3, T4, T5> Parser<Tuple5<T1, T2, T3, T4, T5>>.and(p: Parser<Unit>): Parser<Tuple5<T1, T2, T3, T4, T5>> =
    retuple(this, ignored(p)) {
        Tuple5(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5)
    }

@JvmName("tuple6AndParser")
infix fun <T1, T2, T3, T4, T5, T6, T7> Parser<Tuple6<T1, T2, T3, T4, T5, T6>>.and(p: Parser<T7>): Parser<Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
    retuple(this, p) {
        Tuple7(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7)
    }

@JvmName("tuple6AndIgnored")
infix fun <T1, T2, T3, T4, T5, T6> Parser<Tuple6<T1, T2, T3, T4, T5, T6>>.and(p: Parser<Unit>): Parser<Tuple6<T1, T2, T3, T4, T5, T6>> =
    retuple(this, ignored(p)) {
        Tuple6(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6)
    }

@Suppress("DeprecatedCallableAddReplaceWith", "UNUSED_PARAMETER")
@Deprecated("Further chaining with `and` is unsupported", level = DeprecationLevel.ERROR)
@JvmName("tuple7AndParser")
infix fun <T1, T2, T3, T4, T5, T6, T7, T8> Parser<Tuple7<T1, T2, T3, T4, T5, T6, T7>>.and(
    unsupported: Parser<T8>
): Nothing = error("Further chaining with `and` is unsupported")

@JvmName("tuple7AndIgnored")
infix fun <T1, T2, T3, T4, T5, T6, T7> Parser<Tuple7<T1, T2, T3, T4, T5, T6, T7>>.and(p: Parser<Unit>): Parser<Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
    retuple(this, ignored(p)) {
        Tuple7(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7)
    }
