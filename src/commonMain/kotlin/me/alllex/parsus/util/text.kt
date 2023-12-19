package me.alllex.parsus.util

internal fun Any.toPrintableString() = replaceNonPrintable(toString())

internal fun replaceNonPrintable(string: String): String {
    return buildString {
        for (char in string) {
            append(replaceNonPrintable(char))
        }
    }
}

internal fun replaceNonPrintable(char: Char): Char {
    return when (char) {
        ' ' -> '␣' // U+2423 OPEN BOX
        '\n' -> '␤' // U+2424 SYMBOL FOR NEWLINE
        '\r' -> '␍' // U+240D SYMBOL FOR CARRIAGE RETURN
        '\t' -> '␉' // U+2409 SYMBOL FOR HORIZONTAL TABULATION
        else -> char
    }
}
