package com.example.dshinde.myapplication_xmlpref.helper

import android.graphics.Color
import org.commonmark.node.StrongEmphasis
import org.json.JSONArray
import org.json.JSONObject

object MarkdownFormatter {

    @JvmStatic
    fun formatJsonWithMarkdown(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val trimmed = input.trim()
        if (!looksLikeJson(trimmed)) return trimmed

        return try {
            if (trimmed.startsWith("[")) {
                formatArray(JSONArray(trimmed))
            } else {
                formatObject(JSONObject(trimmed))
            }
        } catch (e: Exception) {
            trimmed
        }
    }

    private fun looksLikeJson(text: String): Boolean =
        (text.startsWith("{") && text.endsWith("}")) ||
                (text.startsWith("[") && text.endsWith("]"))

    private fun looksLikeMarkdown(text: String): Boolean {
        val patterns = listOf(
            Regex("```"),                // code blocks
            Regex("""(^|\n)#{1,6}\s"""), // headings
            Regex("""\*\*.*?\*\*"""),    // bold
            Regex("""\*.*?\*"""),        // italic
            Regex("""\|.*\|"""),         // tables
            Regex("""(^|\n)([-*+]|\d+\.)\s""") // lists
        )
        return patterns.any { it.containsMatchIn(text) }
    }

    private fun formatObject(obj: JSONObject, indent: Int = 0): String {
        val prefix = "  ".repeat(indent)
        val builder = StringBuilder()


        obj.keys().forEach { key ->
            val value = obj.get(key)

            builder.append(prefix).append("**").append(key).append("**: ")

            when (value) {
                is JSONObject -> {
                    builder.append("\n").append(formatObject(value, indent + 1))
                }
                is JSONArray -> {
                    builder.append("\n").append(formatArray(value, indent + 1))
                }
                else -> {
                    val text = value.toString().trim()
                    if (looksLikeJson(text)) {
                        // Nested JSON string
                        builder.append("\n").append(formatJsonWithMarkdown(text))
                    } else if (looksLikeMarkdown(text)) {
                        // Preserve Markdown formatting, indent it visually
                        builder.append("\n").append(indentMultilineMarkdown(text, indent + 1)).append("\n")
                    } else {
                        // Regular text
                        builder.append(text).append("\n")
                    }
                }
            }

            builder.append("\n") // space between fields
        }

        return builder.toString()
    }

    private fun formatArray(arr: JSONArray, indent: Int = 0): String {
        val prefix = "  ".repeat(indent)
        val builder = StringBuilder()

        for (i in 0 until arr.length()) {
            val item = arr.get(i)
            builder.append(prefix).append("- ")
            when (item) {
                is JSONObject -> builder.append("\n").append(formatObject(item, indent + 1))
                is JSONArray -> builder.append("\n").append(formatArray(item, indent + 1))
                else -> {
                    val text = item.toString().trim()
                    if (looksLikeMarkdown(text)) {
                        builder.append("\n").append(indentMultilineMarkdown(text, indent + 1))
                    } else {
                        builder.append(text)
                    }
                    builder.append("\n")
                }
            }
        }

        builder.append("\n")
        return builder.toString()
    }

    /**
     * Adds indentation to multiline Markdown content (code blocks, tables, etc.)
     */
    private fun indentMultilineMarkdown(markdown: String, indent: Int): String {
        val prefix = "  ".repeat(indent)
        return markdown.lines().joinToString("\n") { line ->
            if (line.isBlank()) line else prefix + line
        }
    }
}