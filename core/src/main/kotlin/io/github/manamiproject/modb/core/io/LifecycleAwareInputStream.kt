package io.github.manamiproject.modb.core.io

import java.io.InputStream
import java.io.OutputStream

/**
 * A wrapper for [java.io.InputStream]s which allows to check if the stream has been closed in order to prevent
 * [java.io.IOException]. Some implementations of [java.io.InputStream]
 * allow users to still operate on the stream afterwards, but most don't. In those cases an [java.io.IOException]
 * is thrown. This wrapper allows the caller to prevent that. The condition is that [close] is called on this class
 * and not on a reference to the [delegate] directly which would bypass this feature.
 * Apart from overriding [close] all other functions are delegated to the given [InputStream].
 * @since 19.0.0
 * @property delegate The actual [InputStream] whose lifecycle is being tracked.
 */
public class LifecycleAwareInputStream(private val delegate: InputStream): InputStream(), LifecycleAwareCloseable {

    private var isClosed = false

    override fun isClosed(): Boolean = isClosed

    override fun close() {
        isClosed = true
        delegate.close()
    }

    override fun read(): Int = delegate.read()

    override fun read(b: ByteArray?): Int = delegate.read(b)

    override fun read(b: ByteArray?, off: Int, len: Int): Int = delegate.read(b, off, len)

    override fun readAllBytes(): ByteArray? = delegate.readAllBytes()

    override fun readNBytes(len: Int): ByteArray? = delegate.readNBytes(len)

    override fun readNBytes(b: ByteArray?, off: Int, len: Int): Int = delegate.readNBytes(b, off, len)

    override fun skip(n: Long): Long = delegate.skip(n)

    override fun skipNBytes(n: Long): Unit = delegate.skipNBytes(n)

    override fun available(): Int = delegate.available()

    override fun mark(readlimit: Int): Unit = delegate.mark(readlimit)

    override fun markSupported(): Boolean = delegate.markSupported()

    override fun reset(): Unit = delegate.reset()

    override fun transferTo(out: OutputStream?): Long = delegate.transferTo(out)

    override fun equals(other: Any?): Boolean = delegate.equals(other)

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()
}