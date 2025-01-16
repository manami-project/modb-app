package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.httpclient.RequestBody
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test
import java.net.URL

internal class AnilistHeaderCreatorTest {

    @Test
    fun `default referer`() {
        // given
        AnilistDefaultTokenRepository.token = AnilistToken("cookie-value", "csrf-token-value")

        val requestBody = RequestBody(
            mediaType = "used media type",
            body = "payload",
        )

        // when
        val result = AnilistHeaderCreator.createAnilistHeaders(
            requestBody = requestBody,
        )

        // then
        assertThat(result["authority"]).isEqualTo(listOf("https://anilist.co"))
        assertThat(result["method"]).isEqualTo(listOf("POST"))
        assertThat(result["path"]).isEqualTo(listOf("/graphql"))
        assertThat(result["scheme"]).isEqualTo(listOf("https"))
        assertThat(result["accept"]).isEqualTo(listOf("*/*"))
        assertThat(result["accept-language"]).isEqualTo(listOf("en-US;q=0.9", "en;q=0.8"))
        assertThat(result["content-length"]).isEqualTo(listOf("7"))
        assertThat(result["content-type"]).isEqualTo(listOf("used media type"))
        assertThat(result["cookie"]).isEqualTo(listOf("cookie-value"))
        assertThat(result["origin"]).isEqualTo(listOf("https://anilist.co"))
        assertThat(result["schema"]).isEqualTo(listOf("default"))
        assertThat(result["x-csrf-token"]).isEqualTo(listOf("csrf-token-value"))
        assertThat(result["referer"]).isEqualTo(listOf("https://anilist.co"))
    }

    @Test
    fun `override referer`() {
        // given
        val requestBody = RequestBody(
            mediaType = "used media type",
            body = "payload",
        )

        // when
        val result = AnilistHeaderCreator.createAnilistHeaders(
            requestBody = requestBody,
            referer = URI("https://anilist.co/anime/1535").toURL(),
        )

        // then
        assertThat(result["referer"]).isEqualTo(listOf("https://anilist.co/anime/1535"))
    }
}