package io.github.manamiproject.modb.core.models

import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DurationKtTest {

    @Nested
    inner class InitializationTests {

        @Test
        fun `initializing duration using seconds`() {
            // given
            val value = 120

            // when
            val duration = Duration(value, SECONDS)

            // then
            assertThat(duration.duration).isEqualTo(value)
        }

        @Test
        fun `initializing duration using minutes`() {
            // given
            val value = 2
            val expectedOutput = 120

            // when
            val duration = Duration(value, MINUTES)

            // then
            assertThat(duration.duration).isEqualTo(expectedOutput)
        }

        @Test
        fun `initializing duration using hours`() {
            // given
            val value = 1
            val expectedOutput = 3600

            // when
            val duration = Duration(value, HOURS)

            // then
            assertThat(duration.duration).isEqualTo(expectedOutput)
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `checking equality - 1 minute is equal to 60 seconds`() {
            // given
            val durationInSeconds = Duration(60, SECONDS)
            val durationInMinutes = Duration(1, MINUTES)

            // when
            val result = durationInSeconds == durationInMinutes

            // then
            assertThat(result).isTrue()
            assertThat(durationInSeconds.hashCode()).isEqualTo(durationInMinutes.hashCode())
        }

        @Test
        fun `checking equality - 1 hour is equal to 60 minutes`() {
            // given
            val durationInMinutes = Duration(60, MINUTES)
            val durationInHours = Duration(1, HOURS)

            // when
            val result = durationInMinutes == durationInHours

            // then
            assertThat(result).isTrue()
            assertThat(durationInMinutes.hashCode()).isEqualTo(durationInHours.hashCode())
        }

        @Test
        fun `checking equality - duration is not equal`() {
            // given
            val aSecond = Duration(1, SECONDS)
            val tenSeconds = Duration(10, SECONDS)

            // when
            val result = aSecond == tenSeconds

            // then
            assertThat(result).isFalse()
            assertThat(aSecond.hashCode()).isNotEqualTo(tenSeconds.hashCode())
        }

        @Test
        fun `duration cannot be equal to null`() {
            // given
            val durationInMinutes = Duration(60, MINUTES)

            // when
            val result = durationInMinutes.equals(null)

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `duration cannot be equal to different type`() {
            // given
            val durationInMinutes = Duration(60, MINUTES)
            val other = 60

            // when
            val result = durationInMinutes.equals(other)

            // then
            assertThat(result).isFalse()
            assertThat(durationInMinutes.hashCode()).isNotEqualTo(other.hashCode())
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `initializing duration using seconds, but toString prints out seconds`() {
            // given
            val value = 120

            // when
            val result = Duration(value, SECONDS).toString()

            // then
            assertThat(result).isEqualTo("$value seconds")
        }

        @Test
        fun `initializing duration using minutes, but toString prints out seconds`() {
            // given
            val value = 2

            // when
            val result = Duration(value, MINUTES).toString()

            // then
            assertThat(result).isEqualTo("120 seconds")
        }

        @Test
        fun `initializing duration using hours, but toString prints out seconds`() {
            // given
            val value = 1

            // when
            val result = Duration(value, HOURS).toString()

            // then
            assertThat(result).isEqualTo("3600 seconds")
        }
    }
}