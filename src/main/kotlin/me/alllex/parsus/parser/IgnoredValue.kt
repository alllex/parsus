package me.alllex.parsus.parser

/**
 * A special marker to denote parsed value that is ignored.
 *
 * It can be used to write well-readable parsers that include ignored parts of the input.
 * ```kotlin
 * val lpar by literalToken("(")
 * val rpar by literalToken(")")
 * val braced by parser { -lpar * expr() * -rpar }
 * ```
 */
object IgnoredValue

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
inline operator fun IgnoredValue.times(dropped: IgnoredValue): IgnoredValue = this

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> IgnoredValue.times(value: T): T = value

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
inline operator fun <T> T.times(dropped: IgnoredValue): T = this
