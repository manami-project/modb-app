package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Checks if anime with an unusually high number of either studios or producers exist.
 * @since 1.9.4
 * @property datasetFileAccessor Access to dataset files.
 * @throws IllegalStateException if entries exist which exceed the threshold for plausible number of studios and producers
 */
class StudiosAndProducersExtractionChecker(
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking if anime with an unusually high number of either studios or producers exist" }

        val identifiedStudios = datasetFileAccessor.fetchEntries().filter { it.studios.size > STUDIOS_THRESHOLD }

        if (identifiedStudios.isNotEmpty()) {
            throw IllegalStateException("Entries having more than [$STUDIOS_THRESHOLD] studios: ${identifiedStudios.map { it.sources }}")
        }

        val identifiedProducers = datasetFileAccessor.fetchEntries().filter { it.producers.size > PRODUCERS_THRESHOLD }

        if (identifiedProducers.isNotEmpty()) {
            throw IllegalStateException("Entries having more than [$PRODUCERS_THRESHOLD] producers: ${identifiedProducers.map { it.sources }}")
        }

        return true
    }

    companion object {
        private val log by LoggerDelegate()
        private const val STUDIOS_THRESHOLD = 15
        private const val PRODUCERS_THRESHOLD = 30

        /**
         * Singleton of [StudiosAndProducersExtractionChecker]
         * @since 1.0.0
         */
        val instance: StudiosAndProducersExtractionChecker by lazy { StudiosAndProducersExtractionChecker() }
    }
}