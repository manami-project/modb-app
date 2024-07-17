package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

internal class SimpleFileConverterTest {

    @Nested
    inner class ConvertUnconvertedFilesTests {

        @Test
        fun `convert all files which have haven't been converted yet`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "example.org"
                }

                val testAnime = Anime("Death Note")

                val testConverter = object: PathAnimeConverter {
                    override suspend fun convert(path: Path): Collection<Anime> = listOf(testAnime)
                }

                tempDir.resolve("123.${testConfig.fileSuffix()}").createFile()
                tempDir.resolve("369.${testConfig.fileSuffix()}").createFile()
                tempDir.resolve("369.${CONVERTED_FILE_SUFFIX}").createFile()
                tempDir.resolve("456.BAK").createFile()

                val simpleFileConverter = SimpleFileConverter(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                    converter = testConverter
                )

                // when
                simpleFileConverter.convertUnconvertedFiles()

                // then
                val convFiles = tempDir.listDirectoryEntries("*.$CONVERTED_FILE_SUFFIX").map { it.fileName() }
                assertThat(convFiles).containsExactlyInAnyOrder("123.${CONVERTED_FILE_SUFFIX}", "369.${CONVERTED_FILE_SUFFIX}")
            }
        }
    }


    @Nested
    inner class ConvertFileToConvFileTests {

        @Test
        fun `convert file to conv file`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "example.org"
                }

                val testAnime = Anime("Death Note")

                val testConverter = object: PathAnimeConverter {
                    override suspend fun convert(path: Path): Collection<Anime> = listOf(testAnime)
                }

                val testFile = tempDir.resolve("1535.${testConfig.fileSuffix()}").createFile()

                val simpleFileConverter = SimpleFileConverter(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                    converter = testConverter
                )

                // when
                simpleFileConverter.convertFileToConvFile(testFile)

                // then
                assertThat(tempDir.resolve("1535.$CONVERTED_FILE_SUFFIX")).exists()
            }
        }
    }
}