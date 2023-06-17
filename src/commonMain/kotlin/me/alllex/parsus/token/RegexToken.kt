package me.alllex.parsus.token

import me.alllex.parsus.parser.Grammar
import org.intellij.lang.annotations.Language

/**
 * A token that [matches] the input using a [regex].
 *
 * @see Grammar.regexToken
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

    override fun toString(): String = "RegexToken(${name ?: ""} [$pattern]${if (ignored) " [ignored]" else ""})"
}

/**
 * Creates and registers a regex token in this grammar.
 *
 * This token defined by a regular expression [pattern] that is expected to match the input.
 */
fun Grammar<*>.regexToken(
    @Language("RegExp", "", "")
    pattern: String,
    name: String? = null,
    ignored: Boolean = false
): RegexToken = RegexToken(Regex(pattern), name, ignored).also { register(it) }

/**
 * Creates and registers a regex token in this grammar.
 *
 * This token defined by a [regular expression][regex] that is expected to match the input.
 */
fun Grammar<*>.regexToken(
    regex: Regex,
    name: String? = null,
    ignored: Boolean = false
): RegexToken = RegexToken(regex, name, ignored).also { register(it) }
