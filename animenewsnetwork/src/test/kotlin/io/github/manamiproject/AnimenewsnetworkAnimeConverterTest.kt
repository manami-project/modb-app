package io.github.manamiproject

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.HOURS
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class AnimenewsnetworkAnimeConverterTest {

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract id 6592`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/sources/6592.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.sources).containsExactly(URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=6592"))
            }
        }
    }

    @Nested
    inner class TitleTests {

        @Test
        fun `title with special chars`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/title/special_chars.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Pa-Pa-Pa the ★ Movie: Perman")
            }
        }

        @Test
        fun `title with type is reduced to title only`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/title/title_with_type.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Kaminari Boy Pikkaribee")
            }
        }

        @Test
        fun `title with type and number is reduced to title only`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/title/title_with_type_and_number.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Pretty Guardian Sailor Moon Crystal")
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `correctly extract 12 episodes from 'number of episodes'`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/episodes/12.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(12)
            }
        }

        @Test
        fun `'number of episodes' is missing so take the value from 'episode titles' instead`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/episodes/number_of_episodes_missing_use_episodes_titles_instead.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(87)
            }
        }

        @Test
        fun `no indicator for episodes, but 'movie' in title`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/episodes/no_episodes_but_movie_in_title.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isOne()
            }
        }

        @Test
        fun `no indicator for episodes`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/episodes/no_episodes.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isZero()
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `title contains TV in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/tv.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `title contains MOVIE in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/movie.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `title contains OVA in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/ova.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `title contains ONA in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/ona.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `title contains SPECIAL in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/special.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `title contains motion picture`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/motion-picture-in-title.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `title contains ova`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/type/ova-in-title.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }
    }

    @Nested
    inner class ScoreTests {

        @Test
        fun `score exists`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/score/score.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).isNotEmpty()
                assertThat(result.scores.first().hostname).isEqualTo(AnimenewsnetworkConfig.hostname())
                assertThat(result.scores.first().value).isEqualTo(5.947)
            }
        }

        @Test
        fun `no score`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/score/no-score.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `picture and thumbnail`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.thumbnail).isEqualTo(URI("https://cdn.animenewsnetwork.com/thumbnails/fit200x200/encyc/A6074-2020173022.1442237283.jpg"))
                assertThat(result.picture).isEqualTo(URI("https://cdn.animenewsnetwork.com/thumbnails/max500x600/encyc/A6074-2020173022.1442237283.jpg"))
            }
        }

        @Test
        fun `neither picture nor thumbnail`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"))
                assertThat(result.thumbnail).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"))
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `vintage - single date range`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-single-date-range.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2004)
                }
            }

            @Test
            fun `vintage - year`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-year.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1992)
                }
            }

            @Test
            fun `vintage - year-month-day`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-year-month-day.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2023)
                }
            }

            @Test
            fun `vintage - multiple entries`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-multiple-entries-single-date.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2017)
                }
            }

            @Test
            fun `vintage - multiple entries with a range`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-multiple-entries-range.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1997)
                }
            }

            @Test
            fun `vintage - missing`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-missing.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isZero()
                }
            }

            @Test
            fun `vintage - year followed by text`() {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/year/vintage-year-followed-by-text.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2012)
                }
            }
        }

        @Nested
        inner class SeasonTests {

            @ParameterizedTest
            @ValueSource(strings = ["01", "02", "03"])
            fun `mapped to WINTER`(input: String) {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/season/$input.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["04", "05", "06"])
            fun `mapped to SPRING`(input: String) {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/season/$input.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["07", "08", "09"])
            fun `mapped to SUMMER`(input: String) {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/season/$input.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["10", "11", "12"])
            fun `mapped to FALL`(input: String) {
                runTest {
                    // given
                    val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                    }

                    val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                    val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/anime_season/season/$input.html")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `multiple synonyms different languages`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/synonyms/multiple.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Clannad: After Story",
                    "Кланнад. Продолжение истории",
                    "クラナド アフターストーリー",
                    "클라나드 애프터 스토리",
                )
            }
        }

        @Test
        fun `no synonyms`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/synonyms/no-synonyms.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).isEmpty()
            }
        }

        @Test
        fun `synonyms with nested brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/synonyms/nestesd-brackets.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Guerreros Samurai",
                    "I cinque samurai",
                    "Les Samouraïs de l'Eternel",
                    "Los Cinco Samurais",
                    "Ronin Warriors",
                    "Samurai Troopers",
                    "Samurai Warrior",
                    "Samurai Warriors",
                    "Yoroiden Samurai Troopers",
                    "鎧伝サムライトルーパー",
                    "鎧傳",
                    "鎧甲聖鬥士",
                    "魔神坛斗士",
                )
            }
        }

        @Test
        fun `synonyms with multiple meta info brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/synonyms/multiple-meta-info-brackets.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "As aventuras de Tom Sawyer",
                    "De avonturen van Tom Sawyer",
                    "Las Aventuras de Tom Sawyer",
                    "Le avventure di Tom Sawyer",
                    "Les Aventures de Tom Sawyer",
                    "Przygody Tomka Sawyera",
                    "Tom Sawyer",
                    "Tom Sawyer no Bōken",
                    "Tom Sawyers Abenteuer",
                    "Tom Story",
                    "توم سوير",
                    "ماجراهای تام سایر",
                    "トム・ソーヤーの冒険",
                    "湯姆歷險記",
                )
            }
        }

        @Test
        fun `synonym with brackets in title without language info`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/synonyms/synonym-with-brackets-in-title-without-language-info.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Evangelion: 3.0 (-46h) - You Can (Not) Redo",
                )
            }
        }

        @Test
        fun `synonym with brackets in title with language info`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/synonyms/synonym-with-brackets-in-title-with-language-info.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Evangelion 2.0: Break",
                    "Evangelion Shin Gekijōban: Ha",
                    "Evangelion: 2.0 You Can [Not] Advance",
                    "Evangelion: 2.22 Tu (No) Puedes Avanzar",
                    "Evangelion: 2.22 You Can (Not) Advance",
                    "Evangelion:2.22 Você (Não) Pode Avançar",
                    "Rebuild of Evangelion 2.0: Division",
                    "Rebuild of Evangelion: 2.0",
                    "Евангелион 2.22: ты (не) пройдешь",
                    "ヱヴァンゲリヲン新劇場版：破",
                    "福音戰士新劇場版：破",
                    "에반게리온 신극장판 파",
                )
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `neither genres nor themes`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/tags/neither-genres-nor-themes.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).isEmpty()
            }
        }

        @Test
        fun `multiple genres and multiple themes`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/tags/multiple-genres-and-multiple-themes.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "comedy",
                    "drama",
                    "romance",
                    "supernatural",
                    "alternate future",
                    "amnesia",
                    "body switch",
                    "comets",
                    "coming-of-age",
                    "countryside",
                    "iyashikei",
                    "mythology",
                    "natural disaster",
                    "non-linear narrative",
                    "school",
                    "small town",
                    "time travel",
                    "tragedy",
                )
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `no related anime, but relation in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/related_anime/no-related-anime-but-relation-in-brackets.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=7809")
                )
            }
        }

        @Test
        fun `no related anime, no-relation in-brackets, but an adaption`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/related_anime/no-related-anime-no-relation-in-brackets-but-an-adaption.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `related anime`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/related_anime/related-anime.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=33628"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=30277"),
                )
            }
        }

        @Test
        fun `related anime and relation in brackets`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/related_anime/related-anime-and-relation-in-brackets.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=2960"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=14079"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=12180"),
                )
            }
        }

        @Test
        fun `ignore manga in related anime`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/related_anime/manga-in-related-anime.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=20000"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=28763"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=23886"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=24366"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=27622"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=26353"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=26831"),
                    URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=31113"),
                )
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `80 minutes`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/80-minutes.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 80,
                        unit = MINUTES,
                    )
                )
            }
        }

        @Test
        fun `24 minutes per episode`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/24-minutes-per-episode.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 24,
                        unit = MINUTES,
                    )
                )
            }
        }

        @Test
        fun `half hour`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/half-hour.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 30,
                        unit = MINUTES,
                    )
                )
            }
        }

        @Test
        fun `half hour per episode`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/half-hour-per-episode.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 30,
                        unit = MINUTES,
                    )
                )
            }
        }

        @Test
        fun `one hour`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/one-hour.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 1,
                        unit = HOURS,
                    )
                )
            }
        }

        @Test
        fun `one hour per episode`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/one-hour-per-episode.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 1,
                        unit = HOURS,
                    )
                )
            }
        }

        @Test
        fun `numeric with error`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/numeric-with-error.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 111,
                        unit = MINUTES,
                    )
                )
            }
        }

        @Test
        fun `text with error`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/duration/text-with-error.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(
                    Duration(
                        value = 1,
                        unit = HOURS,
                    )
                )
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `vintage - no range, year only - UPCOMING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1990-12-31T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `vintage - no range, year only - ONGOING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1991-01-01T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `vintage - no range, year only - FINISHED`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1992-01-01T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `vintage - no range, year-month - UPCOMING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("2019-11-30T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `vintage - no range, year-month - ONGOING only for first of month`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("2019-12-01T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `vintage - no range, year-month - FINISHED`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("2020-12-02T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `vintage - no range, year-month-day - UPCOMING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1993-05-27T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month-day.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `vintage - no range, year-month-day - ONGOING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1993-05-28T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month-day.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `vintage - no range, year-month-day - FINISHED`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("2019-05-29T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month-day.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `vintage - no range, year-month - FINISHED special case where the suffix contains 'to', but not to set a range`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1990-12-31T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-year-month-special-case.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `vintage - range - UPCOMING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1991-09-20T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-range.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["1991-09-21", "1991-10-21"])
        fun `vintage - range - ONGOING`(input: String) {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("${input}T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-range.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `vintage - range - FINISHED`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1991-10-22T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/vintage-range.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `list - range - UPCOMING`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1972-09-30T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/list-range.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["1972-10-01", "1974-09-29"])
        fun `list - range - ONGOING`(input: String) {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("${input}T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/list-range.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `list - range - FINISHED`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
                }

                val testClock = Clock.fixed(Instant.parse("1974-09-30T16:02:42.00Z"), UTC)

                val converter = AnimenewsnetworkAnimeConverter(
                    testAnimenewsnetworkConfig,
                    clock = testClock,
                )

                val testFile = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/status/list-range.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }
    }

    @Nested
    inner class StudiosTests {

        @Test
        fun `multiple studios`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/studios/multiple_studios.html")

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "satelight",
                    "hornets",
                )
            }
        }

        @Test
        fun `no studios`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/studios/no_studios.html")

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.studios).isEmpty()
            }
        }
    }

    @Nested
    inner class ProducersTests {

        @Test
        fun `multiple producers`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/producers/multiple_producers.html")

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "ca-cygames anime fund",
                    "crunchyroll",
                    "jy animation",
                    "mixer",
                    "movic",
                    "north stars pictures",
                )
            }
        }

        @Test
        fun `no producers`() {
            runTest {
                // given
                val testAnimenewsnetworkConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimenewsnetworkConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimenewsnetworkAnimeConverterTest/producers/no_producers.html")

                val converter = AnimenewsnetworkAnimeConverter(testAnimenewsnetworkConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.producers).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimenewsnetworkAnimeConverter.instance

                // when
                val result = AnimenewsnetworkAnimeConverter.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimenewsnetworkAnimeConverter::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}