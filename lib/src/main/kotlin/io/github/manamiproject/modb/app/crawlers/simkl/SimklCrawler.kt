package io.github.manamiproject.modb.app.crawlers.simkl

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.crawlers.IntegerBasedLastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.YEAR_OF_THE_FIRST_ANIME
import io.github.manamiproject.modb.core.random
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.simkl.SimklDownloader
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

class SimklCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = SimklConfig,
    private val paginationConfig: MetaDataProviderConfig = SimklPaginationIdRangeSelectorConfig,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val lastPageMemorizer: LastPageMemorizer<Int> = IntegerBasedLastPageMemorizer(metaDataProviderConfig = metaDataProviderConfig),
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
    private val downloader: Downloader = SimklDownloader(httpClient = SuspendableHttpClient()),
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val xmlDataExtractor: DataExtractor = XmlDataExtractor,
): Crawler {

    private val entriesNotScheduledForCurrentWeek = hashSetOf<AnimeId>()

    override suspend fun start() {
        log.info { "Starting crawler for [${metaDataProviderConfig.hostname()}]." }

        if (entriesNotScheduledForCurrentWeek.isEmpty()) {
            entriesNotScheduledForCurrentWeek.addAll(downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig))
        }

        downloadEntriesScheduledForCurrentWeek()
        wait()
        downloadEntriesUsingPagination()

        log.info { "Finished crawling data for [${metaDataProviderConfig.hostname()}]." }
    }

    private suspend fun downloadEntriesScheduledForCurrentWeek() {
        log.info { "Downloading [${metaDataProviderConfig.hostname()}] entries scheduled for the current week." }

        val ids = downloadControlStateScheduler.findEntriesScheduledForCurrentWeek(metaDataProviderConfig) - alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig)
        startDownload(ids.toList().createShuffledList())

        log.info { "Finished downloading [${metaDataProviderConfig.hostname()}] entries scheduled for the current week." }
    }

    private suspend fun downloadEntriesUsingPagination() {
        log.info { "Downloading [${metaDataProviderConfig.hostname()}] entries using pagination." }

        val startYear = lastPageMemorizer.retrieveLastPage().let {
            when {
                it == 1 -> YEAR_OF_THE_FIRST_ANIME
                else -> it
            }
        }
        val maxYear = LocalDate.now(appConfig.clock()).year + 2

        for (currentYear in startYear..maxYear) {
            for (currentOffset in 0..Int.MAX_VALUE step 20) {
                log.debug { "Checking year [$currentYear] with offset [$currentOffset] for [${metaDataProviderConfig.hostname()}]." }
                val (currentDownloadList, skipToNextYear) = extractIds(year = currentYear, offset = currentOffset)
                if (skipToNextYear) {
                    break
                }
                startDownload(currentDownloadList)
            }
            lastPageMemorizer.memorizeLastPage(currentYear)
        }

        log.info { "Finished downloading [${metaDataProviderConfig.hostname()}] entries using pagination." }
    }

    private suspend fun extractIds(year: Int, offset: Int): Pair<List<AnimeId>, Boolean> = withContext(LIMITED_NETWORK) {
        wait()

        val response = httpClient.post(
            url = paginationConfig.buildDataDownloadLink().toURL(),
            requestBody = RequestBody(
                mediaType = "application/x-www-form-urlencoded; charset=UTF-8",
                body = "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F$year%2Fa-z&afilt_tv=&offset=$offset&async=true&double=0",
            ),
            headers = mapOf(
                "X-Requested-With" to listOf("XMLHttpRequest"),
                "Sec-GPC" to listOf("1"),
                "Referer" to listOf("https://${metaDataProviderConfig.hostname()}/anime/all/$year/a-z/"),
                "Sec-Fetch-Dest" to listOf("empty"),
                "Sec-Fetch-Mode" to listOf("cors"),
                "Sec-Fetch-Site" to listOf("same-origin"),
            ),
        )

        check(response.isOk()) { "Response code is not 200." }
        val skipToNextYear = response.bodyAsText == NO_MORE_CONTENT_FOR_OFFSET || response.bodyAsText == EMPTY

        val data = xmlDataExtractor.extract(
            response.bodyAsText, mapOf(
                "animeList" to "//div[@class='SimklTVPosterImage']/a/@href",
            )
        )

        val entriesOnThePage = when {
            data.notFound("animeList") -> hashSetOf()
            else -> data.listNotNull<AnimeId>("animeList") {
                it.remove("/anime/").substringBefore('/')
            }.toHashSet()
        }

        entriesOnThePage.removeAll(entriesNotScheduledForCurrentWeek)
        entriesOnThePage.removeAll(alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig))
        return@withContext entriesOnThePage.toList().createShuffledList() to skipToNextYear
    }

    private suspend fun startDownload(idDownloadList: List<String>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [simklId=$animeId]" }

        val response = downloader.download(animeId) {
            deadEntriesAccessor.addDeadEntry(it, metaDataProviderConfig)
        }

        if (response.neitherNullNorBlank()) {
            response.writeToFile(file, true)
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(1000, 1200).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()
        private const val NO_MORE_CONTENT_FOR_OFFSET = "<div style=\"height: 500px\"></div>"

        /**
         * Singleton of [SimklCrawler]
         * @since 1.0.0
         */
        val instance: SimklCrawler by lazy { SimklCrawler() }
    }
}