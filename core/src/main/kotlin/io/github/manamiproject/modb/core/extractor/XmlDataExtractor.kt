package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Extract data using XPath.
 * @since 12.0.0
 */
public object XmlDataExtractor : DataExtractor {

    private val log by LoggerDelegate()

    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult {
        return try {
            JsoupCssSelectorDataExtractor.extract(rawContent, selection)
        } catch (e: Exception) {
            log.warn { "Tried to execute query using JsoupCssDataExtractor. This resulted in error: [${e.message}]. Retrying extraction using JsoupXPathDataExtractor." }
            JsoupXPathDataExtractor.extract(rawContent, selection)
        }
    }
}