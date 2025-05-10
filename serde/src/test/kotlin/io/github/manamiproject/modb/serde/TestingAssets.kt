package io.github.manamiproject.modb.serde

import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.serde.json.JsonDeserializer
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URL

internal object TestHttpClient : HttpClient {
    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
}

internal object TestJsonDeserializer : JsonDeserializer<List<Int>> {
    override suspend fun deserialize(json: String): List<Int> = shouldNotBeInvoked()
}