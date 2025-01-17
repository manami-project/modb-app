package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.nio.file.*
import java.util.zip.ZipFile
import kotlin.io.path.createFile
import kotlin.io.path.writeText
import kotlin.test.Test

internal class PathExtensionsKtTest {

    @Nested
    inner class ChangeSuffixTests {

        @Test
        fun `does nothing if the given path is a directory`() {
            tempDirectory {
                // given
                val directory = tempDir.resolve("test").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = directory.changeSuffix(LOCK_FILE_SUFFIX)

                // then
                assertThat(result).isEqualTo(directory)
            }
        }

        @Test
        fun `Changes suffix which starts with a dot`() {
            tempDirectory {
                // given
                val originalFile = tempDir.resolve("file.json").apply {
                    Files.createFile(this)
                }

                // when
                originalFile.changeSuffix(LOCK_FILE_SUFFIX).apply {
                    Files.createFile(this)
                }

                // then
                assertThat(tempDir.resolve("file.$LOCK_FILE_SUFFIX")).exists()
            }
        }

        @Test
        fun `Changes suffix which doesn't start with a dot`() {
            tempDirectory {
                // given
                val originalFile = tempDir.resolve("file.json").apply {
                    Files.createFile(this)
                }

                // when
                originalFile.changeSuffix(LOCK_FILE_SUFFIX).apply {
                    Files.createFile(this)
                }

                // then
                assertThat(tempDir.resolve("file.$LOCK_FILE_SUFFIX")).exists()
            }
        }

        @Test
        fun `Correctly changes only the part after the last dot`() {
            tempDirectory {
                // given
                val originalFile = tempDir.resolve("file.json.BAK").apply {
                    Files.createFile(this)
                }

                // when
                originalFile.changeSuffix(EMPTY).apply {
                    Files.createFile(this)
                }

                // then
                assertThat(tempDir.resolve("file.json")).exists()
            }
        }

        @Test
        fun `Add suffix to file without suffix`() {
            tempDirectory {
                // given
                val originalFile = tempDir.resolve("config").apply {
                    Files.createFile(this)
                }

                // when
                originalFile.changeSuffix(".BAK").apply {
                    Files.createFile(this)
                }

                // then
                assertThat(tempDir.resolve("config.BAK")).exists()
            }
        }

        @Test
        fun `Add suffix to 'hidden' files starting with a dot`() {
            tempDirectory {
                // given
                val originalFile = tempDir.resolve(".gitignore").apply {
                    Files.createFile(this)
                }

                // when
                originalFile.changeSuffix(".BAK").apply {
                    Files.createFile(this)
                }

                // then
                assertThat(tempDir.resolve(".gitignore.BAK")).exists()
            }
        }

        @Test
        fun `File without parent directory`() {
            // given
            val originalFile = Paths.get("test.html")

            // when
            val newFile = originalFile.changeSuffix(LOCK_FILE_SUFFIX)


            // then
            assertThat(newFile.fileName.toString()).isEqualTo("test.$LOCK_FILE_SUFFIX")
        }
    }

    @Nested
    inner class RegularFileExists {

        @Test
        fun `returns false if the given path does not exist`() {
            tempDirectory {
                // given
                val nonExistingFile = tempDir.resolve("non-existing-path.json")

                // when
                val result = nonExistingFile.regularFileExists()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `returns false if the given path exist, but is not a regular file`() {
            tempDirectory {
                // given
                val existingPath = tempDir.resolve("test").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = existingPath.regularFileExists()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `returns true if the given path exists and is a regular file`() {
            tempDirectory {
                // given
                val existingPath = tempDir.resolve("test.json").apply {
                    Files.createFile(this)
                }

                // when
                val result = existingPath.regularFileExists()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns true if the given path is a link`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.json").apply {
                    Files.createFile(this)
                }

                val link = tempDir.resolve("a-link").apply {
                    Files.createLink(this, file)
                }

                // when
                val result = link.regularFileExists()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns true if the given path is a symlink`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.json").apply {
                    Files.createFile(this)
                }

                val symlink = tempDir.resolve("a-symlink").apply {
                    Files.createSymbolicLink(this, file)
                }

                // when
                val result = symlink.regularFileExists()

                // then
                assertThat(result).isTrue()
            }
        }
    }

    @Nested
    inner class DirectoryExists {

        @Test
        fun `returns false if the given path does not exist`() {
            tempDirectory {
                // given
                val nonExistingDirectory = tempDir.resolve("non-existing-path")

                // when
                val result = nonExistingDirectory.directoryExists()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `returns false if the given path exist, but is not a directory`() {
            tempDirectory {
                // given
                val existingPath = tempDir.resolve("test.json").apply {
                    Files.createFile(this)
                }

                // when
                val result = existingPath.directoryExists()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `returns true if the given path exists and is a directory`() {
            tempDirectory {
                // given
                val existingPath = tempDir.resolve("test").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = existingPath.directoryExists()

                // then
                assertThat(result).isTrue()
            }
        }
    }

    @Nested
    inner class ReadFileTests {

        @Test
        fun `throws exception if the path is not a regular file`() {
            tempDirectory {
                // when
                val result = exceptionExpected<NoSuchFileException> {
                    tempDir.readFile()
                }

                // then
                assertThat(result)
            }
        }

        @Test
        fun `correctly reads file using carriage return line feed`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.txt").apply {
                    Files.createFile(this)
                    Files.write(this, "This file\r\n  uses\r\ncarriage return line feed [CRLF]".toByteArray())
                }

                // when
                val result = file.readFile()

                // then
                assertThat(result).isEqualTo("""
                    This file
                      uses
                    carriage return line feed [CRLF]
                """.trimIndent())
            }
        }

        @Test
        fun `correctly reads file using line feed`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.txt").apply {
                    Files.createFile(this)
                    Files.write(this, "This file\n  uses\nline feed [LF]".toByteArray())
                }

                // when
                val result = file.readFile()

                // then
                assertThat(result).isEqualTo("""
                    This file
                      uses
                    line feed [LF]
                """.trimIndent())
            }
        }
    }

    @Nested
    inner class CopyToTests {

        @Test
        fun `successfully copy a single file by passing an existing directory`() {
            tempDirectory {
                // given
                val fileName = "test.txt"

                val srcFile = Files.createFile(tempDir.resolve(fileName)).apply {
                    Files.write(this, "File content.".toByteArray())
                }

                val target = tempDir.resolve("target").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = srcFile.copyTo(target)

                // then
                assertThat(tempDir.resolve("target").resolve(fileName)).exists()
                assertThat(Files.readString(result)).isEqualTo(Files.readString(srcFile))
            }
        }

        @Test
        fun `successfully copy a single file by passing a non-existing path`() {
            tempDirectory {
                // given
                val fileName = "test.txt"
                val expectedFileName = "other.txt"

                val srcFile = Files.createFile(tempDir.resolve(fileName)).apply {
                    Files.write(this, "File content.".toByteArray())
                }

                val target = tempDir.resolve("target").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = srcFile.copyTo(target.resolve(expectedFileName))

                // then
                assertThat(target.resolve(expectedFileName)).exists()
                assertThat(Files.readString(result)).isEqualTo(Files.readString(srcFile))
            }
        }

        @Test
        fun `throws an exception if the file already exists`() {
            tempDirectory {
                // given
                val fileName = "test.txt"

                val srcFile = Files.createFile(tempDir.resolve(fileName)).apply {
                    Files.write(this, "File content.".toByteArray())
                }

                val target = tempDir.resolve("target").apply {
                    Files.createDirectory(this)
                    Files.createFile(this.resolve(fileName))
                }

                // when
                val result = exceptionExpected<FileAlreadyExistsException> {
                    srcFile.copyTo(target)
                }

                // then
                assertThat(result).hasMessage(target.resolve(fileName).toString())
            }
        }

        @Test
        fun `successfully copy a directory into another existing directory`() {
            tempDirectory {
                // given
                val srcDirectory = tempDir.resolve("src").apply {
                    Files.createDirectory(this)
                }

                val targetDirectory = tempDir.resolve("target").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = srcDirectory.copyTo(targetDirectory)

                // then
                assertThat(tempDir.resolve("src")).exists()
                assertThat(result).exists()
                assertThat(result).isEqualTo(targetDirectory.resolve("src"))
            }
        }

        @Test
        fun `successfully copy a directory by passing a non-existing path`() {
            tempDirectory {
                // given
                val srcDirectory = tempDir.resolve("src").apply {
                    Files.createDirectory(this)
                }

                val targetDirectory = tempDir.resolve("target").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = srcDirectory.copyTo(targetDirectory.resolve("src"))

                // then
                assertThat(tempDir.resolve("src")).exists()
                assertThat(result).exists()
                assertThat(result).isEqualTo(targetDirectory.resolve("src"))
            }
        }

        @Test
        fun `throws an exception if the directory already exists`() {
            tempDirectory {
                // given
                val srcDirectory = tempDir.resolve("src").apply {
                    Files.createDirectory(this)
                }

                val targetDirectory = tempDir.resolve("target").apply {
                    Files.createDirectory(this)
                    Files.createDirectory(this.resolve("src"))
                }

                // when
                val result = exceptionExpected<FileAlreadyExistsException> {
                    srcDirectory.copyTo(targetDirectory)
                }

                // then
                assertThat(result).hasMessage(targetDirectory.resolve(srcDirectory.fileName).toString())
            }
        }
    }

    @Nested
    inner class FileNameTests {

        @Test
        fun `correctly return the file name`() {
            tempDirectory {
                // given
                val expectedName = "test.json"
                val file = tempDir.resolve(expectedName).apply {
                    Files.createFile(this)
                }

                // when
                val result = file.fileName()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }

        @Test
        fun `correctly return the file name of a hidden file`() {
            tempDirectory {
                // given
                val expectedName = ".gitignore"
                val file = tempDir.resolve(expectedName).apply {
                    Files.createFile(this)
                }

                // when
                val result = file.fileName()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }

        @Test
        fun `correctly return name even if the given path is a directory`() {
            tempDirectory {
                // given
                val expectedName = "subdir"
                val dir = tempDir.resolve(expectedName).apply {
                    Files.createDirectory(this)
                }

                // when
                val result = dir.fileName()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }
    }

    @Nested
    inner class FileSuffixTests {

        @Test
        fun `correctly return the file suffix`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.json").apply {
                    Files.createFile(this)
                }

                // when
                val result = file.fileSuffix()

                // then
                assertThat(result).isEqualTo("json")
            }
        }

        @Test
        fun `return full name if the file is a hidden file`() {
            tempDirectory {
                // given
                val expectedName = ".gitignore"
                val file = tempDir.resolve(expectedName).apply {
                    Files.createFile(this)
                }

                // when
                val result = file.fileSuffix()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }

        @Test
        fun `return full name if there is no dot followed by a file suffix`() {
            tempDirectory {
                // given
                val expectedName = "test"
                val file = tempDir.resolve(expectedName).apply {
                    Files.createFile(this)
                }

                // when
                val result = file.fileSuffix()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }

        @Test
        fun `correctly return the file suffix even if the given path is a directory`() {
            tempDirectory {
                // given
                val expectedName = "subdir.more"
                val dir = tempDir.resolve(expectedName).apply {
                    Files.createDirectory(this)
                }

                // when
                val result = dir.fileSuffix()

                // then
                assertThat(result).isEqualTo("more")
            }
        }

        @Test
        fun `return full name if there is no dot followed by a file suffix even if the given path is a directory`() {
            tempDirectory {
                // given
                val expectedName = "subdir"
                val dir = tempDir.resolve(expectedName).apply {
                    Files.createDirectory(this)
                }

                // when
                val result = dir.fileSuffix()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }

        @Test
        fun `return full name if the given path is a hidden directory`() {
            tempDirectory {
                // given
                val expectedName = ".git"
                val dir = tempDir.resolve(expectedName).apply {
                    Files.createDirectory(this)
                }

                // when
                val result = dir.fileSuffix()

                // then
                assertThat(result).isEqualTo(expectedName)
            }
        }

        @Test
        fun `return correct suffix from a file having a dot within the name`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve("a_name_with_version_2.x.xml").apply {
                    Files.createDirectory(this)
                }

                // when
                val result = dir.fileSuffix()

                // then
                assertThat(result).isEqualTo("xml")
            }
        }
    }

    @Nested
    inner class ListRegularFilesTests {

        @Test
        fun `correctly return all regular files in a directory`() {
            tempDirectory {
                // given
                val file1 = tempDir.resolve("test1.json").apply {
                    Files.createFile(this)
                }

                val file2 = tempDir.resolve("test2.txt").apply {
                    Files.createFile(this)
                }

                tempDir.resolve("a-directory").apply {
                    Files.createDirectory(this)
                }

                val link = tempDir.resolve("a-link").apply {
                    Files.createLink(this, file1)
                }

                val symlink = tempDir.resolve("a-symlink").apply {
                    Files.createSymbolicLink(this, file1)
                }

                // when
                val result = tempDir.listRegularFiles()

                // then
                assertThat(result).containsExactlyInAnyOrder(file1, file2, link, symlink)
            }
        }

        @Test
        fun `correctly filters by glob`() {
            tempDirectory {
                // given
                tempDir.resolve("test1.json").apply {
                    Files.createFile(this)
                }

                val file2 = tempDir.resolve("test2.txt").apply {
                    Files.createFile(this)
                }

                tempDir.resolve("test3.yaml").apply {
                    Files.createFile(this)
                }

                val file4 = tempDir.resolve("test4.txt").apply {
                    Files.createFile(this)
                }

                // when
                val result = tempDir.listRegularFiles("*.txt")

                // then
                assertThat(result).containsExactlyInAnyOrder(file2, file4)
            }
        }

        @Test
        fun `throws exception if receiver is not a directory`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test1.json").apply {
                    Files.createFile(this)
                }

                // when
                val result = exceptionExpected<NotDirectoryException> {
                    file.listRegularFiles()
                }

                // then
                assertThat(result).hasMessageContaining(file.fileName.toString())
            }
        }
    }

    @Nested
    inner class CreateZipOfTests {

        @Test
        fun `throws exception if receiver is not a regular file`() {
            tempDirectory {
                // given
                val testFile = tempDir.resolve(Paths.get("test.txt")).createFile()

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    tempDir.createZipOf(testFile)
                }

                // then
                assertThat(result).hasMessage("Receiver must be a regular file.")
            }
        }

        @Test
        fun `throws exception if file to put into the zip file doesn't exist`() {
            tempDirectory {
                // given
                val receiver = tempDir.resolve(Paths.get("result.zip")).createFile()
                val testFile = tempDir.resolve(Paths.get("test.txt"))

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    receiver.createZipOf(testFile)
                }

                // then
                assertThat(result).hasMessage("Can only include regular files which exist.")
            }
        }

        @Test
        fun `throws exception if file to put into the zip file is not a regular file`() {
            tempDirectory {
                // given
                val receiver = tempDir.resolve(Paths.get("result.zip")).createFile()

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    receiver.createZipOf(tempDir)
                }

                // then
                assertThat(result).hasMessage("Can only include regular files which exist.")
            }
        }

        @Test
        fun `creates zip file if it doesn't exist`() {
            tempDirectory {
                // given
                val receiver = tempDir.resolve(Paths.get("result.zip"))
                val testFile = tempDir.resolve(Paths.get("test.txt")).createFile()

                // when
                val result = receiver.createZipOf(testFile)

                // then
                assertThat(result).exists()
                assertThat(ZipFile(receiver.toAbsolutePath().toString()).entries().toList().map { it.name }).containsExactlyInAnyOrder(
                    "test.txt",
                )
            }
        }

        @Test
        fun `overrides existing zip file`() {
            tempDirectory {
                // given
                val receiver = tempDir.resolve(Paths.get("result.zip")).createFile()
                val testFile1 = tempDir.resolve(Paths.get("test1.txt")).createFile()
                val testFile2 = tempDir.resolve(Paths.get("test2.txt")).createFile()
                receiver.createZipOf(testFile1)

                // when
                val result = receiver.createZipOf(testFile2)

                // then
                assertThat(result).exists()
                assertThat(ZipFile(receiver.toAbsolutePath().toString()).entries().toList().map { it.name }).containsExactlyInAnyOrder(
                    "test2.txt",
                )
            }
        }

        @Test
        fun `correctly creates zip with multiple files`() {
            tempDirectory {
                // given
                val receiver = tempDir.resolve(Paths.get("result.zip"))

                val files = mutableMapOf<String, String>()

                val text1 = "here is some text"
                val testFile1 = tempDir.resolve(Paths.get("test1.txt")).createFile().apply { writeText(text1) }
                files[testFile1.fileName.toString()] = text1

                val text2 = "some other text"
                val testFile2 = tempDir.resolve(Paths.get("test2.txt")).createFile().apply { writeText(text2) }
                files[testFile2.fileName.toString()] = text2

                // when
                val result = receiver.createZipOf(testFile1, testFile2)

                // then
                assertThat(result).exists()
                val zipFile = ZipFile(receiver.toAbsolutePath().toString())
                val entries = zipFile.entries().toList()
                assertThat(entries.map { it.name }).containsExactlyInAnyOrder(
                    "test1.txt",
                    "test2.txt",
                )
                entries.map { it.name }.forEach { name ->
                    val content = zipFile.getInputStream(zipFile.getEntry(name)).bufferedReader().readText()
                    assertThat(content).isEqualTo(files[name])
                }
            }
        }
    }
}
