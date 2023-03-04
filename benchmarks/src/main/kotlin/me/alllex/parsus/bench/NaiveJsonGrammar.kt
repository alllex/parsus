package me.alllex.parsus.bench

import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import me.alllex.parsus.parser.*


object NaiveJsonGrammar : Grammar<Json>() {
    init { register(regexToken("\\s+", ignored = true)) }
    private val comma by literalToken(",")
    private val colon by literalToken(":")
    private val lbrace by literalToken("{")
    private val rbrace by literalToken("}")
    private val lbracket by literalToken("[")
    private val rbracket by literalToken("]")
    private val nullToken by literalToken("null")
    private val trueToken by literalToken("true")
    private val falseToken by literalToken("false")
    private val numToken by regexToken("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
    private val stringToken by regexToken("\"[^\\\\\"]*(\\\\[\"nrtbf\\\\][^\\\\\"]*)*\"")
    private val string by stringToken map { it.text }
    private val jsonNull by nullToken map Json.Null
    private val jsonBool by trueToken or falseToken map { Json.Bool(it.token == trueToken) }
    private val jsonNum by numToken map { Json.Num(it.text.toDouble()) }
    private val jsonStr by string map { Json.Str(it) }

    private val jsonObj by parser {
        val kv = parser { string() * -colon to jsonValue() }
        -lbrace * (separated(kv, comma)) * -rbrace
    } map { Json.Obj(it.toMap()) }

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
    val json = NaiveJsonGrammar.parseEntireOrThrow(input)
    printJson(json)
}

