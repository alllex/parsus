# Parsus

A framework for writing composable parsers based on Kotlin Coroutines.

```kotlin
val booleanGrammar = object : Grammar<BooleanExpression>() {
    val ws by regexToken("\\s+", ignored = true)
    val id by regexToken("\\w+")
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val not by literalToken("!")
    val and by literalToken("&")
    val or by literalToken("|")
    val impl by literalToken("->")

    val term: Parser<BooleanExpression> by
        (id map { Var(it.text) }) or
                parser { Not(-not * term()) } or
                parser { -lpar * root() * -rpar }

    val andChain by parser { leftAssociative(term, and) { a, _, b -> And(a, b) } }
    val orChain by parser { leftAssociative(andChain, or) { a, _, b -> Or(a, b) } }
    val implChain by parser { rightAssociative(orChain, impl) { a, _, b -> Impl(a, b) } }
    override val root by implChain
}

val ast = booleanGrammar.parseToEnd("a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a")
```

## Features

* **0-dependencies**. Parsus only depends on Kotlin Standard Library.
* **Pure Kotlin**. Parsers are specified by users directly in Kotlin without the need for any codegen.
* **Debuggable**. Since parsers are pure non-generated Kotlin, they can be debugged like any other program.
* **Stack-Neutral**. Leveraging the power of coroutines, parsers are able to process inputs with arbitrary nesting
  entirely avoiding stack-overflow problems.
* **Extensible**. Parser combinators provided out-of-the-box are built on top of only a few core primitives. Therefore,
  users can extend the library with custom powerful combinators suitable for their use-case.
* **Composable**. Parsers are essentially functions, so they can be composed in imperative or declarative fashion
  allowing for unlimited flexibility.

There are, however, no pros without cons. Parsus relies heavily on coroutines machinery. This comes at a cost of some
performance and memory overhead as compared to other techniques such as generating parsers at compile-time from special
grammar formats.

## Context

Most often, coroutines in Kotlin are explored and used in the context of concurrency.
This is not surprising, because they allow turning callback-ridden asynchronous code
into sequential implementations that are less error-prone and easier to read.

In Kotlin, [structured concurrency][structured-concurrency] and other machinery related to multi-threaded environments
are provided by `kotlinx.coroutines` library. Note the `x` after `kotlin`.
This library, like any other, makes use of lower-level capabilities of the language itself.
More specifically, the main and only mechanism of Kotlin enabling coroutines is **suspension**.

Kotlin's `suspend` keyword allows declaring so called *suspending functions*.
Most of the time adding this additional keyword will be seen as a necessary
down payment prior to entering the world of structured concurrency.
Not all the time, though. Even in the Kotlin standard library there is at least one example
of using suspending functions without any multi-threaded context. Namely, sequence builders.

You can build an infinite sequence of Fizz-Buzz numbers like this:
```kotlin
fun main() {
    val fb = sequence {
        var i = 1
        while (true) {
            if (i % 3 == 0 || i % 5 == 0) yield(i)
            i++
        }
    }

    for (x in fb.take(10)) {
        println(x)
    }
}
```

As you may have guessed, the lambda we pass to the `sequence` builder is a suspending function.
From inside this lambda we can use `yield` function, which is also suspending.

After a careful inspection, we can conclude that suspending functions related to sequence builders
have nothing to do with dispatchers, flows and channels from `kotlinx.coroutines`.
Both of these cases simply highlight Kotlin's more powerful built-in capabilities.
Even more applications of "bare" coroutines can be found elsewhere.
E.g. coroutines can aid in rather idiomatic [implementation of monads][arrow-monad-tutorial]
directly in Kotlin.

Finally, this project itself takes on a mission of leveraging coroutines to construct and execute parsers.
Continuations, as first-class citizens, can be stored in memory, entirely avoiding unexpected stack-overflows
for heavily nested parsing rules and deeply-structured input.
Suspending functions make sequential composition of parsers trivial.
Error-handling mechanisms that come with coroutines allow for declarative definition of branching in parsers.
Everything else is a fully extensible and debuggable collection of combinators on top of just a couple core primitives.

## Acknowledgements

The structure of the project as well as the form of the grammar DSL is heavily inspired by
the [better-parse] library.

## License

Distributed under the MIT License. See `LICENSE` for more information.


[structured-concurrency]: https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency
[arrow-monad-tutorial]: https://arrow-kt.io/docs/patterns/monads/
[better-parse]: https://github.com/h0tk3y/better-parse
