package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

internal object TestMetaDataProviderConfig : MetaDataProviderConfig {
    override fun isTestContext(): Boolean = true
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestConfigRegistry : ConfigRegistry {
    override fun string(key: String): String = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun int(key: String): Int = shouldNotBeInvoked()
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun <T : Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun <T : Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
}