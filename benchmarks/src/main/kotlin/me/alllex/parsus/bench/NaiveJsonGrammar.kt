package me.alllex.parsus.bench

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

object NaiveJsonGrammar : Grammar<Json>() {
    init {
        regexToken("\\s+", ignored = true)
    }

    private val comma by literalToken(",")
    private val colon by literalToken(":")
    private val lbrace by literalToken("{")
    private val rbrace by literalToken("}")
    private val lbracket by literalToken("[")
    private val rbracket by literalToken("]")
    private val str by regexToken("\"[^\\\\\"]*(\\\\[\"nrtbf\\\\][^\\\\\"]*)*\"") map { it.text.run { substring(1, lastIndex) } }
    private val jsonTrue by literalToken("true") map { Json.Bool(true) }
    private val jsonFalse by literalToken("false") map { Json.Bool(false) }
    private val jsonNull by literalToken("null") map Json.Null
    private val jsonNum by regexToken("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?") map { Json.Num(it.text.toDouble()) }
    private val jsonStr by str map { Json.Str(it) }

    private val keyValue by parser { str() * -colon to jsonValue() }
    private val jsonObj by parser { -lbrace * (split(keyValue, comma)) * -rbrace } map { Json.Obj(it.toMap()) }

    private val jsonArr by parser { -lbracket * split(jsonValue, comma) * -rbracket } map { Json.Arr(it) }
    private val jsonValue: Parser<Json> by jsonNull or jsonTrue or jsonFalse or jsonNum or jsonStr or jsonArr or jsonObj
    override val root by jsonValue
}
