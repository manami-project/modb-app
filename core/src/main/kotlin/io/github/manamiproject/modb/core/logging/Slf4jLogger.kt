package io.github.manamiproject.modb.core.logging

import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import kotlin.reflect.KClass

internal class Slf4jLogger(
    ref: KClass<*>,
    private val slf4jLogger: org.slf4j.Logger = LoggerFactory.getLogger(ref.java),
): Logger {

    init {
        SLF4JBridgeHandler.install()
    }

    override fun error(message: () -> String) = slf4jLogger.error(message.invoke())

    override fun error(exception: Throwable, message: () -> String) = slf4jLogger.error(message.invoke())

    override fun warn(message: () -> String) = slf4jLogger.warn(message.invoke())

    override fun warn(exception: Throwable, message: () -> String) = slf4jLogger.warn(message.invoke())

    override fun info(message: () -> String) = slf4jLogger.info(message.invoke())

    override fun debug(message: () -> String) = slf4jLogger.debug(message.invoke())

    override fun trace(message: () -> String) = slf4jLogger.trace(message.invoke())
}