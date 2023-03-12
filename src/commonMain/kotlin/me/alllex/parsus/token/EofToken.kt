package me.alllex.parsus.token

/**
 * A special token than matches only when current position
 * exceeds input length.
 */
object EofToken : Token("EOF") {
    override fun match(input: CharSequence, fromIndex: Int): Int {
        return if (fromIndex >= input.length) 1 else 0
    }
}
