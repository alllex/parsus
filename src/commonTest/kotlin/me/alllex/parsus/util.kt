package me.alllex.parsus

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.LiteralToken
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.tree.Lexeme
import me.alllex.parsus.tree.Node
import me.alllex.parsus.tree.SyntaxTree
import me.alllex.parsus.tree.lexeme

fun parlex(token: Token) = parser { lexeme(token) }

fun <T> Grammar<T>.assertParsed(text: String): Assert<T> = assertThat(parseOrThrow(text))

fun <T> Grammar<T>.assertThatParsing(text: String): Assert<ParseResult<T>> = assertThat(parse(text))

fun <T> Grammar<T>.assertNotParsed(text: String): Assert<ParseError> = assertThat(parse(text)).isInstanceOf(ParseError::class)

fun node(vararg literals: LiteralToken, startOffset: Int = 0): Node {
    var offset = startOffset
    val lexemes = mutableListOf<Lexeme>()
    for (literal in literals) {
        val l = literal.lex(offset)
        lexemes += l
        offset += l.match.length
    }

    return Node(lexemes)
}

fun node(vararg children: SyntaxTree) = Node(*children)

fun node(children: List<SyntaxTree>) = Node(children)

fun node(children: Tuple2<SyntaxTree, SyntaxTree>) = Node(children.toList())

fun node(children: Tuple3<SyntaxTree, SyntaxTree, SyntaxTree>) = Node(children.toList())

fun node(children: Tuple4<SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree>) = Node(children.toList())

fun node(children: Tuple5<SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree>) = Node(children.toList())

fun node(children: Tuple6<SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree>) = Node(children.toList())

fun node(children: Tuple7<SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree, SyntaxTree>) = Node(children.toList())

fun LiteralToken.lex(offset: Int = 0): Lexeme {
    return Lexeme(TokenMatch(this, offset, string.length), string)
}

fun Token.lex(text: String, offset: Int = 0): Lexeme {
    return Lexeme(TokenMatch(this, offset, text.length), text)
}

fun <T> Assert<ParseResult<T>>.failedWith(parseError: ParseError) {
    isEqualTo(parseError)
}

fun <T> Assert<ParseResult<T>>.failedWithNotEnoughRepetition(offset: Int, expectedAtLeast: Int, actualCount: Int) {
    isInstanceOf(NotEnoughRepetition::class)
        .all {
            prop(NotEnoughRepetition::offset).isEqualTo(offset)
            prop(NotEnoughRepetition::expectedAtLeast).isEqualTo(expectedAtLeast)
            prop(NotEnoughRepetition::actualCount).isEqualTo(actualCount)
        }
}

fun <T> Assert<ParseResult<T>>.failedWithTokenMismatch(expected: Token, actual: Token, offset: Int) {
    isInstanceOf(MismatchedToken::class)
        .all {
            prop("expected token", MismatchedToken::expected).isEqualTo(expected)
            prop("actual lexeme", MismatchedToken::found).all {
                prop(TokenMatch::token).isEqualTo(actual)
                prop(TokenMatch::offset).isEqualTo(offset)
            }
        }
}
