package me.alllex.parsus.tree

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.parser.ParsingScope


/**
 * A syntax-tree consisting of [nodes][Node] and leaves represented as [lexemes][Lexeme].
 *
 * Nodes and lexemes can be combined into larger trees using [SyntaxTree.plus][plus]-operator.
 *
 * ```kotlin
 * val p1 by parser { lexeme(t1) }
 * val p2 by parser { lexeme(t2) + lexeme(t3) }
 * val p3 by parser { p1() + p2() }
 * ```
 */
sealed class SyntaxTree

/**
 * A node in a tree has multiple [children].
 */
data class Node(
    val children: List<SyntaxTree>
) : SyntaxTree() {
    constructor(vararg children: SyntaxTree) : this(children.toList())
}

/**
 * A leaf-element of the [SyntaxTree].
 */
data class Lexeme(
    val match: TokenMatch,
    val text: String
) : SyntaxTree()

/**
 * Parses [token] and returns corresponding lexeme.
 */
suspend fun ParsingScope.lexeme(token: Token): Lexeme {
    val match = token()
    return Lexeme(match, match.text)
}

/**
 * Returns a tree that combines [this] and [other] tree on the same node-level.
 */
@JvmName("plus")
operator fun SyntaxTree.plus(other: SyntaxTree): SyntaxTree {
    return when (this) {
        is Lexeme -> Node(this, other)
        is Node -> Node(this.children + other)
    }
}

@JvmName("plusNullable")
operator fun SyntaxTree.plus(other: SyntaxTree?): SyntaxTree {
    return if (other == null) this else this + other
}

@JvmName("nullablePlus")
operator fun SyntaxTree?.plus(other: SyntaxTree): SyntaxTree {
    return if (this == null) other else this + other
}

@JvmName("nullablePlusNullable")
operator fun SyntaxTree?.plus(other: SyntaxTree?): SyntaxTree? {
    return when {
        this == null -> other
        other == null -> this
        else -> this + other
    }
}
