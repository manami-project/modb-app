package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestJavaWatchService
import io.github.manamiproject.modb.app.TestWatchKey
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.nio.file.ClosedWatchServiceException
import java.nio.file.WatchKey
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import java.nio.file.WatchService as JavaWatchService

internal class JavaWatchServiceExtensionsKtTest {

    @Nested
    inner class LongPollTests {

        @Test
        fun `returns null if ClosedWatchServiceException if thrown`() {
            runTest {
                // given
                val watchService = object: JavaWatchService by TestJavaWatchService {
                    override fun poll(): WatchKey = throw ClosedWatchServiceException()
                }

                // when
                val result = watchService.longPoll()

                // then
                assertThat(result).isNull()
            }
        }

        @Test
        fun `correctly returns item`() {
            runTest {
                // given
                val watchService = object: JavaWatchService by TestJavaWatchService {
                    override fun poll(): WatchKey = TestWatchKey
                }

                // when
                val result = watchService.longPoll()

                // then
                assertThat(result).isNotNull()
            }
        }

        @Test
        fun `increases suspension time when waiting for non-null items`() {
            runTest {
                // given
                val noWaitingWatchService = object: JavaWatchService by TestJavaWatchService {
                    override fun poll(): WatchKey = TestWatchKey
                }
                val timeNoWaiting = measureTimeMillis {
                    noWaitingWatchService.longPoll()
                }

                var invocation = 0
                val watchService = object: JavaWatchService by TestJavaWatchService {
                    override fun poll(): WatchKey? {
                        invocation++

                        return if (invocation < 8) {
                            null
                        } else {
                            TestWatchKey
                        }
                    }
                }

                // when
                val result = measureTimeMillis {
                    watchService.longPoll()
                }

                // then
                assertThat(timeNoWaiting).isLessThan(result)
                assertThat(result).isGreaterThan(1000)
            }
        }
    }
}