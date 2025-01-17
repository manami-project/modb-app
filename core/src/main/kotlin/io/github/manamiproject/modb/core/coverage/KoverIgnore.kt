package io.github.manamiproject.modb.core.coverage

@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.FILE,
    AnnotationTarget.TYPEALIAS,
)
/**
 * Allows to ignore code from kover coverage reports.
 * Place in your code and activate by adding the following to the `build.gradle.kts`:
 * ```kotlin
 * kover {
 *     reports {
 *         filters {
 *             excludes {
 *                 annotatedBy("io.github.manamiproject.modb.core.coverage.KoverIgnore")
 *             }
 *         }
 *     }
 * }
 * ```
 * @since 16.3.0
 */
public annotation class KoverIgnore