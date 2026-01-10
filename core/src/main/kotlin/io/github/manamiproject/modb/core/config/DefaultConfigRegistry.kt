package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.loadResource
import io.github.manamiproject.modb.core.resourceFileExists
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlParseResult
import org.tomlj.TomlTable
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import kotlin.io.path.Path

/**
 * Default implementation of [ConfigRegistry]. Handling is as follows:
 *
 * 1. Loads `config.toml` from classpath if it exists.
 * 2. Loads configuration from `config.toml` if it exists in the same directory of the file system and overrides
 * existing properties.
 * 3. If environment variable `modb.core.config.location` is set and directs to a valid `config.toml` then this file will
 * be loaded even if there is a `config.toml` in the same directory. Setting a system property with the same key will
 * take precedence over the environment variable. Loading that file will override any existing properties as well.
 * 4. If you request a property then the implementation checks if an environment variable with that key exists and
 * returns the value instead of the value defined in any of the steps before.
 * 5. System properties have the highest precedence. If you request a property whose key has been passed as a system
 * property then this value will be used instead of any value defined in any previous step.
 *
 * The files are loaded once when the class is initialized. The existence of environment variables and system properties
 * are checked with each function call.
 *
 * Setting environment variables and system properties is not supported for [Map]s.
 *
 * Property names are supposed to consist of alphanumeric chars and dots.
 *
 * The implementation includes some possible casts.
 * Example: You can retrieve a value directly as [Long] if you call [long] on a property of type [String]. This, of
 * course, only works if the value can be cast from [String] to [Long].
 * Or you could call [list] on a single value which is then returned as a [List] with one value in it.
 * @since 15.0.0
 * @property classPathFile Path of the config file within the classpath.
 * @property localFile Path to a configuration file in the local file system.
 * @property environmentVariables Initially loads the environment variables. Because these are immutable and impossible
 * to set via reflection without override parameter for the JVM those can be accessed and changed later on.
 * @property systemProperties System properties passed to the app which take precedence over any other declaration.
 */
public class DefaultConfigRegistry @KoverIgnore constructor(
    private val classPathFile: String = CONFIG_FILE,
    private val localFile: Path = Path(CONFIG_FILE),
    private val environmentVariables: Map<String, String> = System.getenv(),
    private val systemProperties: Map<String, String> = System.getProperties()
        .filter { it.key is String}
        .filter{ it.value != null }
        .filter { it.value is String }
        .map { it.key as String to it.value as String }
        .toMap(),
): ConfigRegistry {

    private val properties = mutableMapOf<String, Any?>()
    private var isInitialized = false
    private val initializationMutex = Mutex()

    override fun string(key: String): String? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        return fetchProperty(key)?.toString()
    }

    override fun long(key: String): Long? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is Long -> value
            else -> value.toString().toLongOrNull()
        }
    }

    override fun int(key: String): Int? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is Int -> value
            else -> value.toString().toIntOrNull()
        }
    }

    override fun boolean(key: String): Boolean? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is Boolean -> value
            else -> value.toString().toBooleanStrictOrNull()
        }
    }

    override fun double(key: String): Double? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is Double -> value
            else -> value.toString().toDoubleOrNull()
        }
    }

    override fun localDate(key: String): LocalDate? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is LocalDate -> value
            else -> parseToLocalDateOrNull(value)
        }
    }

    override fun localDateTime(key: String): LocalDateTime? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is LocalDateTime -> value
            else -> parseToLocalDateTimeOrNull(value)
        }
    }

    override fun offsetDateTime(key: String): OffsetDateTime? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when {
            value is OffsetDateTime -> value
            else -> parseToOffsetDateTimeOrNull(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> list(key: String): List<T>? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when(value) {
            is Collection<*> -> value.toList() as List<T>
            else -> listOf(value as T)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> map(key: String): Map<String, T>? {
        if (!isInitialized) {
            runBlocking { init() }
        }

        val value = fetchProperty(key) ?: return null

        return when(value) {
            is Map<*, *> -> value.toMap() as Map<String, T>
            else -> null
        }
    }

    private suspend fun init() {
        initializationMutex.withLock {
            if (!isInitialized) {
                loadConfigFromClassPath()
                loadingConfigFromLocalFile()
                isInitialized = true
            }
        }
    }

    private suspend fun loadConfigFromClassPath() {
        if (resourceFileExists(classPathFile) && classPathFile.endsWith(CONFIG_FILE)) {
            deserializeToml(loadResource(classPathFile)).forEach { (key, value) ->
                properties[key] = value
            }
            println("config.toml from classpath: ✅")
        } else {
            println("config.toml from classpath: ❌")
        }
    }

    private suspend fun loadingConfigFromLocalFile() {
        val overriddenPath = if (systemProperties.containsKey(CONFIG_FILE_PATH_PROPERTY_KEY)) {
            systemProperties.getOrDefault(CONFIG_FILE_PATH_PROPERTY_KEY, EMPTY)
        } else {
            environmentVariables.getOrDefault(CONFIG_FILE_PATH_PROPERTY_KEY, EMPTY)
        }
        val isValidOverriddenPath = overriddenPath.neitherNullNorBlank() && overriddenPath.endsWith(CONFIG_FILE)

        when {
            localFile.regularFileExists() && localFile.fileName().endsWith(CONFIG_FILE) && !isValidOverriddenPath -> {
                deserializeToml(localFile.readFile()).forEach { (key, value) ->
                    properties[key] = value
                }
                println("config.toml from local file: ✅")
            }
            isValidOverriddenPath -> {
                val file = Path(overriddenPath)
                when {
                    file.regularFileExists() -> {
                        deserializeToml(file.readFile()).forEach { (key, value) ->
                            properties[key] = value
                        }
                        println("config.toml from local file [${file.toAbsolutePath()}] set via environment variable: ✅")
                    }
                    else -> println("config.toml from local file: ❌")
                }
            }
            else -> println("config.toml from local file: ❌")
        }
    }

    private fun fetchProperty(key: String): Any? {
        if (systemProperties.containsKey(key)) {
            println("System property taking precedence over any other configuration for: [$key]")
            return systemProperties[key]!!
        }

        if (environmentVariables.containsKey(key)) {
            println("Environment variable taking precedence over any other configuration for: [$key]")
            return environmentVariables[key]!!
        }

        return properties[key]
    }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeToml(fileContent: String): Map<String, Any?> {
        val result: TomlParseResult = Toml.parse(fileContent)

        if (result.errors().isNotEmpty()) {
            throw IllegalStateException("Error parsing TOML:\n${result.errors().joinToString("\n") { "- ${it.message}" }}")
        }

        val ret = mutableMapOf<String, Any>()

        result.dottedEntrySet().filter { it.value != null }.forEach { (key, value) ->
            when (value) {
                is TomlArray -> {
                    when {
                        value.size() == 1 && value[0] is TomlTable -> {
                            (value[0] as TomlTable).entryPathSet().map { entry ->
                                val innerKey = entry.key.removeLast()
                                val newKey = "$key.${entry.key.joinToString(".") { it }}"

                                if (ret.containsKey(newKey)) {
                                    (ret[newKey] as MutableMap<String, Any>)[innerKey] = entry.value
                                } else {
                                    ret[newKey] = mutableMapOf(innerKey to entry.value)
                                }
                            }
                        }
                        else -> ret[key] = value.toList()
                    }
                }
                else -> ret[key] = value
            }
        }

        return ret
    }

    private fun parseToLocalDateOrNull(obj: Any?): LocalDate? {
        return try {
            LocalDate.parse(obj.toString())
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun parseToLocalDateTimeOrNull(obj: Any?): LocalDateTime? {
        return try {
            LocalDateTime.parse(obj.toString())
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun parseToOffsetDateTimeOrNull(obj: Any?): OffsetDateTime? {
        return try {
            OffsetDateTime.parse(obj.toString())
        } catch (e: DateTimeParseException) {
            null
        }
    }

    public companion object {
        private const val CONFIG_FILE_PATH_PROPERTY_KEY: String = "modb.core.config.location"
        private const val CONFIG_FILE = "config.toml"

        /**
         * Singleton of [DefaultConfigRegistry]
         * @since 15.0.0
         */
        public val instance: DefaultConfigRegistry by lazy { DefaultConfigRegistry() }
    }
}