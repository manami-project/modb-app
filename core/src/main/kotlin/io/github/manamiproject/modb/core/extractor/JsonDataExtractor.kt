package io.github.manamiproject.modb.core.extractor

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read

/**
 * Extract data using JsonPath.
 * @since 12.0.0
 */
public object JsonDataExtractor : DataExtractor {

    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult {
        return ExtractionResult(selection.map{
            it.key to (JsonPath.parse(rawContent)?.read<Any>(it.value) ?: NotFound)
        }.toMap())
    }
}

