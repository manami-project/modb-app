package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.anidb.AnidbEntryStatus.ADDITION_PENDING
import io.github.manamiproject.modb.anidb.AnidbEntryStatus.DELETED
import io.github.manamiproject.modb.anidb.AnidbEntryStatus.EXISTS
import io.github.manamiproject.modb.anidb.AnidbEntryStatus.HENTAI
import io.github.manamiproject.modb.anidb.AnidbEntryStatus.NOT_FOUND
import io.github.manamiproject.modb.anidb.AnidbEntryStatus.UNKNOWN
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import kotlinx.coroutines.withContext

/**
 * Checks the response for indicators of special cases.
 * @since 8.0.0
 * @property responseBody Raw response
 * @property extractor Extractor which retrieves the data from raw data.
 * @property metaDataProviderConfig Configuration for a specific metadata provider.
 */
public class AnidbResponseStatusChecker(
    private val responseBody: String,
    private val extractor: DataExtractor = XmlDataExtractor,
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbConfig,
) {

    private var data: ExtractionResult? = null
    private var status: AnidbEntryStatus? = null

    private suspend fun processApiData() {
        data = extractor.extract(responseBody, mapOf(
            "error" to "//error/text()",
            "anime" to "//anime",
        ))
    }

    private suspend fun processWebViewData() {
        data = extractor.extract(responseBody, mapOf(
            "contentContainer" to "//div[@id='layout-content']//div[@class='container']/text()",
            "title" to "//title/text()",
        ))
    }

    private suspend fun checkAdditionPending() {
        withContext(LIMITED_CPU) {
            if (data == null && metaDataProviderConfig.fileSuffix() == "html") processWebViewData()

            if (metaDataProviderConfig.fileSuffix() == "html") {
                if (data!!.notFound("contentContainer")) status = UNKNOWN
                if (data!!.stringOrDefault("contentContainer").startsWith("A request for the addition of this title to AniDB is currently")) status = ADDITION_PENDING
            }
        }
    }

    public suspend fun checkDeleted() {
        withContext(LIMITED_CPU) {
            if (data == null && metaDataProviderConfig.fileSuffix() == "html") processWebViewData()

            if (metaDataProviderConfig.fileSuffix() == "html") {
                if (data!!.notFound("contentContainer")) status = UNKNOWN
                if (data!!.stringOrDefault("contentContainer").startsWith("Unknown anime id.")) status = DELETED
            }
        }
    }

    @Deprecated("Will be removed in a future release, because adult entries will be added to the dataset.")
    private suspend fun checkHentai() {
        withContext(LIMITED_CPU) {
            if (data == null && metaDataProviderConfig.fileSuffix() == "html") processWebViewData()

            if (metaDataProviderConfig.fileSuffix() == "html") {
                if (data!!.notFound("contentContainer")) status = UNKNOWN
                if (data!!.stringOrDefault("contentContainer").startsWith("This anime is marked as 18+ restricted")) status = HENTAI
            }
        }
    }

    private suspend fun checkNotFound() {
        withContext(LIMITED_CPU) {
            if (data == null && metaDataProviderConfig.fileSuffix() == "xml") processApiData()

            if (metaDataProviderConfig.fileSuffix() == "xml") {
                if (data!!.notFound("error") && data!!.notFound("anime")) status = UNKNOWN
                if (data!!.stringOrDefault("error") == "Anime not found") status = NOT_FOUND
            }
        }
    }

    private suspend fun checkExists() {
        withContext(LIMITED_CPU) {
            if (data == null && metaDataProviderConfig.fileSuffix() == "xml") processApiData()

            if (metaDataProviderConfig.fileSuffix() == "xml") {
                if (data!!.notFound("anime") && data!!.notFound("error")) status = UNKNOWN
                if (!data!!.notFound("anime")) status = EXISTS
            }
        }
    }

    public suspend fun checkStatus(): AnidbEntryStatus {
        if (status != null) return status!!
        checkExists()
        if (status == null) checkNotFound() else return status!!
        if (status == null) checkHentai() else return status!!
        if (status == null) checkDeleted() else return status!!
        if (status == null) checkAdditionPending() else return status!!
        if (status == null) status = UNKNOWN
        return status!!
    }
}

/**
 * @since 8.0.0
 */
public enum class AnidbEntryStatus {
    /**
     * @since 8.0.0
     */
    UNKNOWN,

    /**
     * @since 8.0.0
     */
    EXISTS,

    /**
     * @since 8.0.0
     */
    NOT_FOUND,

    /**
     * @since 8.0.0
     */
    ADDITION_PENDING,

    /**
     * @since 8.0.0
     */
    DELETED,

    /**
     * @since 8.0.0
     */
    @Deprecated("Will be removed in a future release, because adult entries will be added to the dataset.")
    HENTAI;
}