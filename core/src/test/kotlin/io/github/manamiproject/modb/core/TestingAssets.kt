package io.github.manamiproject.modb.core

import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.OutputKey
import io.github.manamiproject.modb.core.extractor.Selector
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.*

internal object MetaDataProviderTestConfig : MetaDataProviderConfig {
    override fun isTestContext(): Boolean = true
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestAnimeConverter : AnimeConverter {
    override suspend fun convert(rawContent: String) = shouldNotBeInvoked()
}

internal object TestDataExtractor : DataExtractor {
    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult = shouldNotBeInvoked()
}

internal object TestConfigRegistry: ConfigRegistry {
    override fun string(key: String): String = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun int(key: String): Int = shouldNotBeInvoked()
    override fun <T: Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun <T: Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
}

internal class TestKProperty<T>: KProperty<T> {
    override val annotations: List<Annotation>
        get() = shouldNotBeInvoked()
    override val getter: KProperty.Getter<T>
        get() = shouldNotBeInvoked()
    override val isAbstract: Boolean
        get() = shouldNotBeInvoked()
    override val isConst: Boolean
        get() = shouldNotBeInvoked()
    override val isFinal: Boolean
        get() = shouldNotBeInvoked()
    override val isLateinit: Boolean
        get() = shouldNotBeInvoked()
    override val isOpen: Boolean
        get() = shouldNotBeInvoked()
    override val isSuspend: Boolean
        get() = shouldNotBeInvoked()
    override val name: String
        get() = shouldNotBeInvoked()
    override val parameters: List<KParameter>
        get() = shouldNotBeInvoked()
    override val returnType: KType
        get() = shouldNotBeInvoked()
    override val typeParameters: List<KTypeParameter>
        get() = shouldNotBeInvoked()
    override val visibility: KVisibility?
        get() = shouldNotBeInvoked()
    override fun call(vararg args: Any?): T = shouldNotBeInvoked()
    override fun callBy(args: Map<KParameter, Any?>): T = shouldNotBeInvoked()
}