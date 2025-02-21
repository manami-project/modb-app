package io.github.manamiproject.modb.animeplanet

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.HOURS
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.NoMetaDataProviderScore
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class AnimePlanetAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/title/special_chars.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.title).isEqualTo("PriPara Movie: Minna no Akogare♪ Let's Go☆Prix Paris")
            }
        }

        @Test
        fun `extract title from jsonld if it was replaces by 'email protected'`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/title/title_replaced_by_email_protected.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.title).isEqualTo("Malice@Doll")
            }
        }

        @Test
        fun `extract title from meta tag if it was replaces by 'email protected' and jsonld does not exist`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/title/title_replaced_by_email_protected_no_jsonld.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.title).isEqualTo("The iDOLM@STER Million Live!")
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `1 episode`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/episodes/1_ep.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isOne()
            }
        }

        @Test
        fun `1081 and more episodes`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/episodes/1081+_eps.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(1081)
            }
        }

        @Test
        fun `25 episodes`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/episodes/25_eps.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(25)
            }
        }

        @Test
        fun `no episodes is mapped to 1 episode`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/episodes/no_episodes.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isZero()
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `'web' is mapped to ONA`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/web.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `'TV Special' is mapped to Special`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/tv_special.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `'TV' is mapped to TV`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/tv.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `'OVA' is mapped to OVA`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/ova.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `'other' is mapped to Special`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/other.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `'music video' is mapped to Special`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/music_video.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `'movie' is mapped to Movie`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/movie.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `'dvd special' is mapped to Special`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/dvd_special.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `value without braces is extracted correctly`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/type/no_braces.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `correctly extract picture and thumbnail`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture.toString()).isEqualTo("https://cdn.anime-planet.com/anime/primary/natsumes-book-of-friends-season-6-specials-1.jpg")
                assertThat(result.thumbnail.toString()).isEqualTo("https://cdn.anime-planet.com/anime/primary/natsumes-book-of-friends-season-6-specials-1-190x242.jpg")
            }
        }

        @Test
        fun `neither picture nor thumbnail`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture.toString()).isEqualTo("https://cdn.anime-planet.com/images/anime/default/default-anime-spring.png")
                assertThat(result.thumbnail.toString()).isEqualTo("https://cdn.anime-planet.com/images/anime/default/default-anime-spring.png")
            }
        }

        @Test
        fun `ensure that the primary screenshot is extracted as picture`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/picture_and_thumbnail/ensure_primary_screenshot_as_picture.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture.toString()).isEqualTo("https://cdn.anime-planet.com/anime/primary/ado-usseewa-1.webp")
                assertThat(result.thumbnail.toString()).isEqualTo("https://cdn.anime-planet.com/anime/primary/ado-usseewa-1-285x399.webp")
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `'tba' is mapped to UPCOMING`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/status/tba.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `current year is mapped to UNKNOWN, because it is not possible to determine whether the anime has finished or is still running`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/status/year_is_current_year.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2021, 7, 9).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }

        @Test
        fun `year in the future is mapped to UPCOMING`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/status/year_in_the_future.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2021, 7, 9).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `year in the past is mapped to FINISHED`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/status/year_in_the_past.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2021, 7, 9).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `range starting in the past is mapped to ONGOING`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/status/range_to_unknown_-_starts_in_the_past.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2023, 4, 28).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `range starting in the same year is mapped to UNKNOWN, because it is not possible to determine whether the anime has already started or not`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/status/range_to_unknown_-_starts_same_year.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2023, 4, 28).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }

        @Test
        fun `range ending in the past`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/status/range_-_ends_in_the_past.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2021, 7, 9).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `range ending same year`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/status/range_-_ends_same_year.html")

                val converter = AnimePlanetAnimeConverter(
                    metaDataProviderConfig = testAnimePlanetConfig,
                    clock = Clock.fixed(LocalDate.of(2021, 7, 9).atStartOfDay().toInstant(UTC), UTC)
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `1 hour`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/duration/1_hour.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, HOURS))
            }
        }

        @Test
        fun `1 min`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/duration/1_min.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, MINUTES))
            }
        }

        @Test
        fun `2 hours`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/duration/2_hours.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(2, HOURS))
            }
        }

        @Test
        fun `2 hours 15 minutes`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/duration/2_hours_15_min.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(135, MINUTES))
            }
        }

        @Test
        fun `10 minutes`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/duration/10_min.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(10, MINUTES))
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Test
        fun `season is undefined, but year is set`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/anime_season/year_only.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason).isEqualTo(AnimeSeason(UNDEFINED, 2016))
            }
        }

        @Test
        fun `season is summer`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/anime_season/summer.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason).isEqualTo(AnimeSeason(SUMMER, 2020))
            }
        }

        @Test
        fun `season is spring`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/anime_season/spring.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason).isEqualTo(AnimeSeason(SPRING, 2020))
            }
        }

        @Test
        fun `season is winter`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/anime_season/winter.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason).isEqualTo(AnimeSeason(WINTER, 2020))
            }
        }

        @Test
        fun `season is fall`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/anime_season/fall.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason).isEqualTo(AnimeSeason(FALL, 2020))
            }
        }

        @Test
        fun `tba - no year, no season`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/anime_season/tba.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason).isEqualTo(AnimeSeason(UNDEFINED, 0))
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract url slug as id`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/sources/id.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.sources).containsExactly(URI("https://anime-planet.com/anime/code-geass-lelouch-of-the-rebellion-r2"))
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `successfully extract the alternative title`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/synonyms/synonyms_available.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).containsExactly("Kimi no Na wa.")
            }
        }

        @Test
        fun `alternative title is not available`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/synonyms/synonyms_not_available.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).isEmpty()
            }
        }

        @Test
        fun `extract synonym from jsonld if it was replaces by 'email protected'`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/synonyms/synonyms_replaced_by_email_protected.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).containsExactly("GJ Club@")
            }
        }

        @Test
        fun `extract multiple synonyms`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/synonyms/multiple_synonyms.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "\"Space Battleship Yamato\" Era: The Choice in 2202",
                    "\"Uchuu Senkan Yamato\" to Iu Jidai: Seireki 2202-nen no Sentaku",
                )
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `extract same franchise and other franchise`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent =
                    loadTestResource<String>("AnimePlanetAnimeConverterTest/related_anime/same_franchise_and_other_franchise.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://anime-planet.com/anime/fullmetal-alchemist"),
                    URI("https://anime-planet.com/anime/fullmetal-alchemist-brotherhood-4-koma-theater"),
                    URI("https://anime-planet.com/anime/fullmetal-alchemist-brotherhood-specials"),
                    URI("https://anime-planet.com/anime/fullmetal-alchemist-premium-collection"),
                    URI("https://anime-planet.com/anime/fullmetal-alchemist-reflections"),
                    URI("https://anime-planet.com/anime/fullmetal-alchemist-the-movie-conqueror-of-shamballa"),
                    URI("https://anime-planet.com/anime/fullmetal-alchemist-the-sacred-star-of-milos"),
                )
            }
        }

        @Test
        fun `extract related anime if there are only same franchise entries`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/related_anime/same_franchise_only.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://anime-planet.com/anime/demon-slayer-kimetsu-no-yaiba-entertainment-district-arc"),
                    URI("https://anime-planet.com/anime/demon-slayer-kimetsu-no-yaiba-movie-mugen-train"),
                    URI("https://anime-planet.com/anime/demon-slayer-kimetsu-no-yaiba-mugen-train"),
                    URI("https://anime-planet.com/anime/demon-slayer-kimetsu-no-yaiba-swordsmith-village-arc"),
                    URI("https://anime-planet.com/anime/kimetsu-gakuen-monogatari-kimetsu-no-utage-tokubetsu-hen"),
                    URI("https://anime-planet.com/anime/kimetsu-gakuen-valentine-hen"),
                )
            }
        }

        @Test
        fun `no related anime`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/related_anime/no_related_anime.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `successfully extract the tags`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/tags/various_tags.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "based on a manga",
                    "child protagonists",
                    "dark fantasy",
                    "fantasy",
                    "horror",
                    "isolated society",
                    "mind games",
                    "mystery",
                    "noitamina",
                    "orphanage",
                    "orphans",
                    "outside world",
                    "psychological",
                    "sci fi",
                    "shounen",
                    "thriller",
                    "domestic abuse",
                    "self-harm",
                    "mature themes",
                )
            }
        }

        @Test
        fun `tags not available`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val testFileContent = loadTestResource<String>("AnimePlanetAnimeConverterTest/tags/no_tags.html")

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.tags).isEmpty()
            }
        }
    }

    @Nested
    inner class ScoresTests {

        @Test
        fun `successfully load score`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(loadTestResource("AnimePlanetAnimeConverterTest/scores/score.html"))

                // then
                assertThat(result.scores).hasSize(1)
                assertThat((result.scores.first()).scaledValue()).isEqualTo(8.844)
            }
        }

        @Test
        fun `returns NoMetaDataProviderScore`() {
            runBlocking {
                // given
                val testAnimePlanetConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnimePlanetConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                }

                val converter = AnimePlanetAnimeConverter(testAnimePlanetConfig)

                // when
                val result = converter.convert(loadTestResource("AnimePlanetAnimeConverterTest/scores/no-score.html"))

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimePlanetAnimeConverter.instance

                // when
                val result = AnimePlanetAnimeConverter.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimePlanetAnimeConverter::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}