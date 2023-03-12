@file:Suppress("UnusedReceiverParameter")

package me.alllex.parsus.token

import me.alllex.parsus.parser.GrammarContext

/**
 * A token that [matches] the input using a [regex].
 */
class RegexToken(
    private val regex: Regex,
    name: String? = null,
    ignored: Boolean = false,
) : Token(name, ignored) {

    private val pattern: String get() = regex.pattern

    override fun match(input: CharSequence, fromIndex: Int): Int {
        // TODO: consider optimizing this on JVM by reusing a matcher
        val match = regex.matchAt(input, fromIndex) ?: return 0
        return match.value.length
    }

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}

fun GrammarContext.regexToken(
    pattern: String,
    name: String? = null,
    ignored: Boolean = false
): RegexToken = RegexToken(Regex(pattern), name, ignored)

fun GrammarContext.regexToken(
    regex: Regex,
    name: String? = null,
    ignored: Boolean = false
): RegexToken = RegexToken(regex, name, ignored)
