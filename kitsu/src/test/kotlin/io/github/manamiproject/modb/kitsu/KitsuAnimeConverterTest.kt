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
import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.test.exceptionExpected
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

internal class KitsuAnimeConverterTest {

    @Nested
    inner class AnimeTests {

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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("11260.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("11260.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/title/special_chars.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val id = 44019

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/episodes/null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("1126.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("1126.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/episodes/39.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("6266.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("6266.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/tv.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("343.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("343.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/special.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("11613.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("11613.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/ona.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("11913.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("11913.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/ova.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("2027.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("2027.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/movie.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("11791.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("11791.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/type/music.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("6334.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("6334.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/picture_and_thumbnail/null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("42006.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("42006.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/picture_and_thumbnail/pictures.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("13228.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("13228.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/synonyms/combine_titles_and_synonyms.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("1217.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("1217.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/synonyms/abbreviatedTitles_contains_null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("1517.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("1517.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/sources/1517.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.sources).containsExactly(URI("https://kitsu.app/anime/1517"))
                }
            }
        }

        @Nested
        inner class RelationsTests {

            @Test
            fun `throws an exception, because the relations file doesn't exist`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val id = 46232

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/one_adaption_one_relation/$id.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        converter.convert(testFileContent)
                    }

                    // then
                    assertThat(result).hasMessage("Relations file is missing")
                }
            }

            @Test
            fun `no adaption, no relations`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/related_anime/no_adaption_no_relations/5989_relations.json")
                        .copyTo(relationsDir.resolve("5989.json"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("5989.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/no_adaption_no_relations/5989.json")
                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/related_anime/no_adaption_multiple_relations/7664_relations.json")
                        .copyTo(relationsDir.resolve("7664.json"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("7664.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/no_adaption_multiple_relations/7664.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val id = 46232

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/related_anime/one_adaption_one_relation/${id}_relations.json")
                        .copyTo(relationsDir.resolve("$id.json"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/one_adaption_one_relation/$id.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/related_anime/has_adaption_multiple_relations/1415_relations.json")
                        .copyTo(relationsDir.resolve("1415.json"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("1415.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/has_adaption_multiple_relations/1415.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/related_anime/has_adaption_but_no_relation/8641_relations.json")
                        .copyTo(relationsDir.resolve("8641.json"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("8641.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/has_adaption_but_no_relation/8641.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("10041.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("10041.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/finished.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val id = 12

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/current.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val id = 46873

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/unreleased.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val id = 46358

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/upcoming.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val id = 45557

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/tba.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("42059.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("42059.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/status/null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("46530.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("46530.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/null.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("10041.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("10041.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/0.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("10.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("10.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/24.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("10035.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("10035.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/duration/120.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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
            fun `throws an exception, because the tags file doesn't exist`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val id = 46232

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/related_anime/one_adaption_one_relation/$id.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        converter.convert(testFileContent)
                    }

                    // then
                    assertThat(result).hasMessage("Tags file is missing")
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("43298.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/tags/43298_tags.json")
                        .copyTo(tagsDir.resolve("43298.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/tags/43298.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.tags).isEmpty()
                }
            }

            @Test
            fun `successfully extract tags`() {
                tempDirectory {
                    // given
                    val testKitsuConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = KitsuConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = KitsuConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = KitsuConfig.fileSuffix()
                    }

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("1.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/tags/1_tags.json")
                        .copyTo(tagsDir.resolve("1.${testKitsuConfig.fileSuffix()}"))

                    val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/tags/1.json")

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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
                    )
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

                        val id = 10613

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/null.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("186.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("186.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/1989.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("44117.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("44117.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/invalid_format.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val id = 10613

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("$id.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/null.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("41370.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("41370.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/spring.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("42028.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("42028.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/summer.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("42328.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("42328.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/fall.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                        val relationsDir = tempDir.resolve("relations").createDirectory()
                        val tagsDir = tempDir.resolve("tags").createDirectory()

                        testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                            .copyTo(relationsDir.resolve("41312.${testKitsuConfig.fileSuffix()}"))

                        testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                            .copyTo(tagsDir.resolve("41312.${testKitsuConfig.fileSuffix()}"))

                        val testFileContent = loadTestResource<String>("KitsuAnimeConverterTest/anime_season/winter.json")

                        val converter = KitsuAnimeConverter(
                            metaDataProviderConfig = testKitsuConfig,
                            relationsDir = relationsDir,
                            tagsDir = tagsDir,
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("1517.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("1517.${testKitsuConfig.fileSuffix()}"))

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )

                    // when
                    val result = converter.convert(loadTestResource("KitsuAnimeConverterTest/scores/score.json"))

                    // then
                    assertThat(result.scores).hasSize(1)
                    assertThat(result.scores.first().scaledValue()).isEqualTo(7.893636363636364)
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

                    val relationsDir = tempDir.resolve("relations").createDirectory()
                    val tagsDir = tempDir.resolve("tags").createDirectory()

                    testResource("KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json")
                        .copyTo(relationsDir.resolve("44117.${testKitsuConfig.fileSuffix()}"))

                    testResource("KitsuAnimeConverterTest/no_tags_default_file.json")
                        .copyTo(tagsDir.resolve("44117.${testKitsuConfig.fileSuffix()}"))

                    val converter = KitsuAnimeConverter(
                        metaDataProviderConfig = testKitsuConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )

                    // when
                    val result = converter.convert(loadTestResource("KitsuAnimeConverterTest/scores/no-score.json"))

                    // then
                    assertThat(result.scores).isEmpty()
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
                val tagsDir = tempDir.resolve("tags").createDirectory()

                // when
                val result = assertThrows<IllegalArgumentException> {
                    KitsuAnimeConverter(
                        metaDataProviderConfig = TestMetaDataProviderConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
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
                val tagsDir = tempDir.resolve("tags").createDirectory()

                // when
                val result = assertThrows<IllegalArgumentException> {
                    KitsuAnimeConverter(
                        metaDataProviderConfig = TestMetaDataProviderConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )
                }

                // then
                assertThat(result).hasMessage("Directory for relations [$relationsDir] does not exist or is not a directory.")
            }
        }

        @Test
        fun `throws exception if the given path for tagsDir is not a directory`() {
            tempDirectory {
                // given
                val relationsDir = tempDir.resolve("relations").createDirectory()
                val tagsDir = tempDir.resolve("tags").createFile()

                // when
                val result = assertThrows<IllegalArgumentException> {
                    KitsuAnimeConverter(
                        metaDataProviderConfig = TestMetaDataProviderConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )
                }

                // then
                assertThat(result).hasMessage("Directory for tags [$tagsDir] does not exist or is not a directory.")
            }
        }

        @Test
        fun `throws exception if the given path for tagsDir does not exist`() {
            tempDirectory {
                // given
                val relationsDir = tempDir.resolve("relations").createDirectory()
                val tagsDir = tempDir.resolve("tags")

                // when
                val result = assertThrows<IllegalArgumentException> {
                    KitsuAnimeConverter(
                        metaDataProviderConfig = TestMetaDataProviderConfig,
                        relationsDir = relationsDir,
                        tagsDir = tagsDir,
                    )
                }

                // then
                assertThat(result).hasMessage("Directory for tags [$tagsDir] does not exist or is not a directory.")
            }
        }
    }
}