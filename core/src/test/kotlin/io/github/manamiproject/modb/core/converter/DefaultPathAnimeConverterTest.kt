package io.github.manamiproject.modb.core.converter

import io.github.manamiproject.modb.core.TestAnimeConverter
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class DefaultPathAnimeConverterTest {

    @Test
    fun `throws exception if the given Path does not exist`() {
        tempDirectory {
            // given
            val file = tempDir.resolve("test.txt")

            val specificTestConverter = object: AnimeConverter by TestAnimeConverter { }
            val converter = DefaultPathAnimeConverter(specificTestConverter, "txt")

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                converter.convert(file)
            }

            // then
            assertThat(result).hasMessage("Given path [$file] does not exist.")
        }
    }

    @Test
    fun `successfully converts a single file`() {
        tempDirectory {
            // given
            val file = tempDir.resolve("test.txt").createFile()
            "Correct file".writeToFile(file)

            val expectedAnime = AnimeRaw("Expected Anime")

            val specificTestConverter = object : AnimeConverter by TestAnimeConverter {
                override suspend fun convert(rawContent: String): AnimeRaw {
                    return if (rawContent == "Correct file") {
                        expectedAnime
                    } else {
                        shouldNotBeInvoked()
                    }
                }
            }

            val converter = DefaultPathAnimeConverter(specificTestConverter, "txt")

            // when
            val result = converter.convert(file)

            // then
            assertThat(result).containsExactly(expectedAnime)
        }
    }

    @Test
    fun `successfully convert a whole directory`() {
        tempDirectory {
            // given
            val directory = tempDir.resolve("dir").createDirectory().apply {
                "accept 1".writeToFile(this.resolve("file1.txt"))
                "accept 2".writeToFile(this.resolve("file2.txt"))
            }

            val specificTestConverter = object: AnimeConverter by TestAnimeConverter {
                override suspend fun convert(rawContent: String): AnimeRaw {
                    return if (rawContent.startsWith("accept")) {
                        AnimeRaw(rawContent)
                    } else {
                        shouldNotBeInvoked()
                    }
                }
            }

            val converter = DefaultPathAnimeConverter(specificTestConverter, "txt")

            // when
            val result = converter.convert(directory)

            // then
            assertThat(result).containsExactlyInAnyOrder(
                AnimeRaw("accept 1"),
                AnimeRaw("accept 2"),
            )
        }
    }
}