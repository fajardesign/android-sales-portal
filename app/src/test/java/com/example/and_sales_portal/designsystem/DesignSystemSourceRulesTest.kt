package com.example.and_sales_portal.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Static checks for the design system rules in CLAUDE.md §4.
 * Runs on the JVM with the module directory as working directory.
 */
class DesignSystemSourceRulesTest {

    private val root = File("src/main/java/com/example/and_sales_portal/designsystem")

    private fun sources(dir: String) = File(root, dir).walkTopDown().filter { it.extension == "kt" }.toList()

    /** Rule 4: colours, dp and sp only from designsystem/theme. */
    @Test
    fun componentsUseThemeTokensOnly() {
        val literal = Regex("""Color\(0x|\b\d+(\.\d+)?\.(dp|sp)\b""")
        val violations = sources("components").flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                "${file.name}:${i + 1}: ${line.trim()}".takeIf { literal.containsMatchIn(line) }
            }
        }
        assertTrue("Literal colour/dp/sp outside theme:\n" + violations.joinToString("\n"), violations.isEmpty())
    }

    /** A slash-star sequence inside a KDoc opens a nested comment and breaks compilation. */
    @Test
    fun noNestedCommentMarkersInKdoc() {
        val violations = sources("").flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                val t = line.trim()
                val inBlockComment = t.startsWith("/**") || t.startsWith("*")
                val body = t.removePrefix("/**")
                "${file.name}:${i + 1}: $t".takeIf { inBlockComment && body.contains("/*") }
            }
        }
        assertTrue("Nested comment marker in KDoc:\n" + violations.joinToString("\n"), violations.isEmpty())
    }

    /** Rule 5: every public component file links its Figma node. */
    @Test
    fun componentsLinkFigmaNode() {
        val missing = sources("components").filterNot { it.readText().contains("figma.com/design/NCtvxBNe1WdwkKeCpSeT0C") }
        assertTrue("Missing Figma node URL in KDoc: ${missing.map { it.name }}", missing.isEmpty())
    }
}
