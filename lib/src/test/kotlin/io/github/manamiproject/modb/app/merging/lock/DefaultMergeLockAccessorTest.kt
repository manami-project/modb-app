package io.github.manamiproject.modb.app.merging.lock

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DefaultMergeLockAccessorTest {

    @Nested
    inner class IsPartOfMergeLockTests {

        @Test
        fun `returns false if merge lock file does not exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val mergeLockLookup = URI("https://myanimelist.net/anime/1535")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.isPartOfMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).doesNotExist()
            }
        }

        @Test
        fun `returns false if merge lock file exists, but is empty`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/empty-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = URI("https://myanimelist.net/anime/1535")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.isPartOfMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).exists()
            }
        }

        @Test
        fun `returns true if if is part of a merge lock with multiple urls`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = URI("https://myanimelist.net/anime/15487")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )
                val all = defaultMergeLockAccess.allSourcesInAllMergeLockEntries()

                // when
                val result = defaultMergeLockAccess.isPartOfMergeLock(mergeLockLookup)

                // then
                assertThat(result).isTrue()
                assertThat(all).contains(mergeLockLookup)
            }
        }

        @Test
        fun `returns false if it's not part of a merge lock entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = URI("https://myanimelist.net/anime/1535")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.isPartOfMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val mergeLockLookup = URI("https://myanimelist.net/anime/1535")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.isPartOfMergeLock(mergeLockLookup)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class GetMergeLockTests {

        @Test
        fun `returns empty list if merge lock file does not exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.getMergeLock(URI("https://myanimelist.net/anime/1535"))

                // then
                assertThat(result).isEmpty()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).doesNotExist()
            }
        }

        @Test
        fun `returns empty list if merge lock file exists, but is empty`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/empty-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.getMergeLock(URI("https://myanimelist.net/anime/1535"))

                // then
                assertThat(result).isEmpty()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).exists()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.getMergeLock(URI("https://myanimelist.net/anime/1535"))

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `retrieves the correct merge lock for every url`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val expectedMergeLock1 = listOf(
                    URI("https://anilist.co/anime/15487"),
                    URI("https://kitsu.app/anime/7262"),
                    URI("https://myanimelist.net/anime/15487"),
                    URI("https://notify.moe/anime/WPjdpKiiR"),
                )

                val expectedMergeLock2 = listOf(
                    URI("https://anilist.co/anime/98703"),
                    URI("https://kitsu.app/anime/8717"),
                    URI("https://myanimelist.net/anime/22673"),
                    URI("https://notify.moe/anime/7-PJpFmmR"),
                )

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result1 = defaultMergeLockAccess.getMergeLock(URI("https://anilist.co/anime/15487"))
                val result2 = defaultMergeLockAccess.getMergeLock(URI("https://kitsu.app/anime/7262"))
                val result3 = defaultMergeLockAccess.getMergeLock(URI("https://myanimelist.net/anime/15487"))
                val result4 = defaultMergeLockAccess.getMergeLock(URI("https://notify.moe/anime/WPjdpKiiR"))

                val result5 = defaultMergeLockAccess.getMergeLock(URI("https://anilist.co/anime/98703"))
                val result6 = defaultMergeLockAccess.getMergeLock(URI("https://kitsu.app/anime/8717"))
                val result7 = defaultMergeLockAccess.getMergeLock(URI("https://myanimelist.net/anime/22673"))
                val result8 = defaultMergeLockAccess.getMergeLock(URI("https://notify.moe/anime/7-PJpFmmR"))

                // then
                assertThat(result1).containsExactlyInAnyOrder(*expectedMergeLock1.toTypedArray())
                assertThat(result2).containsExactlyInAnyOrder(*expectedMergeLock1.toTypedArray())
                assertThat(result3).containsExactlyInAnyOrder(*expectedMergeLock1.toTypedArray())
                assertThat(result4).containsExactlyInAnyOrder(*expectedMergeLock1.toTypedArray())

                assertThat(result5).containsExactlyInAnyOrder(*expectedMergeLock2.toTypedArray())
                assertThat(result6).containsExactlyInAnyOrder(*expectedMergeLock2.toTypedArray())
                assertThat(result7).containsExactlyInAnyOrder(*expectedMergeLock2.toTypedArray())
                assertThat(result8).containsExactlyInAnyOrder(*expectedMergeLock2.toTypedArray())
            }
        }
    }

    @Nested
    inner class AllSourcesInAllMergeLockEntriesTests {

        @Test
        fun `returns empty list if merge lock file does not exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.allSourcesInAllMergeLockEntries()

                // then
                assertThat(result).isEmpty()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).doesNotExist()
            }
        }

        @Test
        fun `returns empty list if merge lock file exists, but is empty`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/empty-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.allSourcesInAllMergeLockEntries()

                // then
                assertThat(result).isEmpty()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).exists()

            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.allSourcesInAllMergeLockEntries()

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `retrieves all sources in merge lock`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.allSourcesInAllMergeLockEntries()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    URI("https://anilist.co/anime/15487"),
                    URI("https://kitsu.app/anime/7262"),
                    URI("https://myanimelist.net/anime/15487"),
                    URI("https://notify.moe/anime/WPjdpKiiR"),
                    URI("https://anilist.co/anime/98703"),
                    URI("https://kitsu.app/anime/8717"),
                    URI("https://myanimelist.net/anime/22673"),
                    URI("https://notify.moe/anime/7-PJpFmmR"),
                )
            }
        }
    }

    @Nested
    inner class HasMergeLockTests {

        @Test
        fun `single url - returns false if merge lock file does not exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val mergeLockLookup = setOf(URI("https://myanimelist.net/anime/1535"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).doesNotExist()
            }
        }

        @Test
        fun `single url - returns false if merge lock file exists, but is empty`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/empty-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = setOf(URI("https://myanimelist.net/anime/1535"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).exists()
            }
        }

        @Test
        fun `single url - returns true if it is part of a merge lock with multiple urls`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = setOf(URI("https://myanimelist.net/anime/15487"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `single url - returns false if it's not part of a merge lock entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = setOf(URI("https://myanimelist.net/anime/1535"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `multiple urls - returns false if merge lock file does not exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val mergeLockLookup = setOf(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).doesNotExist()
            }
        }

        @Test
        fun `multiple urls - returns false if merge lock file exists, but is empty`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/empty-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val mergeLockLookup = setOf(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg")
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock")).exists()
            }
        }

        @Test
        fun `multiple urls - returns true for all urls of an existing merge lock meaning it matches completely`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = setOf(
                    URI("https://anilist.co/anime/15487"),
                    URI("https://kitsu.app/anime/7262"),
                    URI("https://myanimelist.net/anime/15487"),
                    URI("https://notify.moe/anime/WPjdpKiiR"),
                )

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `multiple urls - returns true for a subset of urls of a merge lock entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = setOf(
                    URI("https://kitsu.app/anime/7262"),
                    URI("https://myanimelist.net/anime/15487"),
                )

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `multiple urls - returns false for all urls of an existing merge lock plus an additional url which is not part of that merge lock entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val mergeLockLookup = setOf(
                    URI("https://anilist.co/anime/15487"),
                    URI("https://kitsu.app/anime/7262"),
                    URI("https://myanimelist.net/anime/15487"),
                    URI("https://notify.moe/anime/WPjdpKiiR"),
                    URI("https://anidb.net/anime/8681"),
                )

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val mergeLockLookup = setOf(URI("https://myanimelist.net/anime/1535"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.hasMergeLock(mergeLockLookup)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class AddMergeLockTests {

        @Test
        fun `does nothing if you pass an empty set`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockFile = testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                defaultMergeLockAccess.addMergeLock(emptySet())

                // then
                val expectedFile = loadTestResource<String>("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")

                assertThat(testMergeLockFile.readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `successfully add a merge lock entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockFile = testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val newMergeLockEntry = setOf(
                    URI("https://anidb.net/anime/12142"),
                    URI("https://anilist.co/anime/21829"),
                    URI("https://kitsu.app/anime/12654"),
                    URI("https://myanimelist.net/anime/33363"),
                    URI("https://notify.moe/anime/cLhmhFiiR"),
                )

                // when
                defaultMergeLockAccess.addMergeLock(newMergeLockEntry)

                // then
                val expectedFile = loadTestResource<String>("merging/lock/DefaultMergeLockAccessTest/expected_file_after_adding_an_entry-merge.lock")

                assertThat(defaultMergeLockAccess.hasMergeLock(newMergeLockEntry)).isTrue()
                assertThat(testMergeLockFile.readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `does nothing if you try to add the exact same merge lock entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockFile = testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val newMergeLockEntry = setOf(
                    URI("https://anidb.net/anime/12142"),
                    URI("https://anilist.co/anime/21829"),
                    URI("https://kitsu.app/anime/12654"),
                    URI("https://myanimelist.net/anime/33363"),
                    URI("https://notify.moe/anime/cLhmhFiiR"),
                )
                defaultMergeLockAccess.addMergeLock(newMergeLockEntry)

                // when
                defaultMergeLockAccess.addMergeLock(newMergeLockEntry)

                // then
                val expectedFile = loadTestResource<String>("merging/lock/DefaultMergeLockAccessTest/expected_file_after_adding_an_entry-merge.lock")

                assertThat(testMergeLockFile.readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `throws exception if you try to add a duplicate`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val newMergeLockEntry = setOf(
                    URI("https://anilist.co/anime/15487"),
                    URI("https://example.org/anime/123456"),
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultMergeLockAccess.addMergeLock(newMergeLockEntry)
                }

                // then
                assertThat(result).hasMessage("You were about to add a duplicate to the merge.lock file for [https://anilist.co/anime/15487]")
            }
        }

        @Test
        fun `output file is always sorted` () {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val one = setOf(
                    URI("https://anilist.co/anime/98703"),
                    URI("https://kitsu.app/anime/8717"),
                    URI("https://myanimelist.net/anime/22673"),
                    URI("https://notify.moe/anime/7-PJpFmmR"),
                )
                defaultMergeLockAccess.addMergeLock(one)

                val two = setOf(
                    URI("https://anidb.net/anime/12142"),
                    URI("https://anilist.co/anime/21829"),
                    URI("https://kitsu.app/anime/12654"),
                    URI("https://myanimelist.net/anime/33363"),
                    URI("https://notify.moe/anime/cLhmhFiiR"),
                )
                defaultMergeLockAccess.addMergeLock(two)

                val three = setOf(
                    URI("https://anilist.co/anime/15487"),
                    URI("https://kitsu.app/anime/7262"),
                    URI("https://myanimelist.net/anime/15487"),
                    URI("https://notify.moe/anime/WPjdpKiiR"),
                )

                // when
                defaultMergeLockAccess.addMergeLock(three)

                // then
                val expectedFile = loadTestResource<String>("merging/lock/DefaultMergeLockAccessTest/expected_file_after_adding_an_entry-merge.lock")

                assertThat(testAppConfig.downloadControlStateDirectory().resolve("merge.lock").readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val testMergeLockEntry = setOf(URI("https://myanimelist.net/anime/1535"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.addMergeLock(testMergeLockEntry)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class ReplaceUriTests {

        @Test
        fun `does nothing if the URI to be replaced doesn't exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockFile = testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val oldUri = URI("https://anime-planet.com/anime/full-metal-panic-new-season")
                val newUri = URI("https://anime-planet.com/anime/full-metal-panic-invisible-victory")

                // when
                defaultMergeLockAccess.replaceUri(oldUri, newUri)

                // then
                val expectedFile = loadTestResource<String>("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")

                assertThat(testMergeLockFile.readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `successfully replaces uri`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val mergeLockEntry1 = setOf(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://anime-planet.com/anime/death-note"),
                    URI("https://anisearch.com/anime/3633"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://livechart.me/anime/3437"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                )
                defaultMergeLockAccess.addMergeLock(mergeLockEntry1)

                val oldUri = URI("https://anime-planet.com/anime/full-metal-panic-new-season")
                val newUri = URI("https://anime-planet.com/anime/full-metal-panic-invisible-victory")

                val mergeLockEntry2 = setOf(
                    URI("https://anidb.net/anime/11731"),
                    URI("https://anilist.co/anime/21451"),
                    URI("https://anime-planet.com/anime/full-metal-panic-new-season"),
                    URI("https://anisearch.com/anime/10974"),
                    URI("https://kitsu.app/anime/11460"),
                    URI("https://livechart.me/anime/1889"),
                    URI("https://myanimelist.net/anime/31931"),
                    URI("https://notify.moe/anime/B7njtFiiR"),
                )

                defaultMergeLockAccess.addMergeLock(mergeLockEntry2)

                // when
                defaultMergeLockAccess.replaceUri(oldUri, newUri)

                // then
                val currentFile = testAppConfig.downloadControlStateDirectory().resolve("merge.lock").readFile()
                assertThat(currentFile).doesNotContain(oldUri.toString())
                assertThat(currentFile).contains(newUri.toString())
                assertThat(currentFile).contains("https://anidb.net/anime/4563")
                assertThat(currentFile).contains("https://anilist.co/anime/1535")
                assertThat(currentFile).contains("https://anime-planet.com/anime/death-note")
                assertThat(currentFile).contains("https://anisearch.com/anime/3633")
                assertThat(currentFile).contains("https://kitsu.app/anime/1376")
                assertThat(currentFile).contains("https://livechart.me/anime/3437")
                assertThat(currentFile).contains("https://myanimelist.net/anime/1535")
                assertThat(currentFile).contains("https://notify.moe/anime/0-A-5Fimg")
                assertThat(currentFile).contains("https://anidb.net/anime/11731")
                assertThat(currentFile).contains("https://anilist.co/anime/21451")
                assertThat(currentFile).contains("https://anisearch.com/anime/10974")
                assertThat(currentFile).contains("https://kitsu.app/anime/11460")
                assertThat(currentFile).contains("https://livechart.me/anime/1889")
                assertThat(currentFile).contains("https://myanimelist.net/anime/31931")
                assertThat(currentFile).contains("https://notify.moe/anime/B7njtFiiR")

                val inMemoryMergeLockList = defaultMergeLockAccess.allSourcesInAllMergeLockEntries()
                assertThat(inMemoryMergeLockList).doesNotContain(oldUri)
                assertThat(inMemoryMergeLockList).containsExactlyInAnyOrder(
                    newUri,
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://anime-planet.com/anime/death-note"),
                    URI("https://anisearch.com/anime/3633"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://livechart.me/anime/3437"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                    URI("https://anidb.net/anime/11731"),
                    URI("https://anilist.co/anime/21451"),
                    URI("https://anisearch.com/anime/10974"),
                    URI("https://kitsu.app/anime/11460"),
                    URI("https://livechart.me/anime/1889"),
                    URI("https://myanimelist.net/anime/31931"),
                    URI("https://notify.moe/anime/B7njtFiiR"),
                )
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val oldUri = URI("https://anime-planet.com/anime/full-metal-panic-new-season")
                val newUri = URI("https://anime-planet.com/anime/full-metal-panic-invisible-victory")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.replaceUri(oldUri, newUri)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class RemoveEntryTests {

        @Test
        fun `does nothing if the URI to be removed doesn't exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockFile = testResource("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val testEntry = URI("https://anime-planet.com/anime/full-metal-panic-new-season")

                // when
                defaultMergeLockAccess.removeEntry(testEntry)

                // then
                val expectedFile = loadTestResource<String>("merging/lock/DefaultMergeLockAccessTest/test-merge.lock")

                assertThat(testMergeLockFile.readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `successfully removes URI from merge lock file`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                val mergeLockEntry1 = setOf(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://anime-planet.com/anime/death-note"),
                    URI("https://anisearch.com/anime/3633"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://livechart.me/anime/3437"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                )
                defaultMergeLockAccess.addMergeLock(mergeLockEntry1)

                val entryToBeRemoved = URI("https://anime-planet.com/anime/full-metal-panic-new-season")

                val mergeLockEntry2 = setOf(
                    URI("https://anidb.net/anime/11731"),
                    URI("https://anilist.co/anime/21451"),
                    entryToBeRemoved,
                    URI("https://anisearch.com/anime/10974"),
                    URI("https://kitsu.app/anime/11460"),
                    URI("https://livechart.me/anime/1889"),
                    URI("https://myanimelist.net/anime/31931"),
                    URI("https://notify.moe/anime/B7njtFiiR"),
                )
                defaultMergeLockAccess.addMergeLock(mergeLockEntry2)

                // when
                defaultMergeLockAccess.removeEntry(entryToBeRemoved)

                // then
                val currentFile = testAppConfig.downloadControlStateDirectory().resolve("merge.lock").readFile()
                assertThat(currentFile).doesNotContain(entryToBeRemoved.toString())
                assertThat(currentFile).contains("https://anidb.net/anime/4563")
                assertThat(currentFile).contains("https://anilist.co/anime/1535")
                assertThat(currentFile).contains("https://anime-planet.com/anime/death-note")
                assertThat(currentFile).contains("https://anisearch.com/anime/3633")
                assertThat(currentFile).contains("https://kitsu.app/anime/1376")
                assertThat(currentFile).contains("https://livechart.me/anime/3437")
                assertThat(currentFile).contains("https://myanimelist.net/anime/1535")
                assertThat(currentFile).contains("https://notify.moe/anime/0-A-5Fimg")
                assertThat(currentFile).contains("https://anidb.net/anime/11731")
                assertThat(currentFile).contains("https://anilist.co/anime/21451")
                assertThat(currentFile).contains("https://anisearch.com/anime/10974")
                assertThat(currentFile).contains("https://kitsu.app/anime/11460")
                assertThat(currentFile).contains("https://livechart.me/anime/1889")
                assertThat(currentFile).contains("https://myanimelist.net/anime/31931")
                assertThat(currentFile).contains("https://notify.moe/anime/B7njtFiiR")

                val inMemoryMergeLockList = defaultMergeLockAccess.allSourcesInAllMergeLockEntries()
                assertThat(inMemoryMergeLockList).doesNotContain(entryToBeRemoved)
                assertThat(inMemoryMergeLockList).containsExactlyInAnyOrder(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://anime-planet.com/anime/death-note"),
                    URI("https://anisearch.com/anime/3633"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://livechart.me/anime/3437"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                    URI("https://anidb.net/anime/11731"),
                    URI("https://anilist.co/anime/21451"),
                    URI("https://anisearch.com/anime/10974"),
                    URI("https://kitsu.app/anime/11460"),
                    URI("https://livechart.me/anime/1889"),
                    URI("https://myanimelist.net/anime/31931"),
                    URI("https://notify.moe/anime/B7njtFiiR"),
                )
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var hasBeenInvoked = false

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory {
                        hasBeenInvoked = true
                        return tempDir
                    }
                }

                val testEntry = URI("https://myanimelist.net/anime/1535")

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                check(!hasBeenInvoked)

                // when
                defaultMergeLockAccess.removeEntry(testEntry)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }
    
    @Nested
    inner class InitializationTests {

        @Test
        fun `throws exception if merge lock file contains duplicates`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testResource("merging/lock/DefaultMergeLockAccessTest/test_with_duplicates-merge.lock")
                    .copyTo(testAppConfig.downloadControlStateDirectory().resolve("merge.lock"))

                val defaultMergeLockAccess = DefaultMergeLockAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result= exceptionExpected<IllegalStateException> {
                    defaultMergeLockAccess.allSourcesInAllMergeLockEntries()
                }

                // then
                assertThat(result).hasMessage("Duplicates found: [https://kitsu.app/anime/7262, https://myanimelist.net/anime/15487]")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultMergeLockAccessor.instance

                // when
                val result = DefaultMergeLockAccessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultMergeLockAccessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}