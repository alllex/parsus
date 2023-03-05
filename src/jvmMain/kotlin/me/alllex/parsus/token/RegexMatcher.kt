package me.alllex.parsus.token

import java.util.regex.Matcher


//internal actual fun Regex.toMatcher(): RegexMatcher =
//  toPattern().matcher("").toRegexMatcher()
//
//
//private fun Matcher.toRegexMatcher(): RegexMatcher {
//  return object : RegexMatcher {
//    private val matcher = this@toRegexMatcher
//
//    override fun reset(input: CharSequence): RegexMatcher = matcher.reset(input).toRegexMatcher()
//
//    override fun region(fromIndex: Int, length: Int) {
//      matcher.region(fromIndex, length)
//    }
//
//    override fun find(): Boolean = matcher.find()
//
//    override fun end(): Int = matcher.end()
//  }
//}
