package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.reflect.full.isSubclassOf


internal object JsoupCssSelectorDataExtractor : DataExtractor {

    private const val PARENT = " > .."

    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult {
        val document = Jsoup.parse(rawContent)
        val result = selection.map { it.key to transformAndExtract(document, it.value) }
            .associate { toOutput(it) }

        return ExtractionResult(result)
    }

    private fun transformAndExtract(document: Document, xpath: String): Any {
        val descendantSplits = parseDescendants(xpath)
            .map { Descendant(it) }
            .toMutableList()
        val first = descendantSplits.removeFirst()

        var elements = if (first.toJsoup().contains(PARENT)) {
            first.toJsoup().split(PARENT).fold(document.allElements) { acc: Elements, e: String ->
                if (e.neitherNullNorBlank()) {
                    acc.select(e).parents()
                } else {
                    acc
                }
            }
        } else {
            document.select(first.toJsoup())
        }

        val last = if (descendantSplits.isEmpty()) {
            first
        } else {
            descendantSplits.removeLast()
        }

        descendantSplits.forEach {
            elements = elements.select(it.toJsoup())
        }

        val terminatingChild = when {
            last.hasTerminatingChild() -> {
                if ( first != last) {
                    elements = elements.select(last.toJsoup())
                }
                last.terminatingChild
            }
            last.isTerminatingChild() -> {
                last.toJsoup()
            }
            else -> {
                if ( first != last) {
                    elements = elements.select(last.toJsoup())
                }
                EMPTY
            }
        }

        return elementSelection(elements, terminatingChild)
    }

    private fun parseDescendants(value: String): List<String> {
        val result = mutableListOf<String>()
        var currentDescendant = StringBuilder()
        var isChild = false
        var isString = false

        for (char in value) {
            when {
                char == '/' && currentDescendant.isEmpty() -> {}
                char == '/' && currentDescendant.isNotEmpty() && !isChild && !isString -> {
                    isChild = true
                }
                char == '/' && currentDescendant.isNotEmpty() && isChild -> {
                    result.add(currentDescendant.toString())
                    currentDescendant = StringBuilder()
                    isChild = false
                    isString = false
                }
                char != '/' && isChild -> {
                    currentDescendant.append("/")
                    currentDescendant.append(char)
                    isChild = false
                }
                char == '\'' -> {
                    isString = !isString
                    currentDescendant.append(char)
                }
                else -> currentDescendant.append(char)
            }
        }

        if (currentDescendant.isNotEmpty()) {
            result.add(currentDescendant.toString())
        }

        return result
    }

    private fun elementSelection(jsoupElements: Elements, terminatingChild: String): Any {
        return when {
            jsoupElements.isEmpty() -> NotFound
            terminatingChild == "text()" -> jsoupElements.eachText()
            terminatingChild == "node()" -> jsoupElements.dataNodes().map { it.wholeData.trim() }
            terminatingChild.eitherNullOrBlank() -> jsoupElements.textNodes().map { it.text().trim() }.filter { it.neitherNullNorBlank() }
            terminatingChild.startsWith('@') -> jsoupElements.eachAttr(terminatingChild.trimStart('@')) ?: NotFound
            else -> throw IllegalStateException("unmapped case")
        }
    }

    private fun toOutput(item: Pair<OutputKey, Any>) : Pair<OutputKey, Any> {
        if (item.second::class.isSubclassOf(Iterable::class)) {
            val castedList = (item.second as Iterable<*>).toList()

            return if (castedList.size == 1) {
                item.first to castedList.first()!!
            } else {
                item.first to castedList
            }
        } else {
            return item
        }
    }
}

