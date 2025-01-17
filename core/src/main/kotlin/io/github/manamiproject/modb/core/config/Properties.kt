package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.core.extensions.EMPTY
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KProperty

/**
 * Configuration parameter returning a [String].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by StringPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp="myValue"
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by StringPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp="myValue"
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class StringPropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (String) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (String) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: String,
        validator: (String) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): String {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.string(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [Long].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by LongPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=1535
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by LongPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=1535
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class LongPropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (Long) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (Long) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: Long,
        validator: (Long) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Long {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.long(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [Int].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by IntPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=1535
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by IntPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=1535
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 16.1.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class IntPropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (Int) -> Boolean,
) {

    /**
     * @since 16.1.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (Int) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 16.1.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: Int,
        validator: (Int) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 16.1.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Int {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.int(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [Boolean].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by BooleanPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=true
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by BooleanPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=true
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class BooleanPropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (Boolean) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (Boolean) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: Boolean,
        validator: (Boolean) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.boolean(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [Double].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by DoublePropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=10.4
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by DoublePropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=10.4
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class DoublePropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (Double) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (Double) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: Double,
        validator: (Double) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Double {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.double(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [LocalDate].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by LocalDatePropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=2024-01-01
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by LocalDatePropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=2024-01-01
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class LocalDatePropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (LocalDate) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (LocalDate) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: LocalDate,
        validator: (LocalDate) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): LocalDate {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.localDate(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [LocalDateTime].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by LocalDateTimePropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=2024-04-04T09:32:00
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by LocalDateTimePropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=2024-04-04T09:32:00
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class LocalDateTimePropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (LocalDateTime) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (LocalDateTime) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: LocalDateTime,
        validator: (LocalDateTime) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): LocalDateTime {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.localDateTime(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [OffsetDateTime].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by OffsetDateTimePropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=2024-04-04T09:32:00+04:00
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by OffsetDateTimePropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=2024-04-04T09:32:00+04:00
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class OffsetDateTimePropertyDelegate private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (OffsetDateTime) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (OffsetDateTime) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: OffsetDateTime,
        validator: (OffsetDateTime) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): OffsetDateTime {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.offsetDateTime(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [List].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by ListPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=[
 *  "one",
 *  "two",
 * ]
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by ListPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=[
 *  "one",
 *  "two",
 * ]
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class ListPropertyDelegate<out T: Any> private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (List<T>) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (List<T>) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: List<T>,
        validator: (List<T>) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.list<T>(key) },
        )

        check(validator(value)) { "Value $value for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [Set].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by SetPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * org.example.project.Test.myProp=[
 *  "one",
 *  "two",
 * ]
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by SetPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * something.different.myProp=[
 *  "one",
 *  "two",
 * ]
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class SetPropertyDelegate<out T: Any> private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (Set<T>) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (Set<T>) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: Set<T>,
        validator: (Set<T>) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Set<T> {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.list<T>(key)?.toSet() },
        )

        check(validator(value)) { "Value $value for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

/**
 * Configuration parameter returning a [Map].
 *
 * # Usage
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by MapPropertyDelegate()
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * [[org.example.project.Test]]
 * myProp={ key1="value1", key2="value2" }
 * ```
 *
 * # Custom namespace
 * Let's assume:
 *
 * ```kotlin
 * package org.example.project
 *
 * class Test {
 *   private val myProp by MapPropertyDelegate(namespace = "something.different")
 * }
 * ```
 *
 * Then your property would look like this:
 *
 * ```toml
 * [[org.example.project.Test]]
 * myProp={ key1="value1", key2="value2" }
 * ```
 *
 * # Testing
 *
 * You can pass a mocked [ConfigRegistry] for testing.
 * @since 13.0.0
 * @property configRegistry Handles the retrieval of the value.
 * @property namespace The prefix of a fully qualified property name. It will internally be extended by the variable name.
 * @property default Default value in case the property cannot be found.
 * @property validator Checks the value and throws an exception if it doesn't pass the check.
 * @throws IllegalStateException if value doesn't pass check from [validator].
 */
public class MapPropertyDelegate<out T: Any> private constructor(
    private val configRegistry: ConfigRegistry,
    private val namespace: String,
    private val default: PropertyDefault,
    private val validator: (Map<String, T>) -> Boolean,
) {

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        validator: (Map<String, T>) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.NoDefault, validator)

    /**
     * @since 13.0.0
     * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry.instance]
     * @param namespace The prefix of a fully qualified property name. It will internally be extended by the variable name. **Default:** [EMPTY]
     * @param default Default value in case the property cannot be found.
     * @param validator Checks the value and throws an exception if it doesn't pass the check. Default always returns `true`.
     * @throws IllegalStateException if value doesn't pass check from [validator].
     */
    public constructor(
        configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
        namespace: String = EMPTY,
        default: Map<String, T>,
        validator: (Map<String, T>) -> Boolean = { true },
    ): this(configRegistry, namespace, PropertyDefault.Default(default), validator)

    /**
     * This allows you to use this class as property delegate using `by` keyword.
     * @since 13.0.0
     * @param thisRef Calling class
     * @param property Property to which the value will be assigned to. It is also part of the fully qualified property name.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Map<String, T> {
        val value = valueFromRegistry(
            thisRef = thisRef,
            property = property,
            namespace = namespace,
            default = default,
            retrieval = { key -> configRegistry.map<T>(key) },
        )

        check(validator(value)) { "Value [$value] for property [$namespace.${property.name}] is invalid." }

        return value
    }
}

private inline fun <reified T> valueFromRegistry(
    thisRef: Any,
    property: KProperty<*>,
    namespace: String,
    default: PropertyDefault,
    crossinline retrieval: (String) -> T?,
): T {
    val namespaceWithoutProperty = when (namespace) {
        EMPTY -> thisRef::class.qualifiedName.toString()
        else -> namespace
    }
    val key = "$namespaceWithoutProperty.${property.name}"

    check("""^[a-zA-Z\d.]+$""".toRegex().matches(key)) { "Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$key]." }
    val value = retrieval(key)

    return when (default) {
        is PropertyDefault.Default<*> -> value ?: default.value as T
        is PropertyDefault.NoDefault -> value ?: throw IllegalStateException("Unable to find property [$key]. Property doesn't have a default set.")
    }
}