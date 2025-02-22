package io.github.manamiproject.modb.notify

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS

internal class NotifyAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/title/special_chars.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Tobidasu PriPara: Mi~nna de Mezase! Idol☆Grand Prix")
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is TV`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/tv.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is Movie`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/movie.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type is ONA`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/ona.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type is OVA`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/ova.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type is Special`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/special.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `'music' is mapped to Special`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/music.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `39 episodes`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/episodes/39.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(39)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `picture and thumbnail`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://media.notify.moe/images/anime/large/IkCdhKimR.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://media.notify.moe/images/anime/small/IkCdhKimR.jpg"))
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `all non main titles and the synonyms are combined`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/synonyms/combine_non_canonical_title_and_synonyms.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Maho Yome",
                    "MahoYome",
                    "Mahoutsukai no Yome",
                    "The Ancient Magus' Bride",
                    "The Magician's Bride",
                    "まほよめ",
                    "魔法使いの嫁",
                )
            }
        }

        @Test
        fun `synonyms is null`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/synonyms/synonyms_is_null.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactly("こぶとり")
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract id 0-A-5Fimg`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/sources/0-A-5Fimg.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.sources).containsExactly(URI("https://notify.moe/anime/0-A-5Fimg"))
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `no relations file`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val relationsDir = tempDir.resolve("relations").createDirectory()

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = relationsDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/no_relations_file/anime.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `multiple relations`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val relationsDir = tempDir.resolve("relations").createDirectory()

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = relationsDir,
                )

                val srcFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/multiple_relations/anime.json")

                testResource("NotifyAnimeConverterTest/related_anime/multiple_relations/relations.json")
                    .copyTo(relationsDir.resolve("uLs5tKiig.${NotifyRelationsConfig.fileSuffix()}"))

                // when
                val result = converter.convert(srcFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://notify.moe/anime/CsBopKmmR"),
                    URI("https://notify.moe/anime/I2ihtKimg"),
                    URI("https://notify.moe/anime/T7qwpKmig"),
                    URI("https://notify.moe/anime/U66k2FimR"),
                    URI("https://notify.moe/anime/eav4hFmiR"),
                    URI("https://notify.moe/anime/vvIppKiiR"),
                )
            }
        }

        @Test
        fun `no relations`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val relationsDir = tempDir.resolve("relations").createDirectory()

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = relationsDir,
                )

                val srcFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/no_relations/anime.json")

                testResource("NotifyAnimeConverterTest/related_anime/no_relations/relations.json")
                    .copyTo(relationsDir.resolve("uLs5tKiig.${NotifyRelationsConfig.fileSuffix()}"))

                // when
                val result = converter.convert(srcFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `items in relations file is null`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val relationsDir = tempDir.resolve("relations").createDirectory()

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = relationsDir,
                )

                val srcFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/items_in_relations_file_is_null/anime.json")

                testResource("NotifyAnimeConverterTest/related_anime/items_in_relations_file_is_null/relations.json")
                    .copyTo(relationsDir.resolve("--eZhFiig.${NotifyRelationsConfig.fileSuffix()}"))

                // when
                val result = converter.convert(srcFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `'current' is mapped to 'ONGOING'`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/current.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `'finished' is mapped to 'FINISHED'`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/finished.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `'upcoming' is mapped to 'UPCOMING'`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/upcoming.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `'tba' is mapped to 'UNKNOWN'`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/tba.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }
    }

    @Nested
    inner class TagTests {

        @Test
        fun `put genres into tags`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/tags/tags_from_genres.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "comedy",
                    "drama",
                    "ecchi",
                    "romance",
                )
            }
        }

        @Test
        fun `genres is null`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/tags/genres_is_null.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).isEmpty()
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `notify only uses minutes for duration - 0 implies a duration of less than a minute or duration is unknown`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/duration/0.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `duration of 24 minutes`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/duration/24.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(24, MINUTES))
            }
        }

        @Test
        fun `duration of 2 hours`() {
            tempDirectory {
                // given
                val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = NotifyConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                }

                val converter = NotifyAnimeConverter(
                    metaDataProviderConfig = testNotifyConfig,
                    relationsDir = tempDir,
                )

                val testFile = loadTestResource<String>("NotifyAnimeConverterTest/duration/120.json")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(2, HOURS))
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `no year`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/no_year.json")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.isYearOfPremiereUnknown()).isTrue()
                }
            }

            @Test
            fun `year of premiere is 1989`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/1989.json")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1989)
                }
            }

            @Test
            fun `season is 'undefined'`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/undefined.json")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }

            @Test
            fun `season is 'spring'`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/spring.json")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @Test
            fun `season is 'summer'`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/summer.json")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @Test
            fun `season is 'fall'`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/fall.json")

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @Test
            fun `season is 'winter'`() {
                tempDirectory {
                    // given
                    val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = NotifyConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
                    }

                    val converter = NotifyAnimeConverter(
                        metaDataProviderConfig = testNotifyConfig,
                        relationsDir = tempDir,
                    )

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/winter.json")

                    // when
                    val result = converter.convert(testFile)

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
                val converter = NotifyAnimeConverter(relationsDir = tempDir)


                // when
                val result = converter.convert(loadTestResource("NotifyAnimeConverterTest/scores/score.json"))

                // then
                assertThat(result.scores).hasSize(1)
                assertThat(result.scores.first().scaledValue()).isEqualTo(8.62030369826108)
            }
        }

        @Test
        fun `returns NoMetaDataProviderScore`() {
            tempDirectory {
                // given
                val converter = NotifyAnimeConverter(relationsDir = tempDir)

                // when
                val result = converter.convert(loadTestResource("NotifyAnimeConverterTest/scores/no-score.json"))

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }


    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if the given path for relationsDir is not a directory`() {
            tempDirectory {
                // given
                val relationsDir = tempDir.resolve("relations").createFile()

                // when
                val result = assertThrows<IllegalArgumentException> {
                    NotifyAnimeConverter(
                        metaDataProviderConfig = TestMetaDataProviderConfig,
                        relationsDir = relationsDir,
                    )
                }

                // then
                assertThat(result).hasMessage("Directory for relations [$relationsDir] does not exist or is not a directory.")
            }
        }

        @Test
        fun `throws exception if the given path for relationsDir does not exist`() {
            tempDirectory {
                // given
                val relationsDir = tempDir.resolve("relations")

                // when
                val result = assertThrows<IllegalArgumentException> {
                    NotifyAnimeConverter(
                        metaDataProviderConfig = TestMetaDataProviderConfig,
                        relationsDir = relationsDir,
                    )
                }

                // then
                assertThat(result).hasMessage("Directory for relations [$relationsDir] does not exist or is not a directory.")
            }
        }
    }
}