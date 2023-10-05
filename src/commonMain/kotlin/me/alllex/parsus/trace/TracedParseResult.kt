package me.alllex.parsus.trace

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.parser.ParseResult

@ExperimentalParsusApi
class TracedParseResult<out R, T>(
    val result: ParseResult<R>,
    val trace: T,
)
