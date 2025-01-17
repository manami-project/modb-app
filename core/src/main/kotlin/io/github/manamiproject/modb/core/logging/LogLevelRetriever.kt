package io.github.manamiproject.modb.core.logging

internal interface LogLevelRetriever {

    fun logLevel(): LogLevel
}