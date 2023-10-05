package me.alllex.parsus.tokenizer

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.trace.TokenMatchingTrace

@OptIn(ExperimentalParsusApi::class)
internal interface Tokenizer {
    val input: String
    fun getTokenMatchingTrace(): TokenMatchingTrace?
    fun findContextFreeMatch(fromIndex: Int): TokenMatch?
    fun findMatchOf(fromIndex: Int, targetToken: Token): TokenMatch?
}
