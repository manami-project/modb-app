package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

/**
 * Constant for empty [String].
 * @since 1.0.0
 */
public const val EMPTY: String = ""

/**
 * File suffix of lock files indicating that a file is currently being written.
 * @since 1.0.0
 */
public const val LOCK_FILE_SUFFIX: String = "lck"

/**
 * Writes a [String] into a [RegularFile]. If the file already exists it will be overwritten.
 * @since 8.0.0
 * @param file The file to which you want to write the given [String].
 * @param writeLockFile You can choose to write an empty lock file which indicates that the file is currently being created.
 * First the empty lock file is created using [LOCK_FILE_SUFFIX]. Then the actual file is being written. After that the lock file is deleted again.
 * **Default** is `false`.
 * @throws IllegalStateException if the given [String] is blank.
 * @receiver Any non-nullable [String].
 */
public suspend fun String.writeToFile(file: RegularFile, writeLockFile: Boolean = false) {
    val content = this

    withContext(LIMITED_FS) {
        check(content.neitherNullNorBlank()) { "Tried to write file [$file], but the String was blank." }

        val lockFile = file.changeSuffix(LOCK_FILE_SUFFIX)

        if (writeLockFile) {
            lockFile.createFile()
        }

        file.writeText(content)

        if (lockFile.regularFileExists() && writeLockFile) {
            lockFile.deleteIfExists()
        }
    }
}

/**
 * Removes all occurrences of [value] in a given [String].
 * @since 5.3.0
 * @param value The value that is supposed to be removed from the given [String].
 * @param ignoreCase Whether to operate case sensitive or not. **Default:** `false`.
 * @param normalizeWhitespaces If set to true multiple consective whitespaces will be replaced with a single one.
 * @return The [String] without the occurrences of [value].
 * @receiver Any non-nullable [String].
 */
public fun String.remove(value: String, ignoreCase: Boolean = false, normalizeWhitespaces: Boolean = false): String {
    var cleanedValue = this.replace(value, EMPTY, ignoreCase)

    if (normalizeWhitespaces) {
        cleanedValue = cleanedValue.normalizeWhitespaces()
    }

    return cleanedValue
}

/**
 * Replaces multiple consecutive whitespaces with a single one, trims the string and replaces different types of
 * whitespaces (not line breaks or tabs) to default whitespaces.
 *
 * **Replaces:**
 * - no-break space
 * - narrow no-break space
 * - hair space
 * - medium mathematical space (MMSP)
 * - en quad
 * - em quad
 * - en space
 * - em space
 * - three-per-em space
 * - four-per-em space
 * - six-per-em space
 * - figure space
 * - punctuation space
 * - thin space
 *
 * **Removes:**
 * - zero width no-break space
 * - mongolian vowel separator
 * - word joiner
 * - zero-width joiner
 * - device control string
 * - zero-width non-joiner
 * - zero-width space
 * - soft hyphen (SHY)
 * - form feed
 * - line separator
 * @since 5.3.0
 * @return The original [String], but trimmed and having a single whitespace in places where it had multiple consecutive whitespaces before and containing only default whitespaces.
 * @receiver Any non-nullable [String].
 */
public fun String.normalizeWhitespaces(): String = this.replace('\u00A0', ' ') // no-break space
    .replace('\u202F', ' ') // narrow no-break space
    .replace('\u200A', ' ') // hair space
    .replace('\u205F', ' ') // medium mathematical space (MMSP)
    .replace('\u2000', ' ') // en quad
    .replace('\u2001', ' ') // em quad
    .replace('\u2002', ' ') // en space
    .replace('\u2003', ' ') // em space
    .replace('\u2004', ' ') // three-per-em space
    .replace('\u2005', ' ') // four-per-em space
    .replace('\u2006', ' ') // six-per-em space
    .replace('\u2007', ' ') // figure space
    .replace('\u2008', ' ') // punctuation space
    .replace('\u2009', ' ') // thin space
    // remove non-visible chars
    .remove("\uFEFF") // zero width no-break space
    .remove("\u180E") // mongolian vowel separator
    .remove("\u2060") // word joiner
    .remove("\u200D") // zero-width joiner
    .remove("\u0090") // device control string
    .remove("\u200C") // zero-width non-joiner
    .remove("\u200B") // zero-width space
    .remove("\u00AD") // soft hyphen (SHY)
    .remove("\u000C") // form feed
    .remove("\u2028") // line separator
    // reduce multiple consecutive whitespaces to a single whitespace
    .replace(Regex(" {2,}"), " ")
    .trim()


/**
 * Removes line breaks, tabs and normalizes whitespaces.
 * @since 12.1.0
 * @return The original [String] having tabs, carriage return and line feed replaced with whitespaces and all whitespace types replaced with the default.
 * @receiver Any non-nullable [String].
 * @see normalizeWhitespaces
 */
public fun String.normalize(): String = this.replace('\r', ' ')
    .replace('\n', ' ')
    .replace('\t', ' ')
    .normalizeWhitespaces()


public fun String?.eitherNullOrBlank(): Boolean {
    if (this == null) {
        return true
    }

    if (this.isEmpty()) {
        return true
    }

    return """^[\u00A0\u202F\u200A\u205F\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\uFEFF\u180E\u2060\u200D\u0090\u200C\u200B\u00AD\u000C\u2028\r\n\t ]*$""".toRegex().matches(this)
}

@OptIn(ExperimentalContracts::class)
public fun String?.neitherNullNorBlank(): Boolean {
    contract {
        returns(false) implies (this@neitherNullNorBlank != null)
    }

    return !this.eitherNullOrBlank()
}