package io.github.manamiproject.modb.analyzer.cluster

import io.github.manamiproject.modb.analyzer.TestMergeLockAccessor
import io.github.manamiproject.modb.analyzer.TestReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.models.Anime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class ClusterServiceTest {

    @Nested
    inner class ClusterDistributionTests {

        @Test
        fun `create a correct cluster distribution`() {
            // given
            val databaseEntries = listOf(
                Anime(
                    _title = "1 - one",
                    sources = hashSetOf(URI("https://anidb.net/anime/24")),
                ),
                Anime(
                    _title = "1 - two",
                    sources = hashSetOf(URI("https://anilist.co/anime/48")),
                ),
                Anime(
                    _title = "1 - three",
                    sources = hashSetOf(URI("https://kitsu.app/anime/30")),
                ),
                Anime(
                    _title = "1 - four",
                    sources = hashSetOf(URI("https://myanimelist.net/anime/48")),
                ),
                Anime(
                    _title = "1 - five",
                    sources = hashSetOf(URI("https://notify.moe/anime/xpIt5Fiig")),
                ),
                Anime(
                    _title = "2 - one",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                    ),
                ),
                Anime(
                    _title = "2 - two",
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                    ),
                ),
                Anime(
                    _title = "2 - three",
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "2 - four",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "3 - one",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                        URI("https://kitsu.app/anime/30"),
                    ),
                ),
                Anime(
                    _title = "3 - two",
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "3 - three",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://kitsu.app/anime/30"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "4 - one",
                    sources = hashSetOf(
                        URI("https://anilist.co/anime/48"),
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "4 - two",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                    ),
                ),
                Anime(
                    _title = "5",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
            )

            val testMergeLockAccessor = object: MergeLockAccessor by TestMergeLockAccessor {
                override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
            }

            val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                override fun contains(uri: URI): Boolean = false
            }

            val clusterService = ClusterService(
                mergeLockAccessor = testMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.clusterDistribution(databaseEntries)

            // then
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    1 to (5 to 5),
                    2 to (4 to 4),
                    3 to (3 to 3),
                    4 to (2 to 2),
                    5 to (1 to 1)
                )
            )
        }

        @Test
        fun `correctly removes reviewed isolated entries from number of unreviewed anime`() {
            // given
            val databaseEntries = listOf(
                Anime(
                    _title = "1 - one",
                    sources = hashSetOf(URI("https://anidb.net/anime/24")),
                ),
                Anime(
                    _title = "1 - two",
                    sources = hashSetOf(URI("https://anilist.co/anime/48")),
                ),
                Anime(
                    _title = "1 - three",
                    sources = hashSetOf(URI("https://kitsu.app/anime/30")),
                ),
                Anime(
                    _title = "1 - four",
                    sources = hashSetOf(URI("https://myanimelist.net/anime/48")),
                ),
                Anime(
                    _title = "1 - five",
                    sources = hashSetOf(URI("https://notify.moe/anime/xpIt5Fiig")),
                ),
            )

            val testMergeLockAccessor = object: MergeLockAccessor by TestMergeLockAccessor {
                override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
            }

            val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                override fun contains(uri: URI): Boolean = setOf(
                    URI("https://myanimelist.net/anime/48"),
                    URI("https://anilist.co/anime/48"),
                ).contains(uri)
            }

            val clusterService = ClusterService(
                mergeLockAccessor = testMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.clusterDistribution(databaseEntries)

            // then
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    1 to (5 to 3),
                )
            )
        }

        @Test
        fun `correctly removes merge lock entries from number of unreviewed anime`() {
            // given
            val databaseEntries = listOf(
                Anime(
                    _title = "2 - one",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                    ),
                ),
                Anime(
                    _title = "2 - two",
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                    ),
                ),
                Anime(
                    _title = "2 - three",
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "2 - four",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
            )

            val testMergeLockAccessor = object: MergeLockAccessor by TestMergeLockAccessor {
                override suspend fun hasMergeLock(uris: Set<URI>): Boolean {
                    return uris == hashSetOf(
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                    )
                }
            }

            val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                override fun contains(uri: URI): Boolean = false
            }

            val clusterService = ClusterService(
                mergeLockAccessor = testMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.clusterDistribution(databaseEntries)

            // then
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    2 to (4 to 3),
                )
            )
        }

        @Test
        fun `correctly removes merge lock entries from number of unreviewed anime cluster 1`() {
            // given
            val databaseEntries = listOf(
                Anime(
                    _title = "1 - one",
                    sources = hashSetOf(URI("https://anidb.net/anime/24")),
                ),
                Anime(
                    _title = "1 - two",
                    sources = hashSetOf(URI("https://anilist.co/anime/48")),
                ),
                Anime(
                    _title = "1 - three",
                    sources = hashSetOf(URI("https://kitsu.app/anime/30")),
                ),
                Anime(
                    _title = "1 - four",
                    sources = hashSetOf(URI("https://myanimelist.net/anime/48")),
                ),
                Anime(
                    _title = "1 - five",
                    sources = hashSetOf(URI("https://notify.moe/anime/xpIt5Fiig")),
                ),
            )

            val testMergeLockAccessor = object: MergeLockAccessor by TestMergeLockAccessor {
                override suspend fun hasMergeLock(uris: Set<URI>): Boolean {
                    return uris == setOf(URI("https://kitsu.app/anime/30")) || uris == setOf(URI("https://myanimelist.net/anime/48"))
                }
            }

            val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                override fun contains(uri: URI): Boolean = false
            }

            val clusterService = ClusterService(
                mergeLockAccessor = testMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.clusterDistribution(databaseEntries)

            // then
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    1 to (5 to 3),
                )
            )
        }

        @Test
        fun `returns empty map on empty dataset`() {
            // given
            val clusterService = ClusterService(
                mergeLockAccessor = TestMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = TestReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.clusterDistribution(emptyList())

            // then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class FetchClusterTests {

        @Test
        fun `correctly fetches cluster`() {
            // given
            val expectedEntryOne = Anime(
                _title = "4 - one",
                sources = hashSetOf(
                    URI("https://anilist.co/anime/48"),
                    URI("https://kitsu.app/anime/30"),
                    URI("https://myanimelist.net/anime/48"),
                    URI("https://notify.moe/anime/xpIt5Fiig"),
                ),
            )

            val expectedEntryTwo = Anime(
                _title = "4 - two",
                sources = hashSetOf(
                    URI("https://anidb.net/anime/24"),
                    URI("https://anilist.co/anime/48"),
                    URI("https://kitsu.app/anime/30"),
                    URI("https://myanimelist.net/anime/48"),
                ),
            )

            val databaseEntries = listOf(
                Anime(
                    _title = "1 - one",
                    sources = hashSetOf(URI("https://anidb.net/anime/24")),
                ),
                Anime(
                    _title = "1 - two",
                    sources = hashSetOf(URI("https://anilist.co/anime/48")),
                ),
                Anime(
                    _title = "1 - three",
                    sources = hashSetOf(URI("https://kitsu.app/anime/30")),
                ),
                Anime(
                    _title = "1 - four",
                    sources = hashSetOf(URI("https://myanimelist.net/anime/48")),
                ),
                Anime(
                    _title = "1 - five",
                    sources = hashSetOf(URI("https://notify.moe/anime/xpIt5Fiig")),
                ),
                Anime(
                    _title = "2 - one",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                    ),
                ),
                Anime(
                    _title = "2 - two",
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                    ),
                ),
                Anime(
                    _title = "2 - three",
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "2 - four",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "3 - one",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                        URI("https://kitsu.app/anime/30"),
                    ),
                ),
                Anime(
                    _title = "3 - two",
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                Anime(
                    _title = "3 - three",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://kitsu.app/anime/30"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                ),
                expectedEntryOne,
                expectedEntryTwo,
                Anime(
                    _title = "5",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/24"),
                        URI("https://anilist.co/anime/48"),
                        URI("https://kitsu.app/anime/30"),
                        URI("https://myanimelist.net/anime/48"),
                        URI("https://notify.moe/anime/xpIt5Fiig"),
                    ),
                )
            )

            val clusterService = ClusterService(
                mergeLockAccessor = TestMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = TestReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.fetchCluster(databaseEntries, 4)

            // then
            assertThat(result).containsExactly(expectedEntryOne, expectedEntryTwo)
        }

        @Test
        fun `returns empty list if there are not entries matching the cluster`() {
            // given
            val databaseEntries = listOf(
                Anime(
                    _title = "1 - one",
                    sources = hashSetOf(URI("https://anidb.net/anime/24")),
                ),
            )

            val clusterService = ClusterService(
                mergeLockAccessor = TestMergeLockAccessor,
                reviewedIsolatedEntriesAccessor = TestReviewedIsolatedEntriesAccessor,
            )

            // when
            val result = clusterService.fetchCluster(databaseEntries, 4)

            // then
            assertThat(result).isEmpty()
        }
    }
}