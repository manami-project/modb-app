package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank

/**
 * [LastPageMemorizer] which uses [Int] to identify pages.
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 */
class IntegerBasedLastPageMemorizer(
    appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig,
): LastPageMemorizer<Int> {

    private val stringBasedLastPageMemorizer = StringBasedLastPageMemorizer(appConfig, metaDataProviderConfig)

    override suspend fun memorizeLastPage(page: Int) = stringBasedLastPageMemorizer.memorizeLastPage(page.toString())

    override suspend fun retrieveLastPage(): Int {
        val value = stringBasedLastPageMemorizer.retrieveLastPage()

        return if (value.neitherNullNorBlank()) {
            Regex("\\d+").find(value)?.value?.toInt() ?: throw IllegalStateException("Unable to retrieve last page for [${metaDataProviderConfig.hostname()}]")
        } else {
            1
        }
    }
}