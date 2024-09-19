package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DefaultHighestIdAlreadyDownloadedDetectorTest {

    @Nested
    inner class DetectHighestIdAlreadyDownloadedTests {

        @Test
        fun `return zero if there are no anime`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = emptyList()
                }

                val defaultHighestIdAlreadyDownloadedDetector = DefaultHighestIdAlreadyDownloadedDetector(
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultHighestIdAlreadyDownloadedDetector.detectHighestIdAlreadyDownloaded(testMetaDataProviderConfig)

                // then
                assertThat(result).isZero()
            }
        }

        @Test
        fun `return zero if the meta data provider doesn't use integer for anime IDs`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = listOf(
                        Anime(
                            _title = "test 1",
                            sources = hashSetOf(
                                URI("https://example.org/anime/title-of-anime-1"),
                            ),
                        ),
                        Anime(
                            _title = "test 2",
                            sources = hashSetOf(
                                URI("https://example.org/anime/title-of-anime-2"),
                            ),
                        ),
                    )
                }

                val defaultHighestIdAlreadyDownloadedDetector = DefaultHighestIdAlreadyDownloadedDetector(
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultHighestIdAlreadyDownloadedDetector.detectHighestIdAlreadyDownloaded(testMetaDataProviderConfig)

                // then
                assertThat(result).isZero()
            }
        }

        @Test
        fun `correctly returns highest ID`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = listOf(
                        Anime(
                            _title = "test 1",
                            sources = hashSetOf(
                                URI("https://example.org/anime/1"),
                            ),
                        ),
                        Anime(
                            _title = "test 4",
                            sources = hashSetOf(
                                URI("https://example.org/anime/4"),
                            ),
                        ),
                        Anime(
                            _title = "test 2",
                            sources = hashSetOf(
                                URI("https://example.org/anime/2"),
                            ),
                        ),
                        Anime(
                            _title = "test 3",
                            sources = hashSetOf(
                                URI("https://example.org/3"),
                            ),
                        ),
                    )
                }

                val defaultHighestIdAlreadyDownloadedDetector = DefaultHighestIdAlreadyDownloadedDetector(
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultHighestIdAlreadyDownloadedDetector.detectHighestIdAlreadyDownloaded(testMetaDataProviderConfig)

                // then
                assertThat(result).isEqualTo(4)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultHighestIdAlreadyDownloadedDetector.instance

                // when
                val result = DefaultHighestIdAlreadyDownloadedDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultHighestIdAlreadyDownloadedDetector::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}