package io.github.manamiproject.modb.myanimelist

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test

internal class MyanimelistAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/title/special_chars.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Tobidasu PriPara: Mi~nna de Mezase! Idol☆Grand Prix")
            }
        }

        @Test
        fun `anime has original and english title in header`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/title/english_and_original_title.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Rurouni Kenshin: Meiji Kenkaku Romantan")
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is TV`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/tv.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `'Unknown' is mapped to 'UNKNOWN'`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/unknown.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(Anime.Type.UNKNOWN)
            }
        }

        @Test
        fun `type is Movie`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/movie.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type is music is mapped to special`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/music.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is ONA`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/ona.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/ova.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/special.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type PV is mapped to SPECIAL`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/pv.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type CM is mapped to SPECIAL`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/cm.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type 'TV Special' is mapped to SPECIAL`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/tv_special.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type Music but without a link is mapped to SPECIAL`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/type/music_without_link.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `movie case which resulted in containsOwn`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/type/movie_case_which_resulted_in_containsOwn.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `unknown number of episodes`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/unknown.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(0)
            }
        }

        @Test
        fun `1 episode`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/1.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/10.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/100.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(100)
            }
        }

        @Test
        fun `1818 episodes`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/episodes/1818.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(1818)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `neither picture nor thumbnail`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/picture_and_thumbnail/neither_picture_nor_thumbnail.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://cdn.myanimelist.net/images/anime/5/50551.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://cdn.myanimelist.net/images/anime/5/50551t.jpg"))
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `no synonyms`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/no_synonyms.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/one_synonym.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactly("エスパー魔美スペシャル マイエンジェル魔美ちゃん")
            }
        }

        @Test
        fun `multiple languages, one synonym each`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/multiple_languages_one_each.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Prétear",
                    "Prétear: The New Legend of Snow White",
                    "Shin Shirayuki-hime Densetsu Pretear",
                    "新白雪姫伝説プリーティア",
                )
            }
        }

        @Test
        fun `synonym containing comma`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/synonyms/synonym_contains_comma_but_title_does_not.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "'Tis Time for \"Torture,\" Princess",
                    "It's Time for \"Interrogation\", Princess!",
                    "Hime-sama \"Goumon\" no Jikan desu",
                    "姫様“拷問”の時間です",
                )
            }
        }

        @Test
        fun `multiple synonyms for one language`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/multiple_synonyms_for_one_language.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "DB Kai",
                    "DBK",
                    "DBZ Kai",
                    "Dragon Ball Z Kai: The Final Chapters",
                    "Dragonball Kai",
                    "ドラゴンボール改",
                )
            }
        }

        @Test
        fun `multiple synonyms with comma in one language section`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/synonyms/multiple_synonyms_with_comma_in_one_language.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Kono Naka ni Hitori, Imouto ga Iru! Episode 13",
                    "Kono Naka ni Hitori, Imouto ga Iru! OVA",
                    "NAKAIMO - My Little Sister Is Among Them! OVA",
                    "NakaImo, One of Them is My Younger Sister! OVA",
                    "Who is Imouto? OVA",
                    "この中に1人、妹がいる！兄、妹、恋人",
                )
            }
        }

        @Test
        fun `one synonym with multiple commas`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/synonyms/one_synonym_with_multiple_commas.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Yuru Yuri Special",
                    "YuruYuri: For Whatever Reason, Never Ending, Heart Throbbing, Pitter-patter, Paradox Eternal",
                    "ゆるゆり どうして☆止まらない☆トキメキ☆ドキドキ☆パラドクス☆エターナル",
                )
            }
        }

        @Test
        fun `semicolon in synonym wihtout whitespaces`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/synonyms/semicolon_in_synonym_wihtout_whitespaces.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "CHAOS;CHILD SILENT SKY",
                    "Chaos Child Episode 13",
                    "Chaos Child Episode 14",
                    "Chaos Child: Silent Sky",
                    "Chaos;Child, Folge 13 & 14 Silent Sky",
                    "ChäoS;Child: Silent Sky. Episodios 13 y 14",
                    "ChäoS;Child: Épisode 13 und 14 Silent Sky",
                )
            }
        }

        @Test
        fun `semicolon in synonym with whitespace`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/synonyms/semicolon_in_synonym_with_whitespace.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactly(
                    "Fate/Grand Order: Divine Realm of the Round Table - Camelot Wandering; Agateram",
                    "劇場版Fate/Grand Order -神聖円卓領域キャメロット- Wandering; Agateram",
                )
            }
        }

        @Test
        fun `semicolon in title but not in synonyms`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/synonyms/semicolon_in_title_but_not_in_synonyms.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Dual Parallel! Trouble Adventures - Final Frontier",
                    "Dual! Parallel Lunlun Monogatari Special",
                    "Dual! Parallel Trouble Adventures Special",
                    "デュアル！ぱられルンルン物語「ファイナル・フロンティア」",
                )
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract id 16498`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/sources/16498.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.sources).containsExactly(URI("https://myanimelist.net/anime/16498"))
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `no adaption, no relations`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/related_anime/no_adaption_no_relations.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `no adaption, multiple relations`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/related_anime/no_adaption_multiple_relations.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://myanimelist.net/anime/17819"),
                    URI("https://myanimelist.net/anime/19671"),
                    URI("https://myanimelist.net/anime/22265"),
                    URI("https://myanimelist.net/anime/30415"),
                    URI("https://myanimelist.net/anime/33845"),
                )
            }
        }

        @Test
        fun `one adaption, one relation`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/related_anime/has_one_adaption_and_one_relation.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactly(
                    URI("https://myanimelist.net/anime/10165"),
                )
            }
        }

        @Test
        fun `has adaption, multiple relations`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("file_converter_tests/related_anime/has_adaption_and_multiple_relations.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://myanimelist.net/anime/12685"),
                    URI("https://myanimelist.net/anime/15197"),
                    URI("https://myanimelist.net/anime/15199"),
                    URI("https://myanimelist.net/anime/15201"),
                    URI("https://myanimelist.net/anime/17277"),
                    URI("https://myanimelist.net/anime/1953"),
                    URI("https://myanimelist.net/anime/2124"),
                    URI("https://myanimelist.net/anime/2904"),
                    URI("https://myanimelist.net/anime/30711"),
                    URI("https://myanimelist.net/anime/33155"),
                    URI("https://myanimelist.net/anime/34438"),
                    URI("https://myanimelist.net/anime/34439"),
                    URI("https://myanimelist.net/anime/40334"),
                    URI("https://myanimelist.net/anime/40836"),
                    URI("https://myanimelist.net/anime/41075"),
                    URI("https://myanimelist.net/anime/4596"),
                    URI("https://myanimelist.net/anime/8888"),
                )
            }
        }

        @Test
        fun `has adaption, no relations`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/related_anime/has_adaption_but_no_relation.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `'currently airing' is mapped to 'ONGOING'`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/status/ongoing.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `'Not yet aired' is mapped to 'UPCOMING'`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/status/upcoming.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `'Finished Airing' is mapped to 'FINISHED'`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/status/finished.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `extract multiple tags`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/tags/multiple_tags.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "action",
                    "adventure",
                    "drama",
                    "fantasy",
                    "military",
                    "shounen",
                )
            }
        }

        @Test
        fun `extract exactly one tag`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/tags/one_tag.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactly("fantasy")
            }
        }

        @Test
        fun `no tags available`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/tags/no_tags.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
        fun `1 hr`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/1_hour.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/1_hour_11_min.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(71, MINUTES))
            }
        }

        @Test
        fun `1 hr 11 min per ep`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/1_hour_11_min_per_episode.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/2_hours.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/2_hours_15_minutes.html")

                val converter = MyanimelistAnimeConverter(testConfig)

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
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/10_min.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(10, MINUTES))
            }
        }

        @Test
        fun `10 min per ep`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/10_min_per_episode.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(10, MINUTES))
            }
        }

        @Test
        fun `10 sec`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/10_sec.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(10, SECONDS))
            }
        }

        @Test
        fun `10 sec per ep`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/10_sec_per_episode.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(10, SECONDS))
            }
        }

        @Test
        fun `unknown duration`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("file_converter_tests/duration/unknown.html")

                val converter = MyanimelistAnimeConverter(testConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class SeasonTests {

            @Test
            fun `'UNDEFINED', because anime season link is not available`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/undefined.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

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
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/fall.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

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
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/spring.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

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
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/summer.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

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
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/winter.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["apr", "may", "jun"])
            fun `season is 'SPRING' by aired because premiered is not set`(value: String) {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/$value.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["jul", "aug", "sep"])
            fun `season is 'SUMMER' by aired because premiered is not set`(value: String) {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/$value.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["oct", "nov", "dec"])
            fun `season is 'FALL' by aired because premiered is not set`(value: String) {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/$value.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["jan", "feb", "mar"])
            fun `season is 'WINTER' by aired because premiered is not set`(value: String) {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("file_converter_tests/anime_season/season/$value.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }
        }

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `extract from 'aired', because anime season is not set - year only`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_only.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1928)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - year to unknown`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_unknown.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1996)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - unavailable`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_unavailable.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(0)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - exact day to unknown`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day_to_unknown.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2004)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - year to year`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_year.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1964)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - month of year to year`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_month_of_year_to_year.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1992)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - month of year to unknown`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_month_of_year_to_unknown.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1996)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - exact day`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1980)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - exact day to exact day`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day_to_exact_day.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1998)
                }
            }


            @Test
            fun `extract from 'aired', because anime season is not set - year to exact day`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_exact_day.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1977)
                }
            }

            @Test
            fun `extract from 'aired', because anime season is not set - exact day to year`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day_to_year.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1982)
                }
            }

            @Test
            fun `extract from anime season link which exists in mobile version, but not on desktop version`() {
                runBlocking {
                    // given
                    val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                        override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
                        override fun buildDataDownloadLink(id: String): URI = MyanimelistConfig.buildDataDownloadLink(id)
                        override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("file_converter_tests/anime_season/year_of_premiere/premiered.html")

                    val converter = MyanimelistAnimeConverter(testConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2010)
                }
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = MyanimelistAnimeConverter.instance

            // when
            val result = MyanimelistAnimeConverter.instance

            // then
            assertThat(result).isExactlyInstanceOf(MyanimelistAnimeConverter::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}