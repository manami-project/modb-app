package io.github.manamiproject.modb.serde

internal fun createExpectedDatasetPrettyPrint(vararg data: String): String {
    val joinedData = data.joinToString(",\n") { entry ->
        entry.lineSequence().map { line -> "            $line" }.joinToString("\n")
    }

    return """
        {
          "${'$'}schema": "https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/anime-offline-database.schema.json",
          "license": {
            "name": "Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0",
            "url": "https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"
          },
          "repository": "https://github.com/manami-project/anime-offline-database",
          "scoreRange": {
            "minInclusive": 1.0,
            "maxInclusive": 10.0
          },
          "lastUpdate": "2020-01-01",
          "data": [
$joinedData
          ]
        }
    """.trimIndent()
}

internal fun createExpectedDatasetMinified(vararg data: String): String {
    val joinedData = data.joinToString(",")

    return """
        {"${'$'}schema":"https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/anime-offline-database-minified.schema.json","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01","data":[$joinedData]}
    """.trimIndent()
}

internal fun createExpectedDeadEntriesPrettyPrint(vararg data: String): String {
    val joinedData = data.joinToString(",\n") { entry ->
        entry.lineSequence().map { line -> "            $line" }.joinToString("\n")
    }

    return """
        {
          "${'$'}schema": "https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/dead-entries/dead-entries.schema.json",
          "license": {
            "name": "Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0",
            "url": "https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"
          },
          "repository": "https://github.com/manami-project/anime-offline-database",
          "lastUpdate": "2020-01-01",
          "deadEntries": [
$joinedData
          ]
        }
    """.trimIndent()
}

internal fun createExpectedDeadEntriesMinified(vararg data: String): String {
    val joinedData = data.joinToString(",")

    return """
        {"${'$'}schema":"https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/dead-entries/dead-entries.schema.json","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","lastUpdate":"2020-01-01","deadEntries":[$joinedData]}
    """.trimIndent()
}