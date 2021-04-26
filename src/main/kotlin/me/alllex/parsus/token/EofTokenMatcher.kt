package me.alllex.parsus.token

/**
 * A special token than matches only when current position
 * exceeds input length.
 */
internal object EofTokenMatcher : TokenMatcher {
    override fun match(input: CharSequence, fromIndex: Int): Int {
        return if (fromIndex >= input.length) 1 else 0
    }
}
