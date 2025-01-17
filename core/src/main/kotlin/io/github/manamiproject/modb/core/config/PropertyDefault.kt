package io.github.manamiproject.modb.core.config

internal sealed class PropertyDefault {

    data class Default<out T>(val value: T): PropertyDefault()

    data object NoDefault: PropertyDefault()
}

