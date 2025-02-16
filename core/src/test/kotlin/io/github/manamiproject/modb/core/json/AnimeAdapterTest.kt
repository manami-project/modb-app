package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI

internal class AnimeAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = AnimeAdapter()
            val expected = Anime(
                title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                type = TV,
                episodes = 24,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2009
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = Duration(24, MINUTES),
                score = ScoreValue(
                    arithmeticGeometricMean = 1.29,
                    arithmeticMean = 2.38,
                    median = 3.47,
                ),
                synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            )

            // when
            val result = adapter.fromJson(
                """
                {
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": {
                    "value": 1440,
                    "unit": "SECONDS"
                  },
                  "score": {
                    "arithmeticGeometricMean": 1.29,
                    "arithmeticMean": 2.38,
                    "median": 3.47
                  },
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""null""")
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_OBJECT but was NULL at path \$")
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `throws exception if title is blank`(value: String) {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.fromJson(
                    """
                    {
                      "title": "$value",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Title cannot be blank.")
        }

        @Test
        fun `throws exception if title is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": null,
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.title")
        }

        @Test
        fun `throws exception if title is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'title' is either missing or null.")
        }

        @Test
        fun `throws exception if sources is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": null,
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.sources")
        }

        @Test
        fun `throws exception if sources is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'sources' is either missing or null.")
        }

        @Test
        fun `throws exception if synonyms is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": null,
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.synonyms")
        }

        @Test
        fun `throws exception if synonyms is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'synonyms' is either missing or null.")
        }

        @Test
        fun `throws exception if type is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": null,
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.type")
        }

        @Test
        fun `throws exception if type is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'type' is either missing or null.")
        }

        @Test
        fun `throws exception if episodes is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": null,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected an int but was NULL at path \$.episodes")
        }

        @Test
        fun `throws exception if episodes is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'episodes' is either missing or null.")
        }

        @Test
        fun `throws exception if status is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": null,
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.status")
        }

        @Test
        fun `throws exception if status is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'status' is either missing or null.")
        }

        @Test
        fun `throws exception if animeSeason is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": null,
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_OBJECT but was NULL at path \$.animeSeason")
        }

        @Test
        fun `throws exception if animeSeason is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'animeSeason' is either missing or null.")
        }

        @Test
        fun `throws exception if picture is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": null,
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.picture")
        }

        @Test
        fun `throws exception if picture is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'picture' is either missing or null.")
        }

        @Test
        fun `throws exception if thumbnail is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": null,
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.thumbnail")
        }

        @Test
        fun `throws exception if thumbnail is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'thumbnail' is either missing or null.")
        }

        @Test
        fun `returns anime with unknown duration if duration property is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(
                """
                {
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": null,
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )!!

            // then
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
        }

        @Test
        fun `returns anime with unknown duration if duration property is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(
                """
                {
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )!!

            // then
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
        }

        @Test
        fun `returns anime with NoScore if score property is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(
                """
                {
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": {
                    "value": 1440,
                    "unit": "SECONDS"
                  },
                  "score": null,
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )!!

            // then
            assertThat(result.score).isEqualTo(NoScore)
        }

        @Test
        fun `returns anime with NoScore if score property is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(
                """
                {
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": {
                    "value": 1440,
                    "unit": "SECONDS"
                  },
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )!!

            // then
            assertThat(result.score).isEqualTo(NoScore)
        }

        @Test
        fun `throws exception if relatedAnime is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": null,
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.relatedAnime")
        }

        @Test
        fun `throws exception if relatedAnime is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'relatedAnime' is either missing or null.")
        }

        @Test
        fun `throws exception if tags is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": null
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.tags")
        }

        @Test
        fun `throws exception if tags is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson(
                    """
                    {
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "synonyms": [
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編"
                      ],
                      "type": "TV",
                      "episodes": 24,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "SUMMER",
                        "year": 2009
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                      "duration": {
                        "value": 1440,
                        "unit": "SECONDS"
                      },
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ]
                    }
            """.trimIndent()
                )
            }

            // then
            assertThat(result).hasMessage("Property 'tags' is either missing or null.")
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = AnimeAdapter().indent("  ")
            val obj = Anime(
                title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                type = TV,
                episodes = 24,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2009
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = Duration(24, MINUTES),
                score = ScoreValue(
                    arithmeticGeometricMean = 1.29,
                    arithmeticMean = 2.38,
                    median = 3.47,
                ),
                synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(
                """
                {
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": {
                    "value": 1440,
                    "unit": "SECONDS"
                  },
                  "score": {
                    "arithmeticGeometricMean": 1.29,
                    "arithmeticMean": 2.38,
                    "median": 3.47
                  },
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )
        }

        @Test
        fun `correctly serialize nullable data if serializeNulls has been set`() {
            // given
            val adapter = AnimeAdapter().indent("  ").serializeNulls()
            val obj = Anime(
                title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                type = TV,
                episodes = 24,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 0
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = UNKNOWN_DURATION,
                score = NoScore,
                synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(
                """
                {
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": null
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": null,
                  "score": null,
                  "synonyms": [
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編"
                  ],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent()
            )
        }

        @Test
        fun `sources synonyms relatedAnime and tags are being sorted asc by their string representation`() {
            // given
            val adapter = AnimeAdapter().indent("  ")
            val obj = Anime(
                title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                sources = hashSetOf(
                    URI("https://livechart.me/anime/3681"),
                    URI("https://anisearch.com/anime/6826"),
                    URI("https://kitsu.io/anime/4529"),
                    URI("https://anime-planet.com/anime/clannad-another-world-kyou-chapter"),
                    URI("https://anilist.co/anime/6351"),
                    URI("https://notify.moe/anime/3L63cKimg"),
                    URI("https://myanimelist.net/anime/6351"),
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/4181"),
                    URI("https://anilist.co/anime/2167"),
                    URI("https://anime-planet.com/anime/clannad"),
                    URI("https://livechart.me/anime/10537"),
                    URI("https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter"),
                    URI("https://livechart.me/anime/10976"),
                    URI("https://anime-planet.com/anime/clannad-movie"),
                    URI("https://anisearch.com/anime/4199"),
                    URI("https://notify.moe/anime/F2eY5Fmig"),
                    URI("https://livechart.me/anime/3581"),
                    URI("https://anime-planet.com/anime/clannad-after-story"),
                    URI("https://livechart.me/anime/3588"),
                    URI("https://myanimelist.net/anime/2167"),
                    URI("https://livechart.me/anime/3657"),
                    URI("https://anilist.co/anime/4059"),
                    URI("https://livechart.me/anime/3822"),
                    URI("https://kitsu.io/anime/1962"),
                ),
                type = TV,
                episodes = 24,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2009
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = Duration(24, MINUTES),
                synonyms = hashSetOf(
                    "Clannad (TV)",
                    "Kuranado",
                    "Clannad TV",
                    "CLANNAD",
                    "クラナド",
                    "Кланнад",
                    "Кланад",
                    "كلاناد",
                    "Clannad 1",
                    "클라나드",
                    "خانواده",
                    "کلاناد",
                    "แคลนนาด",
                    "くらなど",
                    "ＣＬＡＮＮＡＤ -クラナド-",
                ),
                tags = hashSetOf(
                    "baseball",
                    "based on a visual novel",
                    "basketball",
                    "amnesia",
                    "coming of age",
                    "asia",
                    "daily life",
                    "comedy",
                    "delinquents",
                    "earth",
                    "romance",
                    "ensemble cast",
                    "drama",
                )
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(
                """
                {
                  "sources": [
                    "https://anilist.co/anime/6351",
                    "https://anime-planet.com/anime/clannad-another-world-kyou-chapter",
                    "https://anisearch.com/anime/6826",
                    "https://kitsu.io/anime/4529",
                    "https://livechart.me/anime/3681",
                    "https://myanimelist.net/anime/6351",
                    "https://notify.moe/anime/3L63cKimg"
                  ],
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "type": "TV",
                  "episodes": 24,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "SUMMER",
                    "year": 2009
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                  "duration": {
                    "value": 1440,
                    "unit": "SECONDS"
                  },
                  "synonyms": [
                    "CLANNAD",
                    "Clannad (TV)",
                    "Clannad 1",
                    "Clannad TV",
                    "Kuranado",
                    "Кланад",
                    "Кланнад",
                    "خانواده",
                    "كلاناد",
                    "کلاناد",
                    "แคลนนาด",
                    "くらなど",
                    "クラナド",
                    "클라나드",
                    "ＣＬＡＮＮＡＤ -クラナド-"
                  ],
                  "relatedAnime": [
                    "https://anilist.co/anime/2167",
                    "https://anilist.co/anime/4059",
                    "https://anime-planet.com/anime/clannad",
                    "https://anime-planet.com/anime/clannad-after-story",
                    "https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter",
                    "https://anime-planet.com/anime/clannad-movie",
                    "https://anisearch.com/anime/4199",
                    "https://kitsu.io/anime/1962",
                    "https://livechart.me/anime/10537",
                    "https://livechart.me/anime/10976",
                    "https://livechart.me/anime/3581",
                    "https://livechart.me/anime/3588",
                    "https://livechart.me/anime/3657",
                    "https://livechart.me/anime/3822",
                    "https://myanimelist.net/anime/2167",
                    "https://myanimelist.net/anime/4181",
                    "https://notify.moe/anime/F2eY5Fmig"
                  ],
                  "tags": [
                    "amnesia",
                    "asia",
                    "baseball",
                    "based on a visual novel",
                    "basketball",
                    "comedy",
                    "coming of age",
                    "daily life",
                    "delinquents",
                    "drama",
                    "earth",
                    "ensemble cast",
                    "romance"
                  ]
                }
            """.trimIndent()
            )
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = AnimeAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("AnimeAdapter expects non-nullable value, but received null.")
        }
    }
}