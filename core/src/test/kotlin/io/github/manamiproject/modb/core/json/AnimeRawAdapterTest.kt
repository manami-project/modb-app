package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.MetaDataProviderScoreValue
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeRawAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = AnimeRawAdapter()
            val expected = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                _relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
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
                _synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                _tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "myanimelist.net",
                    value = 7.77,
                    range = 1.0..10.0,
                )
            )

            // when
            val result = adapter.fromJson("""
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
                  "scores": [
                    {
                      "hostname": "myanimelist.net",
                      "value": 7.77,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 10.0
                      }
                    }
                  ],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent())

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = AnimeRawAdapter()

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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.title")
        }

        @Test
        fun `throws exception if title is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'title' is either missing or null.")
        }

        @Test
        fun `throws exception if sources is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.sources")
        }

        @Test
        fun `throws exception if sources is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'sources' is either missing or null.")
        }

        @Test
        fun `throws exception if synonyms is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.synonyms")
        }

        @Test
        fun `throws exception if synonyms is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'synonyms' is either missing or null.")
        }

        @Test
        fun `throws exception if type is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.type")
        }

        @Test
        fun `throws exception if type is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'type' is either missing or null.")
        }

        @Test
        fun `throws exception if episodes is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected an int but was NULL at path \$.episodes")
        }

        @Test
        fun `throws exception if episodes is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'episodes' is either missing or null.")
        }

        @Test
        fun `throws exception if status is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.status")
        }

        @Test
        fun `throws exception if status is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'status' is either missing or null.")
        }

        @Test
        fun `throws exception if animeSeason is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_OBJECT but was NULL at path \$.animeSeason")
        }

        @Test
        fun `throws exception if animeSeason is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'animeSeason' is either missing or null.")
        }

        @Test
        fun `throws exception if picture is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.picture")
        }

        @Test
        fun `throws exception if picture is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'picture' is either missing or null.")
        }

        @Test
        fun `throws exception if thumbnail is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.thumbnail")
        }

        @Test
        fun `throws exception if thumbnail is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'thumbnail' is either missing or null.")
        }

        @Test
        fun `returns anime with unknown duration if duration property is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson("""
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
                  "scores": [],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent())!!

            // then
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
        }

        @Test
        fun `returns anime with unknown duration if duration property is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson("""
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
                  "scores": [],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": [
                    "comedy",
                    "romance"
                  ]
                }
            """.trimIndent())!!

            // then
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
        }

        @Test
        fun `throws exception if relatedAnime is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": null,
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.relatedAnime")
        }

        @Test
        fun `throws exception if relatedAnime is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "tags": [
                        "comedy",
                        "romance"
                      ]
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'relatedAnime' is either missing or null.")
        }

        @Test
        fun `throws exception if tags is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": null
                    }
            """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.tags")
        }

        @Test
        fun `throws exception if tags is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
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
                      "scores": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ]
                    }
            """.trimIndent())
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
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                _relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
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
                _synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                _tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "myanimelist.net",
                    value = 7.77,
                    range = 1.0..10.0,
                ),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
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
                  "scores": [
                    {
                      "hostname": "myanimelist.net",
                      "value": 7.77,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 10.0
                      }
                    }
                  ],
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
            """.trimIndent())
        }

        @Test
        fun `correctly serialize nullable data if serializeNulls has been set`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ").serializeNulls()
            val obj = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                _relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
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
                _synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                _tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "myanimelist.net",
                    value = 7.77,
                    range = 1.0..10.0,
                ),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
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
                  "scores": [
                    {
                      "hostname": "myanimelist.net",
                      "value": 7.77,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 10.0
                      }
                    }
                  ],
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
            """.trimIndent())
        }

        @ParameterizedTest
        @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  "])
        fun `runs performChecks if activateChecks is false and fixes title`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = value,
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
                {
                  "sources": [],
                  "title": "Death Note",
                  "type": "UNKNOWN",
                  "episodes": 0,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "scores": [],
                  "synonyms": [],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())
        }

        @Test
        fun `runs performChecks if activateChecks is false and throws an exception if episodes is negative`() {
            // given
            val adapter = AnimeRawAdapter()
            val obj = AnimeRaw(
                _title = "Death Note",
                episodes = -1,
                activateChecks = false,
            )

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(obj)
            }

            // then
            assertThat(result).hasMessage("Episodes cannot have a negative value.")
        }

        @Test
        fun `runs performChecks if activateChecks is false and removes sources from relatedAnime`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2167"),
                    URI("https://myanimelist.net/anime/6351"),
                ),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
                {
                  "sources": [
                    "https://myanimelist.net/anime/6351"
                  ],
                  "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                  "type": "UNKNOWN",
                  "episodes": 0,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "scores": [],
                  "synonyms": [],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2167"
                  ],
                  "tags": []
                }
            """.trimIndent())
        }

        @ParameterizedTest
        @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  "])
        fun `runs performChecks if activateChecks is false and fixes synonyms`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "デスノート",
                _synonyms = hashSetOf(value),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
                {
                  "sources": [],
                  "title": "デスノート",
                  "type": "UNKNOWN",
                  "episodes": 0,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "scores": [],
                  "synonyms": [
                    "Death Note"
                  ],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())
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
        fun `runs performChecks if activateChecks is false and removes blank entries from synonyms`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "デスノート",
                _synonyms = hashSetOf(value),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
                {
                  "sources": [],
                  "title": "デスノート",
                  "type": "UNKNOWN",
                  "episodes": 0,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "scores": [],
                  "synonyms": [],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())
        }

        @ParameterizedTest
        @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  ", "DEATH NOTE"])
        fun `runs performChecks if activateChecks is false and fixes tags`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "デスノート",
                _tags = hashSetOf(value),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
                {
                  "sources": [],
                  "title": "デスノート",
                  "type": "UNKNOWN",
                  "episodes": 0,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "scores": [],
                  "synonyms": [],
                  "relatedAnime": [],
                  "tags": [
                    "death note"
                  ]
                }
            """.trimIndent())
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
        fun `runs performChecks if activateChecks is false and removes blank entries from tags`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "デスノート",
                _tags = hashSetOf(value),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
                {
                  "sources": [],
                  "title": "デスノート",
                  "type": "UNKNOWN",
                  "episodes": 0,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "scores": [],
                  "synonyms": [],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())
        }

        @Test
        fun `sources synonyms relatedAnime and tags are being sorted asc by their string representation`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(
                    URI("https://livechart.me/anime/3681"),
                    URI("https://anisearch.com/anime/6826"),
                    URI("https://kitsu.io/anime/4529"),
                    URI("https://anime-planet.com/anime/clannad-another-world-kyou-chapter"),
                    URI("https://anilist.co/anime/6351"),
                    URI("https://notify.moe/anime/3L63cKimg"),
                    URI("https://myanimelist.net/anime/6351"),
                ),
                _relatedAnime = hashSetOf(
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
                _synonyms = hashSetOf(
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
                _tags = hashSetOf(
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
            assertThat(result).isEqualTo("""
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
                  "scores": [],
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
            """.trimIndent())
        }

        @Test
        fun `sort scores by hostname`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                _relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
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
                _synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                _tags = hashSetOf(
                    "comedy",
                    "romance",
                )
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "myanimelist.net",
                    value = 7.77,
                    range = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "anilist.co",
                    value = 84.0,
                    range = 1.0..100.0,
                ),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""
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
                  "scores": [
                    {
                      "hostname": "anilist.co",
                      "value": 84.0,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 100.0
                      }
                    },
                    {
                      "hostname": "myanimelist.net",
                      "value": 7.77,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 10.0
                      }
                    }
                  ],
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
            """.trimIndent())
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = AnimeRawAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("AnimeRawAdapter expects non-nullable value, but received null.")
        }
    }
}