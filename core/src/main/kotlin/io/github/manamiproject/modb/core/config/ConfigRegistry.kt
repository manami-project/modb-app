package io.github.manamiproject.modb.core.config

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

/**
 * Retrieves configuration parameters.
 * @since 13.0.0
 */
public interface ConfigRegistry {

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [String] or `null` if the parameter is not set.
     */
    public fun string(key: String): String?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [Long] or `null` if the parameter is not set.
     */
    public fun long(key: String): Long?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [Int] or `null` if the parameter is not set.
     */
    public fun int(key: String): Int?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [Boolean] or `null` if the parameter is not set.
     */
    public fun boolean(key: String): Boolean?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [Double] or `null` if the parameter is not set.
     */
    public fun double(key: String): Double?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [LocalDate] or `null` if the parameter is not set.
     */
    public fun localDate(key: String): LocalDate?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [LocalDateTime] or `null` if the parameter is not set.
     */
    public fun localDateTime(key: String): LocalDateTime?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [OffsetDateTime] or `null` if the parameter is not set.
     */
    public fun offsetDateTime(key: String): OffsetDateTime?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [List] or `null` if the parameter is not set.
     */
    public fun <T: Any> list(key: String): List<T>?

    /**
     * @since 13.0.0
     * @param key Identifier of the configuration parameter.
     * @return Either the value of the configuration parameter presented as [Map] or `null` if the parameter is not set.
     */
    public fun <T: Any> map(key: String): Map<String, T>?
}