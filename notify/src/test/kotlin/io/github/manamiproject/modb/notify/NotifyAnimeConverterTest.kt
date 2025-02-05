package io.github.manamiproject.modb.notify

import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

internal class NotifyAnimeConverterTest {

    @Nested
    inner class AnimeTests {

        @Nested
        inner class TitleTests {

            @Test
            fun `title containing special chars`() {
                tempDirectory { 
                    // given
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/title/special_chars.json")
    
                    val converter = NotifyAnimeConverter(relationsDir = tempDir)
    
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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/tv.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/movie.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/ona.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/ova.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/special.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/type/music.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/episodes/39.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/synonyms/combine_non_canonical_title_and_synonyms.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/synonyms/synonyms_is_null.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/sources/0-A-5Fimg.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val relationsDir = tempDir.resolve("relations").createDirectory()

                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/no_relations_file/anime.json")

                    val converter = NotifyAnimeConverter(relationsDir =  relationsDir)

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
                    val relationsDir = tempDir.resolve("relations").createDirectory()

                    val srcFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/multiple_relations/anime.json")

                    testResource("NotifyAnimeConverterTest/related_anime/multiple_relations/relations.json")
                        .copyTo(relationsDir.resolve("uLs5tKiig.${NotifyRelationsConfig.fileSuffix()}"))

                    val converter = NotifyAnimeConverter(relationsDir =  relationsDir)

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
                    val relationsDir = tempDir.resolve("relations").createDirectory()

                    val srcFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/no_relations/anime.json")

                    testResource("NotifyAnimeConverterTest/related_anime/no_relations/relations.json")
                        .copyTo(relationsDir.resolve("uLs5tKiig.${NotifyRelationsConfig.fileSuffix()}"))

                    val converter = NotifyAnimeConverter(relationsDir =  relationsDir)

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
                    val relationsDir = tempDir.resolve("relations").createDirectory()

                    val srcFile = loadTestResource<String>("NotifyAnimeConverterTest/related_anime/items_in_relations_file_is_null/anime.json")

                    testResource("NotifyAnimeConverterTest/related_anime/items_in_relations_file_is_null/relations.json")
                        .copyTo(relationsDir.resolve("--eZhFiig.${NotifyRelationsConfig.fileSuffix()}"))

                    val converter = NotifyAnimeConverter(relationsDir =  relationsDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/current.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/finished.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/upcoming.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/status/tba.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.status).isEqualTo(Anime.Status.UNKNOWN)
                }
            }
        }

        @Nested
        inner class TagTests {

            @Test
            fun `put genres into tags`() {
                tempDirectory {
                    // given
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/tags/tags_from_genres.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/tags/genres_is_null.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/duration/0.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/duration/24.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                    val testFile = loadTestResource<String>("NotifyAnimeConverterTest/duration/120.json")

                    val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/no_year.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/1989.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/undefined.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/spring.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/summer.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/fall.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

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
                        val testFile = loadTestResource<String>("NotifyAnimeConverterTest/anime_season/winter.json")

                        val converter = NotifyAnimeConverter(relationsDir = tempDir)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(WINTER)
                    }
                }
            }
        }
    }

    @Nested
    inner class ConverterTests {

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