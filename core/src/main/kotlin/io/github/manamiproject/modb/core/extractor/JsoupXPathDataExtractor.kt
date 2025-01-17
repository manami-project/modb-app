package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.reflect.full.isSubclassOf

internal object JsoupXPathDataExtractor : DataExtractor {

    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult {
        val document = Jsoup.parse(rawContent)

        val result: Map<OutputKey, Any> = selection.map { transformXpathToTriple(it) }
            .map { elementSelection(document, it) }
            .associate { toOutput(it) }

        return ExtractionResult(result)
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

    private fun transformXpathToTriple(entry: Map.Entry<OutputKey, Selector>): Triple<OutputKey, Selector, String> {
        val split = entry.value.split('/')
        val allExceptLast = split.subList(0, split.size-1).joinToString("/")

        return when {
            split.last().startsWith("@") -> Triple(entry.key, allExceptLast, split.last().trimStart('@'))
            split.last().endsWith("text()") -> Triple(entry.key, allExceptLast, split.last())
            split.last().endsWith("node()") -> Triple(entry.key, allExceptLast, split.last())
            else -> Triple(entry.key, entry.value, EMPTY)
        }
    }

    private fun elementSelection(document: Document, entry: Triple<OutputKey, Selector, String>): Pair<OutputKey, Any> {
        val jsoupElements = if (entry.second.neitherNullNorBlank()) {
            document.selectXpath(entry.second)
        } else {
            document.allElements
        }

        val value = when {
            jsoupElements.isEmpty() -> NotFound
            entry.third == "text()" -> jsoupElements.eachText() ?: NotFound
            entry.third == "node()" -> jsoupElements.dataNodes().map { it.wholeData.trim() }
            entry.third == EMPTY -> jsoupElements.textNodes().map { it.text().trim() }.filter { it.neitherNullNorBlank() }
            else -> jsoupElements.eachAttr(entry.third) ?: NotFound
        }

        return entry.first to value
    }
}