package io.github.manamiproject.modb.core.io

import io.github.manamiproject.modb.core.TestInputStream
import io.github.manamiproject.modb.core.TestReadOnceInputStream
import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.io.OutputStream
import kotlin.test.Test

internal class LifecycleAwareInputStreamTest {

    @Nested
    inner class IsClosedTests {

        @Test
        fun `returns true if the stream has been closed`() {
            // given
            val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream("test".byteInputStream())).apply {
                close()
            }

            // when
            val result = inputStream.isClosed()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if the stream is available`() {
            // given
            val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream("test".byteInputStream()))

            // when
            val result = inputStream.isClosed()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false if the delegate has been closed outside`() {
            // given
            val delegate = TestReadOnceInputStream("test".byteInputStream())
            val inputStream = LifecycleAwareInputStream(delegate)
            delegate.close()

            // when
            val result = inputStream.isClosed()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class CloseTests {

        @Test
        fun `tracks status internally, but delegates close call - no exception is thrown`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun close() {
                    isCallDelegated = true
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream).apply {
                close()
            }

            // when
            val before = inputStream.isClosed()
            inputStream.close()
            val after = inputStream.isClosed()

            // then
            assertThat(before).isTrue()
            assertThat(isCallDelegated).isTrue()
            assertThat(after).isTrue()
        }
    }

    @Nested
    inner class ReadTests {

        @Test
        fun `delegates calls for read`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun read(): Int {
                    isCallDelegated = true
                    return -1
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.read()

            // then
            assertThat(result).isEqualTo(-1)
            assertThat(isCallDelegated).isTrue()
        }

        @Test
        fun `delegates calls for read with ByteArray`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun read(b: ByteArray?): Int {
                    isCallDelegated = true
                    return -1
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.read(EMPTY.toByteArray())

            // then
            assertThat(result).isEqualTo(-1)
            assertThat(isCallDelegated).isTrue()
        }

        @Test
        fun `delegates calls for read with ByteArray Int Int`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun read(b: ByteArray?, off: Int, len: Int): Int {
                    isCallDelegated = true
                    return -1
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.read(EMPTY.toByteArray(), 1, 1)

            // then
            assertThat(result).isEqualTo(-1)
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class ReadAllBytesTests {

        @Test
        fun `delegates calls for readAllBytes`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun readAllBytes(): ByteArray? {
                    isCallDelegated = true
                    return EMPTY.toByteArray()
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.readAllBytes()

            // then
            assertThat(result).isEmpty()
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class ReadNBytesTests {

        @Test
        fun `delegates calls for readNBytes with Int`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun readNBytes(len: Int): ByteArray? {
                    isCallDelegated = true
                    return EMPTY.toByteArray()
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.readNBytes(1)

            // then
            assertThat(result).isEmpty()
            assertThat(isCallDelegated).isTrue()
        }

        @Test
        fun `delegates calls for readNBytes with ByteArray Int Int`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun readNBytes(b: ByteArray?, off: Int, len: Int): Int {
                    isCallDelegated = true
                    return -1
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.readNBytes(EMPTY.toByteArray(),1, 1)

            // then
            assertThat(result).isEqualTo(-1)
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class SkipTests {

        @Test
        fun `delegates calls for skip`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun skip(n: Long): Long {
                    isCallDelegated = true
                    return 2L
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.skip(1L)

            // then
            assertThat(result).isEqualTo(2L)
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class SkipNBytesTests {

        @Test
        fun `delegates calls for skipNBytes`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun skipNBytes(n: Long) {
                    isCallDelegated = true
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            inputStream.skipNBytes(1L)

            // then
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class AvailableTests {

        @Test
        fun `delegates calls for available`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun available(): Int {
                    isCallDelegated = true
                    return 0
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.available()

            // then
            assertThat(result).isZero()
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class MarkTests {

        @Test
        fun `delegates calls for mark`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun mark(readlimit: Int) {
                    isCallDelegated = true
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            inputStream.mark(1)

            // then
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class MarkSupportedTests {

        @Test
        fun `delegates calls for markSupported`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun markSupported(): Boolean {
                    isCallDelegated = true
                    return true
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.markSupported()

            // then
            assertThat(result).isTrue
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class ResetTests {

        @Test
        fun `delegates calls for reset`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun reset() {
                    isCallDelegated = true
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            inputStream.reset()

            // then
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class TransferToTests {

        @Test
        fun `delegates calls for transferTo`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun transferTo(out: OutputStream?): Long {
                    isCallDelegated = true
                    return 0L
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.transferTo(OutputStream.nullOutputStream())

            // then
            assertThat(result).isZero()
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class EqualsTests {

        @Test
        fun `delegates calls for equals - returns true although objects are different`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun equals(other: Any?): Boolean {
                    isCallDelegated = true
                    return true
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.equals(EMPTY)

            // then
            assertThat(result).isTrue()
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class HashCodeTests {

        @Test
        fun `delegates calls for hashCode`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun hashCode(): Int {
                    isCallDelegated = true
                    return 37
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.hashCode()

            // then
            assertThat(result).isEqualTo(37)
            assertThat(isCallDelegated).isTrue()
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `delegates calls for toString`() {
            // given
            var isCallDelegated = false
            val testInputStream = object: TestInputStream() {
                override fun toString(): String {
                    isCallDelegated = true
                    return "from testInputStream"
                }
            }

            val inputStream = LifecycleAwareInputStream(testInputStream)

            // when
            val result = inputStream.toString()

            // then
            assertThat(result).isEqualTo("from testInputStream")
            assertThat(isCallDelegated).isTrue()
        }
    }
}