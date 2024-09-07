package io.github.manamiproject.modb.app.extensions

import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import kotlin.reflect.KClass

/**
 * Checks the HTTP response and returns the body as [String] if everything is valid.
 * @since 1.0.0
 * @param thisRef Calling class. This allows to locate the problem if one of the checks fails.
 * @return Response body as [String].
 * @receiver Any non-null [HttpResponse].
 * @throws IllegalStateException if one of the checks fails. The response code must be 200 and the body musn't be blank.
 */
internal fun HttpResponse.checkedBody(thisRef: KClass<*>): String {
    check(isOk()) { "${thisRef.simpleName}: Response code [${code}] is not 200." }
    check(bodyAsText.neitherNullNorBlank()) { "${thisRef.simpleName}: Response body was blank." }
    return bodyAsText
}