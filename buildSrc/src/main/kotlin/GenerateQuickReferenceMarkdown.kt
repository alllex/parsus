import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

@CacheableTask
abstract class GenerateQuickReferenceMarkdown : DefaultTask() {

    @get:[InputFile PathSensitive(PathSensitivity.NONE)]
    abstract val kotlinTestSource: RegularFileProperty

    @get:OutputFile
    abstract val markdownOutput: RegularFileProperty

    @TaskAction
    fun generate() {
        val input = kotlinTestSource.get().asFile.readText()
        val output = markdownOutput.get().asFile
        output.writeText(generateMarkdown(input))
    }

    private fun generateMarkdown(input: String): String {
        val entries = parseSource(input)
        return buildString {
            appendLine("| Description | Procedural Grammar | Combinator Grammar | Parsing |")
            appendLine("| ----------- | ------------------ | ------------------ | ------- |")
            for (entry in entries) {
                append("| ")
                append(entry.description)
                append(" | ")
                append(entry.proceduralGrammar.toMultilineMarkdownCodeBlock())
                append(" | ")
                append(entry.combinatorGrammar.toMultilineMarkdownCodeBlock())
                append(" | ")
                append(entry.testCases.joinToString("<br/>"))
                appendLine(" |")
            }
        }
    }

    private fun List<String>.toMultilineMarkdownCodeBlock(): String {
        // TODO: syntax highlight
        return joinToString("<br/>", prefix = "<pre>", postfix = "</pre>")
    }

    private data class QuickRefEntry(
        val description: String,
        val proceduralGrammar: List<String>,
        val combinatorGrammar: List<String>,
        val testCases: List<String>,
    )

    private fun <T> Iterator<T>.skipUntil(predicate: (T) -> Boolean): T? {
        while (hasNext()) {
            val v = next()
            if (predicate(v)) return v
        }

        return null
    }

    private fun <T> Iterator<T>.collectUntil(predicate: (T) -> Boolean): List<T> {
        val result = mutableListOf<T>()
        while (hasNext()) {
            val v = next()
            if (predicate(v)) break
            result += v
        }

        return result
    }

    private fun parseSource(input: String): List<QuickRefEntry> {
        val entries = mutableListOf<QuickRefEntry>()
        val linesIter = input.lines().iterator()
        while (linesIter.hasNext()) {
            // Find the next test function whose name starts with `quickRef`
            linesIter.skipUntil { it.trim().startsWith("fun quickRef") } ?: break

            // Extract the first line-comment from the function body
            val descriptionLine = linesIter.next().trim()
            check(descriptionLine.startsWith("//")) { "Expected a // comment with description, got: $descriptionLine" }
            val description = descriptionLine.removePrefix("//").trim()

            // Find the `testCases` and extract them
            linesIter.skipUntil { it.trim().startsWith("val testCases") } ?: break

            val testCaseLines = linesIter.collectUntil { it.trim().startsWith(")") }

            val testCases = testCaseLines.asSequence()
                .map { it.trim().removeSuffix(",") }
                .map { it.replace("\"", "`") }
                .map { it.replaceFirst(" to ", " => ") }
                .map { it.replace(" to ", ", ") }

            // Find the procedural grammar and extract it
            linesIter.skipUntil { it.trim().startsWith("val proc") } ?: break
            val proceduralGrammarLines = linesIter.collectUntil { it.startsWith("        }") }

            // Find the combinator grammar and extract it
            linesIter.skipUntil { it.trim().startsWith("val comb") } ?: break
            val combinatorGrammarLines = linesIter.collectUntil { it.startsWith("        }") }

            // Add the entry
            entries += QuickRefEntry(
                description = description,
                proceduralGrammar = proceduralGrammarLines,
                combinatorGrammar = combinatorGrammarLines,
                testCases = testCases.toList(),
            )
        }

        return entries
    }

}
