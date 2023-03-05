# Parsus FAQ

This page contains answers to frequently asked questions about Parsus.

### Can Parsus be implemented via `DeepRecursiveFunction`?

[Deep recursive functions](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deep-recursive-function/) are part of Kotlin stdlib.
They allow to write recursive functions that are not limited by the stack size.

There are two alternatives when approximating Parsus with deep recursion:

- Convert parse errors to exceptions
- Use `ParseResult<T> = T | ParseError` as a return type for deep recursive functions

Using exceptions to represent parse errors allows to preserve clean parser signatures.
The return type of parser would be a clean `T`, and appropriately placed `try-catch` statements would become points of backtracking.
However, using exceptions would be **prohibitively expensive**.
Parsing errors occur frequently during parsing when one alternative fails and another one should be tried.

Using explicit `ParseResult<T>` as the return type of the deep functions defeats the purpose of clean signatures of parser combinators. It also forces
users to add explicit error handling code for each call-site.

See an [example implementation here](https://gist.github.com/alllex/afcaf4dd1d1c4b1a5f2fa825f471e9d3).

### How is Parsus different from `better-parse`?

The [`better-parse`](https://github.com/h0tk3y/better-parse) is also a parser combinator library.
It works by allowing the users to compose parsers and to consume the combined parsed state in the form
of [`Tuple` data classes](https://github.com/h0tk3y/better-parse/blob/master/src/commonMain/kotlin/generated/andFunctions.kt).

The grammar APIs of `better-parse` are as type-safe and concise as that of Parsus.
However, they do not offer the ability to write parsers in the procedural style.
And the custom `Tuple` classes create a disconnect between the parser execution and its result.

Defining new combinators in `better-parse` is also more cumbersome than in Parsus.
Each combinator has to be defined as a new class, whereas in Parsus it is just a function.

### Why is `Parser` an interface and not a suspending lambda?

The parser type could have been potentially defined as

```kotlin
typealias Parser<T> = suspend ParsingScope.() -> T
```

However, that definition does not play well with restricted suspension safety measures (`@RestrictsSuspension`).

It is very useful to have `Token` implement a `Parser<TokenMatch>`.
But that would not work anymore with `Parser` being a `suspend` lambda.
Extending a `suspend` lambda (although possible in general) would not allow calling restricted suspending methods
in the actual `invoke` implementation of the `Token` class.
The method that needs to be called is `tryParse(Token)`, which is a restricted suspending method on `ParsingScope`.
