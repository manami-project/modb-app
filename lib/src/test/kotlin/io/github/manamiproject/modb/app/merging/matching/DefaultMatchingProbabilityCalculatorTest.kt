package io.github.manamiproject.modb.app.merging.matching

import io.github.manamiproject.modb.app.merging.goldenrecords.PotentialGoldenRecord
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Status.UPCOMING
import io.github.manamiproject.modb.core.models.Anime.Type.MOVIE
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.SPRING
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.URI
import java.util.UUID.randomUUID
import kotlin.test.Test

internal class DefaultMatchingProbabilityCalculatorTest {

    @Nested
    inner class CalculateTests {

        @Nested
        inner class BasePropertiesTests {

            @Test
            fun `matches 100 percent with basic properties, because the anime for is the same as in the potential golden record`() {
                // given
                val anime = Anime("Cowboy Bebop")

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy()
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.anime).isEqualTo(anime)
                assertThat(result.potentialGoldenRecord).isEqualTo(potentialGoldenRecord)
                assertThat(result.matchProbability).isEqualTo(1.0)
            }

            @Test
            fun `title differs - matches 93 percent`() {
                // given
                val anime = Anime("Cowboy Bebop")

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        _title = "- Cowboy Bebop -",
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.93)
            }

            @Test
            fun `type differs - matches 66 percent`() {
                // given
                val anime = Anime(
                    _title = "Cowboy Bebop",
                    type = TV,
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        type = MOVIE,
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.66)
            }

            @ParameterizedTest
            @CsvSource(
                "SPECIAL,ONA",
                "ONA,SPECIAL",
            )
            fun `type within special and ona - matches 80 percent`(animeType: String, potentialGoldenRecordType: String) {
                // given
                val anime = Anime(
                    _title = "Cowboy Bebop",
                    type = Anime.Type.of(animeType),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        type = Anime.Type.of(potentialGoldenRecordType),
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.8)
            }

            @Test
            fun `episodes differ - matches 98 percent`() {
                // given
                val anime = Anime("Cowboy Bebop")

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        episodes = 1,
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.anime).isEqualTo(anime)
                assertThat(result.potentialGoldenRecord).isEqualTo(potentialGoldenRecord)
                assertThat(result.matchProbability).isEqualTo(0.98)
            }

            @Test
            fun `completely different - matches 0 percent`() {
                // given
                val anime = Anime(
                    _title = "ABC",
                    episodes = 1,
                    type = MOVIE,
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = Anime(
                        _title = "DEF",
                        episodes = 1500,
                        type = TV,
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.anime).isEqualTo(anime)
                assertThat(result.potentialGoldenRecord).isEqualTo(potentialGoldenRecord)
                assertThat(result.matchProbability).isEqualTo(0.0)
            }

            @ParameterizedTest
            @CsvSource(
                "FINISHED,UNKNOWN",
                "UNKNOWN,FINISHED",
            )
            fun `ignore status if one of one of the two is UNKNOWN`(animeStatus: String, potentialGoldenRecordStatus: String) {
                // given
                val anime = Anime(
                    _title = "Cowboy Bebop",
                    status = Anime.Status.of(animeStatus),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        status = Anime.Status.of(potentialGoldenRecordStatus),
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.anime).isEqualTo(anime)
                assertThat(result.potentialGoldenRecord).isEqualTo(potentialGoldenRecord)
                assertThat(result.matchProbability).isEqualTo(1.0)
            }

            @ParameterizedTest
            @CsvSource(
                "1998,0",
                "0,1998",
            )
            fun `ignore yearOfPremiere if one of one of the two is UNKNOWN`(animeYear: String, potentialGoldenRecordYear: String) {
                // given
                val anime = Anime(
                    _title = "Cowboy Bebop",
                    animeSeason = AnimeSeason(
                        year = animeYear.toInt(),
                    )
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        animeSeason = AnimeSeason(
                            year = potentialGoldenRecordYear.toInt(),
                        )
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.anime).isEqualTo(anime)
                assertThat(result.potentialGoldenRecord).isEqualTo(potentialGoldenRecord)
                assertThat(result.matchProbability).isEqualTo(1.0)
            }

            @ParameterizedTest
            @CsvSource(
                "23,0",
                "0,23",
            )
            fun `ignore duration if one of one of the two is UNKNOWN`(animeMinutes: String, potentialGoldenRecordMinutes: String) {
                // given
                val anime = Anime(
                    _title = "Cowboy Bebop",
                    duration = Duration(
                        value = animeMinutes.toInt(),
                        unit = MINUTES,
                    )
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        duration = Duration(
                            value = potentialGoldenRecordMinutes.toInt(),
                            unit = MINUTES,
                        )
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.anime).isEqualTo(anime)
                assertThat(result.potentialGoldenRecord).isEqualTo(potentialGoldenRecord)
                assertThat(result.matchProbability).isEqualTo(1.0)
            }
        }

        @Nested
        inner class AllPropertiesTests {

            @Test
            fun `matches 100 percent with all properties, because the anime for is the same as in the potential golden record`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy()
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(1.0)
            }

            @Test
            fun `title differs - matches 96 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        _title = "- Cowboy Bebop -",
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.96)
            }

            @Test
            fun `type differs - matches 83 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        type = MOVIE,
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.83)
            }

            @ParameterizedTest
            @CsvSource(
                "SPECIAL,ONA",
                "ONA,SPECIAL",
            )
            fun `type within special and ona - matches 90 percent`(animeType: String, potentialGoldenRecordType: String) {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = Anime.Type.of(animeType),
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        type = Anime.Type.of(potentialGoldenRecordType),
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.9)
            }

            @Test
            fun `episodes differ - matches 99 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        episodes = 27,
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.99)
            }

            @Test
            fun `status differs - matches 83 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        status = UPCOMING,
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.83)
            }

            @Test
            fun `yearOfPremiere differs - matches 99 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        animeSeason = anime.animeSeason.copy(
                            year = 1999,
                        )
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.99)
            }

            @Test
            fun `duration in minutes differs - matches 99 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        duration = Duration(
                            value = 23,
                            unit = MINUTES,
                        )
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.99)
            }

            @Test
            fun `duration in seconds differs - matches 99 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 30,
                        unit = SECONDS,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        duration = Duration(
                            value = 31,
                            unit = SECONDS,
                        )
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.99)
            }

            @Test
            fun `duration in hours differs - matches 99 percent`() {
                // given
                val anime = Anime(
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = FINISHED,
                    duration = Duration(
                        value = 2,
                        unit = HOURS,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 1998,
                    ),
                    _synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = anime.copy(
                        duration = Duration(
                            value = 3,
                            unit = HOURS,
                        )
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.99)
            }

            @Test
            fun `completely different - matches 0 percent`() {
                // given
                val anime = Anime(
                    _title = "ABC",
                    type = MOVIE,
                    episodes = 1,
                    status = UPCOMING,
                    duration = Duration(
                        value = 2,
                        unit = HOURS,
                    ),
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2025,
                    ),
                )

                val potentialGoldenRecord = PotentialGoldenRecord(
                    id = randomUUID(),
                    anime = Anime(
                        _title = "DEF",
                        type = TV,
                        episodes = 1500,
                        status = FINISHED,
                        duration = Duration(
                            value = 30,
                            unit = SECONDS,
                        ),
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 1981,
                        ),
                    )
                )

                val matchingProbabilityCalculator = DefaultMatchingProbabilityCalculator()

                // when
                val result = matchingProbabilityCalculator.calculate(anime, potentialGoldenRecord)

                // then
                assertThat(result.matchProbability).isEqualTo(0.0)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultMatchingProbabilityCalculator.instance

                // when
                val result = DefaultMatchingProbabilityCalculator.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultMatchingProbabilityCalculator::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}