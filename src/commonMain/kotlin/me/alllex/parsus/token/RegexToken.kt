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

private fun Regex.withIgnoreCase(ignoreCase: Boolean) =
    if (!ignoreCase || RegexOption.IGNORE_CASE in options) this
    else Regex(pattern, options + RegexOption.IGNORE_CASE)

// TODO: Add a @Language annotation to automatically highlight the pattern as a regex in the IDE
//  see: https://github.com/kotest/kotest/pull/3397
/**
 * Creates and registers a regex token in this grammar.
 *
 * This token defined by a regular expression [pattern] that is expected to match the input.
 */
fun Grammar<*>.regexToken(
    @Language("RegExp", "", "")
    pattern: String,
    name: String? = null,
    ignored: Boolean = false,
    ignoreCase: Boolean = this.ignoreCase,
): RegexToken = regexToken(Regex(pattern), name, ignored, ignoreCase)

/**
 * Creates and registers a regex token in this grammar.
 *
 * This token defined by a [regular expression][regex] that is expected to match the input.
 */
fun Grammar<*>.regexToken(
    regex: Regex,
    name: String? = null,
    ignored: Boolean = false,
    ignoreCase: Boolean = this.ignoreCase,
): RegexToken = RegexToken(regex.withIgnoreCase(ignoreCase), name, ignored).also { register(it) }


