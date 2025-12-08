package io.github.manamiproject.modb.app.dataset

/**
 * Different output types for dataset files.
 * @since 1.0.0
 */
enum class DatasetFileType {

    /**
     * Minified JSON file.
     * @since 1.0.0
     */
    JSON_MINIFIED,

    /**
     * Minified JSON file compressed using Zstandard.
     * @since 1.0.0
     */
    JSON_MINIFIED_ZST,

    /**
     * JSON lines format.
     * @since 1.10.0
     */
    JSON_LINES,

    /**
     * JSON lines format compressed using Zstandard.
     * @since 1.10.0
     */
    JSON_LINES_ZST;
}