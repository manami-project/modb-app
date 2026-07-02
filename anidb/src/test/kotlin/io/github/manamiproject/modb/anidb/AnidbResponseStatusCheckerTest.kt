package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.anidb.AnidbEntryStatus.*
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnidbResponseStatusCheckerTest {

    @Nested
    inner class CheckStatusTests {

        @Test
        fun `status is UNKNOWN`() {
            runTest {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val responseChecker = AnidbResponseStatusChecker(
                    responseBody = "<unknown>",
                    metaDataProviderConfig = testMetaDataProviderConfig,
                )

                // when
                val result = responseChecker.checkStatus()

                // then
                assertThat(result).isEqualTo(UNKNOWN)
            }
        }

        @Test
        fun `status is EXISTS`() {
            runTest {
                // given
                val responseChecker = AnidbResponseStatusChecker(
                    responseBody = loadTestResource<String>("AnidbResponseStatusCheckerTest/exists.xml"),
                )

                // when
                val result = responseChecker.checkStatus()

                // then
                assertThat(result).isEqualTo(EXISTS)
            }
        }

        @Test
        fun `status is NOT_FOUND`() {
            runTest {
                // given
                val responseChecker = AnidbResponseStatusChecker(
                    responseBody = loadTestResource<String>("AnidbResponseStatusCheckerTest/not_found.xml"),
                )

                // when
                val result = responseChecker.checkStatus()

                // then
                assertThat(result).isEqualTo(NOT_FOUND)
            }
        }

        @Test
        fun `status is ADDITION_PENDING`() {
            runTest {
                // given
                val responseChecker = AnidbResponseStatusChecker(
                    responseBody = loadTestResource<String>("AnidbResponseStatusCheckerTest/addition_pending.html"),
                    metaDataProviderConfig = AnidbWebViewConfig,
                )

                // when
                val result = responseChecker.checkStatus()

                // then
                assertThat(result).isEqualTo(ADDITION_PENDING)
            }
        }

        @Test
        fun `status is DELETED`() {
            runTest {
                // given
                val responseChecker = AnidbResponseStatusChecker(
                    responseBody = loadTestResource<String>("AnidbResponseStatusCheckerTest/deleted.html"),
                    metaDataProviderConfig = AnidbWebViewConfig,
                )

                // when
                val result = responseChecker.checkStatus()

                // then
                assertThat(result).isEqualTo(DELETED)
            }
        }

        @Test
        fun `status is HENTAI`() {
            runTest {
                // given
                val responseChecker = AnidbResponseStatusChecker(
                    responseBody = loadTestResource<String>("AnidbResponseStatusCheckerTest/hentai.html"),
                    metaDataProviderConfig = AnidbWebViewConfig,
                )

                // when
                val result = responseChecker.checkStatus()

                // then
                assertThat(result).isEqualTo(HENTAI)
            }
        }
    }
}