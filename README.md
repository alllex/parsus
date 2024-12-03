# Parsus

[![Maven Central](https://img.shields.io/maven-central/v/me.alllex.parsus/parsus.svg?color=success)](https://search.maven.org/search?q=g:me.alllex.parsus)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Gradle build](https://github.com/alllex/parsus/actions/workflows/check.yml/badge.svg)](https://github.com/alllex/parsus/actions/workflows/check.yml)

A framework for writing composable parsers for JVM, JS and Kotlin/Native based on Kotlin Coroutines.

```kotlin
val booleanGrammar = object : Grammar<Expr>() {
    init { regexToken("\\s+", ignored = true) }
    val id by regexToken("\\w+")
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val not by literalToken("!")
    val and by literalToken("&")
    val or by literalToken("|")
    val impl by literalToken("->")

    val variable by id map { Var(it.text) }
    val negation by -not * ref(::term) map { Not(it) }
    val braced by -lpar * ref(::root) * -rpar

    val term: Parser<Expr> by variable or negation or braced

    val andChain by leftAssociative(term, and, ::And)
    val orChain by leftAssociative(andChain, or, ::Or)
    val implChain by rightAssociative(orChain, impl, ::Impl)

    override val root by implChain
}

val ast = booleanGrammar.parse("a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a").getOrThrow()
```

## Usage

<details open>
<summary>Using with Gradle for JVM projects</summary>

```kotlin
dependencies {
    implementation("me.alllex.parsus:parsus:0.6.1")
}
```

</details>

<details open>
<summary>Using with Gradle for Multiplatform projects</summary>

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("me.alllex.parsus:parsus:0.6.1")
            }
        }
    }
}
```

</details>


<details>
<summary>Using with Maven for JVM projects</summary>

```xml
<dependency>
  <groupId>me.alllex.parsus</groupId>
  <artifactId>parsus-jvm</artifactId>
  <version>0.6.1</version>
</dependency>
```

> ℹ️ The Parsus artifact ID must have a `-jvm` suffix when using Maven.
> 
> Parsus is a Kotlin Multiplatform library with multiple targets, and Maven only supports JVM targets, and needs the `-jvm` suffix to select the Kotlin/JVM artifact.
> (Gradle can automatically select the correct target, and does not require a suffix.)

</details>

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

## Quick Reference

This is a reference of some of the basic combinators provided by the library.

There is a combinator available in both procedural-style and combinator-style grammars.
You can pick and choose the style for each parser and sub-parser, as there are no restrictions.

| Description | Grammars |
| ----------- | -------- |
| Parsing a token and getting its text&#13;&#13;Parses: `ab`, `aB` | Procedural:&#13;<pre lang="kotlin">val ab by regexToken("a[bB]")&#13;override val root by parser {&#13;    val abMatch = ab()&#13;    abMatch.text&#13;}</pre>Combinator:&#13;<pre lang="kotlin">val ab by regexToken("a[bB]")&#13;override val root by ab map { it.text }</pre> |
| Parsing two tokens sequentially&#13;&#13;Parses: `ab`, `aB` | Procedural:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by parser {&#13;    val aMatch = a()&#13;    val bMatch = b()&#13;    aMatch.text to bMatch.text&#13;}</pre>Combinator:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by a and b map&#13;    { (aM, bM) -> aM.text to bM.text }</pre> |
| Parsing one of two tokens&#13;&#13;Parses: `a`, `b`, `B` | Procedural:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by parser {&#13;    val abMatch = choose(a, b)&#13;    abMatch.text&#13;}</pre>Combinator:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by a or b map { it.text }</pre> |
| Parsing an optional token&#13;&#13;Parses: `ab`, `aB`, `b`, `B` | Procedural:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by parser {&#13;    val aMatch = poll(a)&#13;    val bMatch = b()&#13;    aMatch?.text to bMatch.text&#13;}</pre>Combinator:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by maybe(a) and b map&#13;    { (aM, bM) -> aM?.text to bM.text }</pre> |
| Parsing a token and ignoring its value&#13;&#13;Parses: `ab`, `aB` | Procedural:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by parser {&#13;    skip(a) // or just a() without using the value&#13;    val bMatch = b()&#13;    bMatch.text&#13;}</pre>Combinator:&#13;<pre lang="kotlin">val a by literalToken("a")&#13;val b by regexToken("[bB]")&#13;override val root by -a * b map { it.text }</pre> |

## Introduction

The goal of a grammar is to define rules by which to turn an input string of characters into a structured value. This
value is usually an [abstract syntax tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree). But it could also be an
evaluated result, if we have specified evaluation rules directly in the grammar.

In order to define a grammar we only need two things: list of tokens and a root parser. Here is how one of the simplest
grammars looks with Parsus:

```kotlin
val g1 = object : Grammar<String>() {
    val tokenA by literalToken("a")
    override val root by parser { tokenA().text }
}

println(g1.parseOrThrow("a")) // prints "a"
```

It is just a few lines of declarative code, but there a lot going on under the hood. So, let us break it down.

### Grammars

First, there is the `Grammar` class that needs to be extended in order to define you custom grammar. In the example
above an anonymous class is declared, but it could just as well be a normal class.

```kotlin
class MyClass : Grammar<MyResult>() {
    // tokens and parsers go here

    override val root: Parser<MyResult> = TODO()
}
```

There are two important things to note. The `Grammar` is a generic class, and has a type parameter that defines the
result type of the `root` parser. Because Kotlin requires us to specify type parameters of the class, often the explicit
type of the `root` parser can be omitted. The `root` parser will be used to produce the parsed result when calling a
method such as `parseToEnd` on a grammar. However, before we can discuss how to define the `root` and other parsers, we
need to understand the basic building block of any parser - a token.

### Tokens

Each token we declare within a grammar describes a pattern of how this token can be recognized in the input string.
Whenever a parser requires the next token to proceed, the parser asks the grammar to find a token match for the current
position in the input. When a match is found it is described by the token, an `offset` in the input string where the
match starts, and the `length` of the match.

The simplest type of token is a literal token. It matches only strings that are exactly like the given literal.
Therefore, the token `tokenA` from the example will only match if the character in the current position is `"a"`.

```kotlin
    val tokenA by literalToken("a")
```

Another thing to note is that the member `tokenA` is declared via the `by` keyword, meaning that it uses Kotlin's
property-delegation mechanism. When declaring tokens this way, they are automatically registred within a grammar, so
they can participate in the matching process when parsing.

Alternatively, the token could be registered anonymously. This could be useful, when we do not need to reference the
token anywhere else when writing parsers. Most often, the tokens that need to be ignored are defined this way.

```kotlin
val g2 = object : Grammar<String>() {
    init {
        regexToken("\\s+", ignored = true)
    }

    val tokenA by literalToken("a")
    override val root by parser { tokenA().text }
}

println(g2.parseOrThrow(" a\t")) // prints "a"
```

In this example, we create a token by calling `regexToken`.
This token will use the regular expression to match any whitespace in the input string.
Since we want to simply ignore the whitespace, we will not reference this token in any of the parsers.
Therefore, we register the token in the init-block of the class without assigning it to a member.

Now, that we know how to declare and register different kinds of tokens, let us explore how to use those tokens to write
parsers.

### Parsers

Parser definition achieves two goals. Firstly, it defines the sequence of tokens that is expected to appear in the
input. Secondly, it transforms the matched tokens into a value.

One of the simplest parsers that we can construct expects only one token and returns the text of the token match as a
value. And that is exactly what we saw previously.

```kotlin
val g1 = object : Grammar<String>() {
    val tokenA by literalToken("a")
    override val root by parser { tokenA().text }
}
```

In order to understand how to use parsers, we need to take a look at the core abstractions.

The central piece of the puzzle is the `Parser` interface itself.

```kotlin
interface Parser<out T> {
    suspend fun ParsingScope.parse(): T
}
```

Essentially, a parser is a function that can be called within a parsing scope and would return a parsed value.
When something is a function, it can almost certainly be represented as a lambda.
This is exactly how we have seen the parsers to be defined using the `parser { ... }` function that takes lambda and returns a parser.

The parsing result is an explicit representation of either a successfully parsed value, or an error that the parser
encountered while trying to process the input.

```kotlin
sealed class ParseResult<out T>
data class ParsedValue<T>(val value: T) : ParseResult<T>()
abstract class ParseError : ParseResult<Nothing>()
data class MismatchedToken(val expected: Token, val found: TokenMatch) : ParseError()
// more parser errors
```

The most powerful thing about parsers is that they can be composed. A parsing scope is what gives parsers this power.
The parser scope interface provides an extension function to execute any parser and extract its result.

```kotlin
interface ParsingScope {
    suspend operator fun <R> Parser<R>.invoke(): R
    // ... more ...
}
```

We have already seen an example with a call to this function: tokens are parsers too. The `Token` class
implements `Parser<TokenMatch>`, and when invoked within a parsing scope it would return an actual `TokenMatch`. From
this match we can take the text fragment of the input string to which this match corresponds. The text fragment can then
be converted into a number or stored as a name of an identifier, etc.

Here is grammar that parses an integer:

```kotlin
val g3 = object : Grammar<Int>() {
    val tokenNum by regexToken("[0-9]+")
    override val root by parser { tokenNum().text.toInt() }
}

println(g3.parseOrThrow("123")) // prints 123
```

### Parser Combinators

In order to combine parsers, we need to define more than one. The intermediate parsers can be declared as members of the
same grammar class to make them easier to be reused.

As we have learned previously, tokens are parsers. So we can define a couple of them to play with.

```kotlin
val g4 = object : Grammar<String>() {
    val tokenNum by regexToken("[0-9]+")
    val tokenId by regexToken("[a-z]+")
    val tokenPlus by literalToken("+")
    override val root by parser {
        val id = tokenId().text
        tokenPlus()
        val num = tokenNum().text
        "($id) + ($num)"
    }
}

println(g4.parseOrThrow("abc+123")) // prints "(abc) + (123)"
```

This example shows the main way in which parsers are combined - sequentially. The `root` parser expects first an id to
appear, then a plus-sign, then a number. If at any point there is an unexpected token, then the whole parser fails with
the mismatched-token error.

Notice also, that we use another useful property of the sequential execution. With the `tokenPlus()` statement we
execute the parser, but we ignore the result. This is most often used with token-parsers when we only need to make sure
that a certain piece of syntax is in the expected place in the input.

Another important way of combining parsers is to say that we expect *one of several* parsers to succeed at a certain
point. Even in the case when the first parser fails, the parent parser does not produce an error immediately. Instead,
the parent parser tries out the remaining alternatives. If there is one alternative that succeeds, the parent parser
takes its result and proceeds without any errors.

We can use the `choose` function from the `ParsingScope` to achieve this behaviour:

```kotlin
val g5 = object : Grammar<String>() {
    val tokenNum by regexToken("[0-9]+")
    val tokenId by regexToken("[a-z]+")
    val tokenPlus by literalToken("+")
    override val root by parser {
        val idOrNum1 = choose(tokenNum, tokenId).text
        tokenPlus()
        val idOrNum2 = choose(tokenNum, tokenId).text
        "($idOrNum1) + ($idOrNum2)"
    }
}

println(g5.parseOrThrow("abc+123")) // prints "(abc) + (123)"
println(g5.parseOrThrow("909+wow")) // prints "(909) + (wow)"
```

Now we have a repeating piece of code inside our parser implementation. So we ought to refactor it by introducing
another intermediate parser `term` to do the job. Since `term` is a parser, it can be invoked within the parsing scope.

```kotlin
val g6 = object : Grammar<String>() {
    val tokenNum by regexToken("[0-9]+")
    val tokenId by regexToken("[a-z]+")
    val tokenPlus by literalToken("+")
    val term by parser { choose(tokenNum, tokenId).text }
    override val root by parser {
        val idOrNum1 = term()
        tokenPlus()
        val idOrNum2 = term()
        "($idOrNum1) + ($idOrNum2)"
    }
}
```

Armed with this knowledge of the basics, you can now explore more sophisticated parser implementations that use various
extension functions to make parser definitions look declarative. Also, feel free to get familiar the with core
interfaces and their extension functions to learn how more elaborate parser combinators can be created from the provided
primitives.

## Examples

Here are some examples of grammars written with Parsus:

* Arithmetic expression parser and calculator: [Arithmetic.kt](./demo/src/commonMain/kotlin/Arithmetic.kt)
* Boolean expression parser: [BooleanExpression.kt](./demo/src/commonMain/kotlin/BooleanExpression.kt)
* S-expression parser: [SExpression.kt](./demo/src/commonMain/kotlin/SExpression.kt)
* JSON parser: [(link)](benchmarks/src/main/kotlin/me/alllex/parsus/bench/NaiveJsonGrammar.kt)

## Coroutines

Most often, coroutines in Kotlin are explored and used in the context of concurrency. This is not surprising, because
they allow turning callback-ridden asynchronous code into sequential implementations that are less error-prone and
easier to read.

In Kotlin, [structured concurrency][structured-concurrency] and other machinery related to multi-threaded environments
are provided by `kotlinx.coroutines` library. Note the `x` after `kotlin`. This library, like any other, makes use of
lower-level capabilities of the language itself. More specifically, the main and only mechanism of Kotlin enabling
coroutines is **suspension**.

Kotlin's `suspend` keyword allows declaring so called *suspending functions*. Most of the time adding this additional
keyword will be seen as a necessary down payment prior to entering the world of structured concurrency. Not all the
time, though. Even in the Kotlin standard library there is at least one example of using suspending functions without
any multi-threaded context. Namely, sequence builders.

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

As you may have guessed, the lambda we pass to the `sequence` builder is a suspending function. From inside this lambda
we can use `yield` function, which is also suspending.

After a careful inspection, we can conclude that suspending functions related to sequence builders have nothing to do
with dispatchers, flows and channels from `kotlinx.coroutines`. Both of these cases simply highlight Kotlin's more
powerful built-in capabilities. Even more applications of "bare" coroutines can be found elsewhere. E.g. coroutines can
aid in rather idiomatic [implementation of monads][arrow-monad-tutorial]
directly in Kotlin.

Finally, this project itself takes on a mission of leveraging coroutines to construct and execute parsers.
Continuations, as first-class citizens, can be stored in memory, entirely avoiding unexpected stack-overflows for
heavily nested parsing rules and deeply-structured input. Suspending functions make sequential composition of parsers
trivial. Error-handling mechanisms that come with coroutines allow for declarative definition of branching in parsers.
Everything else is a fully extensible and debuggable collection of combinators on top of just a couple core primitives.

## Acknowledgements

The structure of the project as well as the form of the grammar DSL is heavily inspired by the [better-parse] library.

## License

Distributed under the MIT License. See `LICENSE` for more information.


[structured-concurrency]: https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency

[arrow-monad-tutorial]: https://arrow-kt.io/docs/patterns/monads/

[better-parse]: https://github.com/h0tk3y/better-parse
