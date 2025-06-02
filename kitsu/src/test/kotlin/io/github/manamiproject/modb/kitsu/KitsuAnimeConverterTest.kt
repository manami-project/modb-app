package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS

internal class KitsuAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/title/special_chars.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.title).isEqualTo("Tobidasu PriPara: Mi~nna de Mezase! Idol☆Grand Prix")
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `episodes is null`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/episodes/null.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isZero()
            }
        }

        @Test
        fun `39 episodes`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/episodes/39.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(39)
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is tv`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/tv.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is special`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/special.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is ona`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/ona.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type is ova`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/ova.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type is movie`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/movie.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type is music is mapped to special`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/music.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `posterImage is null`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/picture_and_thumbnail/null.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"))
                assertThat(result.thumbnail).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"))
            }
        }

        @Test
        fun `posterImage is set`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/picture_and_thumbnail/pictures.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture).isEqualTo(URI("https://media.kitsu.app/anime/poster_images/42006/small.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://media.kitsu.app/anime/poster_images/42006/tiny.jpg"))
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `all non main titles and the synonyms are combined`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/synonyms/combine_titles_and_synonyms.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Maho Yome",
                    "MahoYome",
                    "Mahou Tsukai no Yome",
                    "The Ancient Magus' Bride",
                    "The Magician's Bride",
                    "まほよめ",
                    "魔法使いの嫁",
                )
            }
        }

        @Test
        fun `abbreviatedTitles contains null`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/synonyms/abbreviatedTitles_contains_null.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Detective Conan Movie 04: Captured in Her Eyes",
                    "Detective Conan Movie 4",
                    "Meitantei Conan: Hitomi no Naka no Ansatsusha",
                    "瞳の中の暗殺者",
                )
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `build correct source link`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/sources/1517.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.sources).containsExactly(URI("https://kitsu.app/anime/1517"))
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `no adaption, no relations`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/no_adaption_no_relations.json")
                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `no adaption, multiple relations`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/no_adaption_multiple_relations.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://kitsu.app/anime/10761"),
                    URI("https://kitsu.app/anime/12549"),
                    URI("https://kitsu.app/anime/13562"),
                    URI("https://kitsu.app/anime/7742"),
                    URI("https://kitsu.app/anime/7913"),
                    URI("https://kitsu.app/anime/8273"),
                )
            }
        }

        @Test
        fun `one adaption, one relation`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/one_adaption_one_relation.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactly(URI("https://kitsu.app/anime/47280"))
            }
        }

        @Test
        fun `has adaption, multiple relations`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/has_adaption_multiple_relations.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://kitsu.app/anime/13850"),
                    URI("https://kitsu.app/anime/1759"),
                    URI("https://kitsu.app/anime/1921"),
                    URI("https://kitsu.app/anime/2634"),
                    URI("https://kitsu.app/anime/3700"),
                    URI("https://kitsu.app/anime/42535"),
                    URI("https://kitsu.app/anime/5518"),
                    URI("https://kitsu.app/anime/6791"),
                    URI("https://kitsu.app/anime/7627"),
                )
            }
        }

        @Test
        fun `has adaption, no relations`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/has_adaption_but_no_relation.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `no property called 'included'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/no_property_called_included.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `'finished' is mapped to 'FINISHED_AIRING'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/finished.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `'current' is mapped to 'ONGOING'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/current.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `'unreleased' is mapped to 'NOT_YET_AIRED'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/unreleased.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `'upcoming' is mapped to 'NOT_YET_AIRED'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/upcoming.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `'tba' is mapped to 'UNKNOWN'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/tba.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }

        @Test
        fun `null is mapped to 'UNKNOWN'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/null.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
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
        fun `duration is not set and therefore 0`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/null.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `kitsu only uses minutes for duration - 0 implies a duration of less than a minute`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/0.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `duration of 24 minutes`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/24.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(24, MINUTES))
            }
        }

        @Test
        fun `duration of 2 hours`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/120.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(2, HOURS))
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `multiple tags`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/tags/multiple_tags.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "action",
                    "adventure",
                    "bounty hunter",
                    "future",
                    "gunfights",
                    "other planet",
                    "science fiction",
                    "shipboard",
                    "space",
                    "space travel",
                    "comedy",
                    "post apocalypse",
                    "drama",
                    "detective",
                    "sci-fi",
                )
            }
        }

        @Test
        fun `no tags`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/tags/no_tags.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.tags).isEmpty()
            }
        }

        @Test
        fun `no property called 'included'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/tags/no_property_called_included.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.tags).isEmpty()
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `startDate is null`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isZero()
                }
            }

            @Test
            fun `year of premiere is 1989`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/year_of_premiere/1989.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1989)
                }
            }

            @Test
            fun `year has a wrong format`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/invalid_format.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.isYearOfPremiereUnknown())
                    assertThat(result.animeSeason.year).isZero()
                }
            }
        }

        @Nested
        inner class SeasonTests {

            @Test
            fun `season is 'undefined'`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }

            @Test
            fun `season is 'spring'`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/season/spring.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @Test
            fun `season is 'summer'`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/season/summer.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @Test
            fun `season is 'fall'`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/season/fall.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @Test
            fun `season is 'winter'`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/season/winter.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }
        }
    }

    @Nested
    inner class ScoresTests {

        @Test
        fun `successfully load score`() {
            tempDirectory {
                // given
                val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(loadTestResource("KitsuAnimeConverterTest/scores/score.json"))

                // then
                assertThat(result.scores).hasSize(1)
                assertThat(result.scores.first().scaledValue()).isEqualTo(7.897272727272727)
            }
        }

        @Test
        fun `returns NoMetaDataProviderScore`() {
            tempDirectory {
                // given
                val testKitsuConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(loadTestResource("KitsuAnimeConverterTest/scores/no_score.json"))

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }

    @Nested
    inner class StudiosTests {

        @Test
        fun `multiple studios`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/studios/multiple_studios.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "shin-ei animation",
                    "studio pierrot",
                )
            }
        }

        @Test
        fun `no studios`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/studios/no_studios.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.studios).isEmpty()
            }
        }

        @Test
        fun `no property called 'included'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/studios/no_property_called_included.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

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
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/producers/multiple_producers.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "vap",
                    "konami",
                    "nippon television network corporation",
                    "shueisha",
                    "madhouse",
                    "ashi productions",
                )
            }
        }

        @Test
        fun `no producers`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/producers/no_producers.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.producers).isEmpty()
            }
        }

        @Test
        fun `no property called 'included'`() {
            tempDirectory {
                // given
                val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = KitsuConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                }

                val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/producers/no_property_called_included.json")

                val converter = KitsuAnimeConverter(
                    metaDataProviderConfig = testKitsuConfig,
                )

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
            // given
            val previous = KitsuAnimeConverter.instance

            // when
            val result = KitsuAnimeConverter.instance

            // then
            assertThat(result).isExactlyInstanceOf(KitsuAnimeConverter::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}