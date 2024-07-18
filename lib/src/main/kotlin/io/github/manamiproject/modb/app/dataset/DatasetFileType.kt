package io.github.manamiproject.modb.app.dataset

/**
 * Different output types for dataset files.
 * @since 1.0.0
 */
enum class DatasetFileType {
    /**
     * Pretty print JSON file.
     * @since 1.0.0
     */
    JSON,

    /**
     * Minified JSON file.
     * @since 1.0.0
     */
    JSON_MINIFIED,

    /**
     * Zipped minified JSON file.
     * @since 1.0.0
     */
    ZIP;
}