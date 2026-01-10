package io.github.manamiproject.modb.app.crawlers

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.extensions.writeToFile
import kotlinx.coroutines.withContext
import kotlin.io.path.readLines

/**
 * [LastPageMemorizer] which uses [String] to identify pages.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific metadata provider.
 */
class StringBasedLastPageMemorizer(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig,
): LastPageMemorizer<String> {

    override suspend fun memorizeLastPage(page: String) {
        page.writeToFile(appConfig.workingDir(metaDataProviderConfig).resolve(LAST_PAGE_MEMORIZER_FILE_NAME))
    }

    override suspend fun retrieveLastPage(): String = withContext(LIMITED_FS) {
        val file = appConfig.workingDir(metaDataProviderConfig).resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

        return@withContext if (file.regularFileExists()) {
            file.readLines().first().trim()
        } else {
            EMPTY
        }
    }
}