package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class MetaDataProviderConfigExtensionsKtTest {

    @Nested
    inner class IdentityHashCodeTests {

        @Test
        fun `returns the same value for objects`() {
            // given
            val expected = MyanimelistConfig.identityHashCode()

            // when
            val result = MyanimelistConfig.identityHashCode()

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `returns different values for anonymous objects`() {
            // given
            val one = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
                override fun fileSuffix(): FileSuffix = "html"
            }
            val expected = one.identityHashCode()

            val two = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
                override fun fileSuffix(): FileSuffix = "html"
            }

            // when
            val result = two.identityHashCode()

            // then
            assertThat(result).isNotEqualTo(expected)
        }
    }
}