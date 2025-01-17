package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class ContextAwareKtTest {

    @Test
    fun `default for isTestContext is false`() {
        // given
        val testConfig = object: MetaDataProviderConfig {
            override fun hostname(): Hostname = shouldNotBeInvoked()
            override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
        }

        // when
        val result = testConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }
}