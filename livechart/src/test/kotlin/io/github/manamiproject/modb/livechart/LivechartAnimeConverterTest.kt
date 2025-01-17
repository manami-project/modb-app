package io.github.manamiproject.modb.livechart

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.Anime.Type.UNKNOWN
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class LivechartAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/title/special_chars.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Alice Gear Aegis: Doki! Actress Darake no Mermaid Grand Prix ♡")
            }
        }

        @Test
        fun `title containing encoded special chars`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/title/encoded_special_char.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Love Live! School Idol Project: μ's →NEXT LoveLive! 2014 - Endless Parade Encore Animation")
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `no synonyms`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/no_synonyms.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).isEmpty()
            }
        }

        @Test
        fun `one synonym`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/one_synonym.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactly("劇場版 少女☆歌劇 レヴュースタァライト ロンド・ロンド・ロンド")
            }
        }

        @Test
        fun `multiple synonyms`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/multiple_synonyms.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Angels of Death",
                    "殺戮の天使",
                )
            }
        }

        @Test
        fun `synonym containing encoded special char`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/encoded_special_chars.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactly("ラブライブ! μ's Final LoveLive! オープニングアニメーション")
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `unknown number of episodes`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/unknown.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isZero()
            }
        }

        @Test
        fun `1 episode`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/1.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isOne()
            }
        }

        @Test
        fun `10 episodes`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/10.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(10)
            }
        }

        @Test
        fun `100 episodes`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/100.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(100)
            }
        }

        @Test
        fun `number episodes unknown, but currently running`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/number_of_episodes_unknown_but_currently_running.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(1093)
            }
        }

        @Test
        fun `number episodes known and currently running`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/number_of_episodes_known_and_running.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(12)
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract id 3437`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/sources/3437.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.sources).containsExactly(URI("https://livechart.me/anime/3437"))
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `neither picture nor thumbnail`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/picture_and_thumbnail/neither_picture_nor_thumbnail.html")


                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"))
                assertThat(result.thumbnail).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"))
            }
        }

        @Test
        fun `picture and thumbnail`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.html")


                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://u.livechart.me/anime/3437/poster_image/ea9acd1ccea844fd9c4debde5e8e631e.png/large.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://u.livechart.me/anime/3437/poster_image/ea9acd1ccea844fd9c4debde5e8e631e.png/small.jpg"))
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is TV`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/tv.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `Unknown is mapped to 'UNKNOWN'`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/unknown.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(UNKNOWN)
            }
        }
        
        @Test
        fun `type 'tv short' is mapped to 'TV'`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/tv_short.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }
        
        @Test
        fun `type is Movie`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/movie.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type 'web short' is mapped to 'ONA'`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/web_short.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type 'web' is mapped to 'ONA'`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/web.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type is OVA`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/ova.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type is Special`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/special.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `1 hr`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/1_hour.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, HOURS))
            }
        }

        @Test
        fun `1 hr 11 min`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/1_hour_11_min.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(71, MINUTES))
            }
        }

        @Test
        fun `2 hr`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/2_hours.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(2, HOURS))
            }
        }

        @Test
        fun `2 hr 15 min`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/2_hours_15_minutes.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(135, MINUTES))
            }
        }

        @Test
        fun `10 min`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/10_min.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(10, MINUTES))
            }
        }

        @Test
        fun `30 sec`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/30_sec.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(30, SECONDS))
            }
        }

        @Test
        fun `unknown duration`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/unknown.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `extract multiple tags`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/tags/multiple_tags.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "comedy",
                    "fantasy",
                    "mythology",
                    "romantic subtext",
                    "supernatural",
                    "workplace",
                    "sequel",
                )
            }
        }

        @Test
        fun `extract exactly one tag`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/tags/one_tag.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactly("cgi")
            }
        }

        @Test
        fun `no tags available`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/tags/no_tags.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).isEmpty()
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class SeasonTests {

            @Test
            fun `season is 'UNDEFINED'`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/undefined.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }

            @Test
            fun `season is 'FALL'`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/fall.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @Test
            fun `season is 'SPRING'`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/spring.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @Test
            fun `season is 'SUMMER'`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/summer.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @Test
            fun `season is 'WINTER'`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/winter.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }

            @Test
            fun `season is 'UNDEFINED' because there is no element containing season info`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/no_season_element.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }
        }

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `season set completely`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/season_set.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2023)
                }
            }

            @Test
            fun `season is not set, but premiere`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/season_tba_premiere_set.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2023)
                }
            }

            @Test
            fun `both season and premiere are not set`() {
                runBlocking {
                    // given
                    val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/season_tba_premiere_not_set.html")

                    val converter = LivechartAnimeConverter(
                        metaDataProviderConfig = testLivechartConfig,
                    )

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isZero()
                }
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `status is ongoing`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/status/ongoing.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `status is upcoming`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/status/no_yet_released.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `status is finished`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/status/finished.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `no related anime`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/related_anime/no_relations.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `one related anime`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/related_anime/one_relation.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactly(
                    URI("https://livechart.me/anime/3808"),
                )
            }
        }

        @Test
        fun `multiple related anime no scrolling`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/related_anime/multiple_relations_no_scrolling.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://livechart.me/anime/3367"),
                    URI("https://livechart.me/anime/3369"),
                    URI("https://livechart.me/anime/3368"),
                )
            }
        }

        @Test
        fun `multiple related anime with scrolling`() {
            runBlocking {
                // given
                val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = LivechartConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/related_anime/multiple_relations_with_scrolling.html")

                val converter = LivechartAnimeConverter(
                    metaDataProviderConfig = testLivechartConfig,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://livechart.me/anime/3662"),
                    URI("https://livechart.me/anime/6221"),
                    URI("https://livechart.me/anime/3767"),
                    URI("https://livechart.me/anime/7353"),
                    URI("https://livechart.me/anime/3782"),
                    URI("https://livechart.me/anime/7133"),
                    URI("https://livechart.me/anime/4823"),
                    URI("https://livechart.me/anime/7289"),
                    URI("https://livechart.me/anime/6604"),
                    URI("https://livechart.me/anime/5530"),
                    URI("https://livechart.me/anime/3984"),
                    URI("https://livechart.me/anime/5793"),
                    URI("https://livechart.me/anime/7156"),
                    URI("https://livechart.me/anime/4054"),
                    URI("https://livechart.me/anime/6022"),
                )
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = LivechartAnimeConverter.instance

            // when
            val result = LivechartAnimeConverter.instance

            // then
            assertThat(result).isExactlyInstanceOf(LivechartAnimeConverter::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}