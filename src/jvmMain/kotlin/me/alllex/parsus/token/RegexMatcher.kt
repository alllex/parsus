package me.alllex.parsus.token

import java.util.regex.Matcher


//internal actual fun Regex.toMatcher(): RegexMatcher =
//    RegexMatcherJvm(toPattern().matcher(""))
//
//
//internal class RegexMatcherJvm(
//    private val matcher: Matcher
//) : RegexMatcher {
//    override fun reset(input: CharSequence) {
//        matcher.reset(input)
//    }
//
//    override fun region(start: Int, end: Int) {
//        matcher.region(start, end)
//    }
//
//    override fun find(): Boolean = matcher.find()
//
//    override fun end(): Int = matcher.end()
//}
