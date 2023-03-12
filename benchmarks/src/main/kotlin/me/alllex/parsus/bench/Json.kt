package me.alllex.parsus.bench

import kotlin.math.roundToLong

sealed class Json {
    object Null : Json() { override fun toString(): String = "Null" }
    data class Bool(val value: Boolean) : Json()
    data class Num(val value: Double) : Json()
    data class Str(val value: String) : Json()
    data class Arr(val values: List<Json>) : Json()
    data class Obj(val values: Map<String, Json>) : Json()
}

fun jsonToString(json: Json, indentInc: Int = 2): String {
    fun StringBuilder.indent(count: Int) = repeat(count) { append(" ") }
    fun StringBuilder.appendQuoted(s: String) = append('"').append(s).append('"')
    fun helper(j: Json, indent: Int, sb: StringBuilder) {
        when (j) {
            Json.Null -> sb.append("null")
            is Json.Bool -> sb.append(j.value)
            is Json.Str -> sb.appendQuoted(j.value)
            is Json.Num -> {
                val d = j.value
                val l = d.roundToLong()
                if (d.isFinite() && d == l.toDouble()) {
                    sb.append(l)
                } else {
                    sb.append(d)
                }
            }
            is Json.Arr -> {
                if (j.values.isEmpty()) {
                    sb.append("[]")
                } else {
                    sb.append("[")
                    var first = true
                    for (value in j.values) {
                        if (!first) sb.append(",")
                        sb.appendLine()
                        sb.indent(indent + indentInc)
                        helper(value, indent + indentInc, sb)
                        first = false
                    }
                    sb.appendLine()
                    sb.indent(indent)
                    sb.append("]")
                }
            }
            is Json.Obj -> {
                if (j.values.isEmpty()) {
                    sb.append("{}")
                } else {
                    sb.append("{")
                    var first = true
                    for ((key, value) in j.values) {
                        if (!first) sb.append(",")
                        sb.appendLine()
                        sb.indent(indent + indentInc)
                        sb.appendQuoted(key).append(": ")
                        helper(value, indent + indentInc, sb)
                        first = false
                    }
                    sb.appendLine()
                    sb.indent(indent)
                    sb.append("}")
                }
            }
        }
    }

    val sb = StringBuilder()
    helper(json, 0, sb)
    return sb.toString()
}
