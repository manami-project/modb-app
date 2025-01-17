package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank


internal class Descendant(private val initial: String) {

    /**
     * First part up to the first child element if it exists.
     */
    private val selfPath: String = initial.trimStart('/').substringBefore('/').ifBlank {
        initial.trimStart('/')
    }

    val terminatingChild: String by lazy {
        initial.removePrefix("/")
            .removePrefix("/")
            .removePrefix(selfPath)
            .removePrefix("/")
            .split('/')
            .last()
    }

    fun hasTerminatingChild(): Boolean = terminatingChild.neitherNullNorBlank() && (terminatingChild in setOf("text()", "node()") || terminatingChild.startsWith('@'))

    fun isTerminatingChild(): Boolean = selfPath.neitherNullNorBlank() && (selfPath in setOf("text()", "node()") || selfPath.startsWith('@'))

    private fun allFilters(value: String): List<String> {
        return ALL_FILTERS.findAll(value)
            .map { it.value }
            .toList()
    }

    private fun transformFilters(value: String): String {
        var ret = value

        allFilters(ret).forEach {
            val replacement = when {
                FILTER_ATTRIBUTE_EQUALS_VALUE.matches(it) -> { // [@attr='value']
                    val matchResult = FILTER_ATTRIBUTE_EQUALS_VALUE.matchEntire(it)!!
                    "[${matchResult.groups["attr"]!!.value}=${matchResult.groups["value"]!!.value}]"
                }

                FILTER_ATTRIBUTE_CONTAINS.matches(it) -> { // [contains(@attr, 'value')]
                    val matchResult = FILTER_ATTRIBUTE_CONTAINS.matchEntire(it)!!
                    "[${matchResult.groups["attr"]!!.value}*=${matchResult.groups["value"]!!.value}]"
                }

                FILTER_TEXT_CONTAINS.matches(it) -> { // [contains(text(), 'value')]
                    val matchResult = FILTER_TEXT_CONTAINS.matchEntire(it)!!
                    ":matchesOwn(${matchResult.groups["value"]!!.value})"
                }

                FILTER_SELECT_SIBLING.matches(it) -> { // [1]
                    val matchResult = FILTER_SELECT_SIBLING.matchEntire(it)!!
                    ":eq(${matchResult.groups["number"]!!.value})"
                }

                FILTER_ATTRIBUTE_EXISTS.matches(it) -> { // [@attr]
                    val matchResult = FILTER_ATTRIBUTE_EXISTS.matchEntire(it)!!
                    "[${matchResult.groups["attr"]!!.value}]"
                }

                else -> throw IllegalStateException("No transformation for [$it]")
            }

            ret = ret.replace(it, replacement)
        }

        return ret
    }

    fun toJsoup(): String {
        var converted = initial.removePrefix("/")
            .removePrefix("/")

        if (hasTerminatingChild()) {
            converted = initial.removePrefix("/")
                .removePrefix("/")
                .split('/')
                .toMutableList()
                .apply { removeLast() }
                .joinToString("/")
        }

        val replacements = STRING_VALUES.findAll(converted)
            .map { it.groups }
            .map { it["stringValue"] }
            .filterNotNull()
            .mapIndexed { index, matchGroup ->
                "'#|STRVAL$index|#'" to "'${matchGroup.value}'"
            }
            .toList()

        converted = replacements.fold(converted) { acc, pair ->
            acc.replace(pair.second, pair.first)
        }.replace("/", " > ")

        replacements.toMap().forEach { (replacement, original) ->
            converted = converted.replace(replacement, original)
        }

        converted = transformFilters(converted).replace("> following-sibling::", "~ ")

        return converted
    }

    private companion object {
        private val ALL_FILTERS = """\[(.*?)\]""".toRegex()
        private val FILTER_ATTRIBUTE_EQUALS_VALUE = """^\[@(?<attr>[\w-]+) *= *'(?<value>.*?)'\]$""".toRegex()
        private val FILTER_ATTRIBUTE_CONTAINS = """^\[contains\(@(?<attr>[\w-]+) *, *'(?<value>.*?)'\)\]$""".toRegex()
        private val FILTER_TEXT_CONTAINS = """^\[contains\(text\(\) *, *'(?<value>.*?)'\)\]$""".toRegex()
        private val FILTER_SELECT_SIBLING = """^\[(?<number>\d+)\]$""".toRegex()
        private val FILTER_ATTRIBUTE_EXISTS = """^\[@(?<attr>[\w-]+)\]$""".toRegex()
        private val STRING_VALUES = """.*?'(?<stringValue>.*?)'.*?""".toRegex()
    }
}