package me.alllex.parsus.bench

import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.token
import me.alllex.parsus.parser.*


object FasterJsonGrammar : Grammar<Json>() {

    @Suppress("unused")
    private val whiteSpace by token(ignored = true, firstChars = " \n\t") { it, at ->
        var index = at
        val length = it.length
        while (index < length && it[index].isWhitespace()) { index++ }
        index - at
    }

    private val comma by literalToken(",")
    private val colon by literalToken(":")
    private val lbrace by literalToken("{")
    private val rbrace by literalToken("}")
    private val lbracket by literalToken("[")
    private val rbracket by literalToken("]")
    private val nullToken by literalToken("null")
    private val trueToken by literalToken("true")
    private val falseToken by literalToken("false")

    private val numToken by token(firstChars = "+-0123456789.") { it, at ->
        var index = at
        val maybeSign = it[index]
        val sign = maybeSign == '+' || maybeSign == '-'
        if (sign) index++

        val length = it.length
        while (index < length && it[index].isDigit()) { index++ }

        if (index < length && it[index] == '.') { // decimal
            index++
            while (index < length && it[index].isDigit()) { index++ }
        }
        if (index == at || (index == at + 1 && sign)) return@token 0
        index - at
    }

    private val stringToken by token(firstChars = "\"") { it, at ->
        var index = at
        if (it[index++] != '"') return@token 0
        val length = it.length
        while (index < length && it[index] != '"') {
            if (it[index] == '\\') { // quote
                index++
            }
            index++
        }
        if (index == length) return@token 0 // unclosed string
        index + 1 - at
    }

    private val string by stringToken map { it.text }
    private val jsonNull by nullToken map Json.Null
    private val jsonBool by trueToken or falseToken map { Json.Bool(it.token == trueToken) }
    private val jsonNum by numToken map { Json.Num(it.text.toDouble()) }
    private val jsonStr by string map { Json.Str(it) }

    private val kv = parser { string() * -colon to jsonValue() }
    private val jsonObj by parser { -lbrace * (separated(kv, comma)) * -rbrace } map { Json.Obj(it.toMap()) }

    private val jsonArr by parser { -lbracket * separated(jsonValue, comma) * -rbracket } map { Json.Arr(it) }
    private val jsonValue: Parser<Json> by jsonNull or jsonBool or jsonNum or jsonStr or jsonArr or jsonObj
    override val root by jsonValue
}

fun main() {
    val input = """
        {
            "v1": true,
            "v2": null,
            "v3": 42.0,
            "v4": "wow",
            "v5": { "such": ["json"] }
        }
    """.trimIndent()
    val json = FasterJsonGrammar.parseEntireOrThrow(input)
    printJson(json)
}

