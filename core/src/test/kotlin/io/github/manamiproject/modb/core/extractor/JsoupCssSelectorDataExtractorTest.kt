package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.models.Tag
import io.github.manamiproject.modb.core.models.Title
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class JsoupCssSelectorDataExtractorTest {

    @Test
    fun `return NotFound if selector was not found`() {
        runBlocking {
            // when
            val result = JsoupCssSelectorDataExtractor.extract(
                rawContent = loadTestResource("DataExtractorTest/myanimelist/most_cases.html"),
                selection = mapOf(
                    "result" to "//unknow",
                )
            )

            // then
            assertThat(result.notFound("result")).isTrue()
        }
    }


    @Nested
    inner class RealXpathExpressionsTests {

        @Nested
        inner class AnidbExpressionsTests {

            @Test
            fun `correctly parse most cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/anidb/most_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "title" to "//h1[contains(@class, 'anime')]/text()",
                        "episodesString" to "//span[contains(@itemprop, 'numberOfEpisodes')]/text()",
                        "episodesTypeCell" to "//tr[contains(@class, 'type')]/td[contains(@class, 'value')]/text()",
                        "tags" to "//span[contains(@itemprop, 'genre')]/text()",
                        "image" to "//img[contains(@itemprop, 'image')]/@src",
                        "source" to "//input[contains(@type, 'hidden')][contains(@name, 'aid')]/@value",
                        "type" to "//tr[contains(@class, 'type')]//th[contains(text(), 'Type')]/following-sibling::*/text()",
                        "duration" to "//table[contains(@id, 'eplist')]/tbody/tr//td[contains(@class, 'duration')]/text()",
                        "season" to "//tr[contains(@class, 'season')]//td[contains(@class, 'value')]/text()",
                        "startDate" to "//tr[contains(@class, 'year')]//td[contains(@class, 'value')]//span[contains(@itemprop, 'startDate')]/@content",
                        "datePublished" to "//tr[contains(@class, 'year')]//td[contains(@class, 'value')]//span[contains(@itemprop, 'datePublished')]/@content",
                        "relatedAnime" to "//div[contains(@class, 'directly_related')]//a/@href",
                        "alternateNames" to "//label[contains(@itemprop, 'alternateName')]/text()",
                        "synonymsList" to "//div[contains(@class, 'titles')]//tr[contains(@class, 'syn')]/td/text()",
                        "shortNames" to "//div[contains(@class, 'titles')]//tr[contains(@class, 'short')]/td/text()",
                        "startDateAttr" to "//span[contains(@itemprop, 'startDate')]/@content",
                        "endDateAttr" to "//span[contains(@itemprop, 'endDate')]/@content",
                        "isTimePeriod" to "//tr[contains(@class, 'year')]/td[contains(@class, 'value')]/text()",
                        "datePublishedAttr" to "//span[contains(@itemprop, 'datePublished')]/@content",
                    ))

                    // then
                    assertThat(result.string("title")).isEqualTo("Anime: Death Note")
                    assertThat(result.string("episodesString")).isEqualTo("37")
                    assertThat(result.listNotNull<String>("episodesTypeCell").first()).isEqualTo("TV Series, 37 episodes")
                    assertThat(result.listNotNull<Tag>("tags")).containsExactlyInAnyOrder(
                        "contemporary fantasy",
                        "detective",
                        "manga",
                        "shounen",
                        "thriller",
                    )
                    assertThat(result.string("image")).isEqualTo("https://cdn.anidb.net/images/main/221544.jpg")
                    assertThat(result.listNotNull<String>("source").first()).isEqualTo("4563")
                    assertThat(result.listNotNull<String>("type").first()).isEqualTo("TV Series, 37 episodes")
                    assertThat(result.listNotNull<String>("duration").first()).isEqualTo("25m")
                    assertThat(result.string("season")).isEqualTo("Autumn 2006")
                    assertThat(result.string("startDate")).isEqualTo("2006-10-04")
                    assertThat(result.listNotNull<String>("relatedAnime")).containsExactlyInAnyOrder(
                        "/anime/4563/relation/graph",
                        "/anime/4563/relation",
                        "/anime/8146",
                        "/anime/8146",
                        "/anime/8147",
                        "/anime/8147",
                    )
                    assertThat(result.listNotNull<String>("alternateNames")).containsExactlyInAnyOrder(
                        "DEATH NOTE",
                        "Death Note",
                        "DEATH NOTE",
                        "Death Note",
                        "Death Note",
                        "Death Note - Carnetul morţii",
                        "Death Note - Zápisník smrti",
                        "데스노트",
                        "Bilježnica smrti",
                        "Death Note",
                        "Death Note - A halállista",
                        "Death Note - Zápisník smrti",
                        "Notatnik śmierci",
                        "Τετράδιο Θανάτου",
                        "Бележник на Смъртта",
                        "Тетрадь cмерти",
                        "Үхлийн Тэмдэглэл",
                        "دفترچه یادداشت مرگ",
                        "كـتـاب الـموت",
                        "डेथ नोट",
                        "死亡笔记",
                    )
                    assertThat(result.string("synonymsList")).isEqualTo("Caderno da Morte, Mirties Užrašai, Notes Śmierci, Quaderno della Morte, Sveska Smrti, Ölüm Defteri, Записник Смерті, Свеска Смрти, Тетрадка на Смъртта, מחברת המוות, دفتر الموت, دفترچه مرگ, ديث نوت, مدونة الموت, مذكرة المـوت, مذكرة الموت, موت نوٹ, デスノート")
                    assertThat(result.string("shortNames")).isEqualTo("DN")
                    assertThat(result.string("startDateAttr")).isEqualTo("2006-10-04")
                    assertThat(result.string("endDateAttr")).isEqualTo("2007-06-27")
                    assertThat(result.string("isTimePeriod")).isEqualTo("04.10.2006 until 27.06.2007")
                }
            }

            @Test
            fun `date published cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/anidb/date_published_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "datePublished" to "//tr[contains(@class, 'year')]//td[contains(@class, 'value')]//span[contains(@itemprop, 'datePublished')]/@content",
                        "datePublishedAttr" to "//span[contains(@itemprop, 'datePublished')]/@content",
                    ))

                    // then
                    assertThat(result.string("datePublished")).isEqualTo("2014-08-23")
                    assertThat(result.string("datePublishedAttr")).isEqualTo("2014-08-23")
                }
            }
        }

        @Nested
        inner class AnimePlanetExpressionsTests {

            @Test
            fun `correctly parse most cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/anime-planet/most_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "titleH1" to "//h1[@itemprop='name']/text()",
                        "jsonld" to "//script[@type='application/ld+json']/node()",
                        "titleMeta" to "//meta[@property='og:title']/@content",
                        "thumbnail" to "//img[@itemprop='image']/@src",
                        "typeEpisodesDuration" to "//span[@class='type']/text()",
                        "iconYear" to "//section[@class='pure-g entryBar']//span[@class='iconYear']/text()",
                        "seasonYear" to "//section[@class='pure-g entryBar']//span[@class='iconYear']/following-sibling::*/text()",
                        "source" to "//link[@rel='canonical']/@href",
                        "alternativeTitle" to "//h2[@class='aka']/text()",
                        "relatedAnime" to "//div[@id='tabs--relations--anime']/div//a/@href",
                        "tags" to "//div[contains(@class, 'tags')]//a/text()",
                    ))

                    // then
                    assertThat(result.string("titleH1")).isEqualTo("The Promised Neverland")
                    assertThat(result.string("jsonld")).isEqualTo("""{"@context":"http:\/\/schema.org","@type":"TVSeries","name":"The Promised Neverland","description":"Emma, Norman, Ray, and many other &quot;siblings&quot; live at an isolated orphanage cut off from the outside world. They live a charmed life surrounded by friends and watched over by Mama, their caretaker, until they leave the house for a foster family by the age of twelve. The only rule is that they must never venture outside of the gate that isolates them from the rest of society, for dangers await. But one day, the children learn the true horror of what's in store for their short lives...&nbsp;","url":"https:\/\/www.anime-planet.com\/anime\/the-promised-neverland","image":"https:\/\/www.anime-planet.comhttps:\/\/cdn.anime-planet.com\/anime\/primary\/the-promised-neverland-1-190x266.jpg?t=1625898174","startDate":"2019-01-10","endDate":"2019-03-29","numberOfEpisodes":12,"genre":["Based on a Manga","Child Protagonists","Dark Fantasy","Domestic Abuse","Fantasy","Horror","Isolated Society","Mature Themes","Mind Games","Mystery","noitaminA","Orphanage","Orphans","Outside World","Psychological","Sci Fi","Self-Harm","Shounen","Thriller"],"actor":[{"@type":"Person","name":"Ai KAYANO","url":"https:\/\/www.anime-planet.com\/people\/ai-kayano"},{"@type":"Person","name":"Amber CONNOR","url":"https:\/\/www.anime-planet.com\/people\/amber-connor"},{"@type":"Person","name":"Ari OZAWA","url":"https:\/\/www.anime-planet.com\/people\/ari-ozawa"},{"@type":"Person","name":"Brianna KNICKERBOCKER","url":"https:\/\/www.anime-planet.com\/people\/brianna-knickerbocker"},{"@type":"Person","name":"Cedric WILLIAMS","url":"https:\/\/www.anime-planet.com\/people\/cedric-williams"},{"@type":"Person","name":"Coco HAYASHI","url":"https:\/\/www.anime-planet.com\/people\/coco-hayashi"},{"@type":"Person","name":"Cristina VALENZUELA","url":"https:\/\/www.anime-planet.com\/people\/cristina-valenzuela"},{"@type":"Person","name":"Erica MENDEZ","url":"https:\/\/www.anime-planet.com\/people\/erica-mendez"},{"@type":"Person","name":"Erika HARLACHER","url":"https:\/\/www.anime-planet.com\/people\/erika-harlacher"},{"@type":"Person","name":"Erisa KUON","url":"https:\/\/www.anime-planet.com\/people\/erisa-kuon"},{"@type":"Person","name":"Hiyori KONO","url":"https:\/\/www.anime-planet.com\/people\/hiyori-kono"},{"@type":"Person","name":"Jackie LASTRA","url":"https:\/\/www.anime-planet.com\/people\/jackie-lastra"},{"@type":"Person","name":"Jeannie TIRADO","url":"https:\/\/www.anime-planet.com\/people\/jeannie-tirado"},{"@type":"Person","name":"Laura POST","url":"https:\/\/www.anime-planet.com\/people\/laura-post"},{"@type":"Person","name":"Laura STAHL","url":"https:\/\/www.anime-planet.com\/people\/laura-stahl"},{"@type":"Person","name":"Lynn","url":"https:\/\/www.anime-planet.com\/people\/lynn"},{"@type":"Person","name":"Maaya UCHIDA","url":"https:\/\/www.anime-planet.com\/people\/maaya-uchida"},{"@type":"Person","name":"Mami KOYAMA","url":"https:\/\/www.anime-planet.com\/people\/mami-koyama"},{"@type":"Person","name":"Mari HINO","url":"https:\/\/www.anime-planet.com\/people\/mari-hino"},{"@type":"Person","name":"Mariya ISE","url":"https:\/\/www.anime-planet.com\/people\/mariya-ise"},{"@type":"Person","name":"Michelle RUFF","url":"https:\/\/www.anime-planet.com\/people\/michelle-ruff"},{"@type":"Person","name":"Morgan BERRY","url":"https:\/\/www.anime-planet.com\/people\/morgan-berry"},{"@type":"Person","name":"Nao FUJITA","url":"https:\/\/www.anime-planet.com\/people\/nao-fujita"},{"@type":"Person","name":"Nao SHIRAKI","url":"https:\/\/www.anime-planet.com\/people\/nao-shiraki"},{"@type":"Person","name":"Philece SAMPLER","url":"https:\/\/www.anime-planet.com\/people\/philece-sampler"},{"@type":"Person","name":"Rebeka THOMAS","url":"https:\/\/www.anime-planet.com\/people\/rebeka-thomas"},{"@type":"Person","name":"Ryan BARTLEY","url":"https:\/\/www.anime-planet.com\/people\/ryan-bartley"},{"@type":"Person","name":"Sanae KOBAYASHI","url":"https:\/\/www.anime-planet.com\/people\/sanae-kobayashi"},{"@type":"Person","name":"Shinei UEKI","url":"https:\/\/www.anime-planet.com\/people\/shinei-ueki"},{"@type":"Person","name":"Shizuka ISHIGAMI","url":"https:\/\/www.anime-planet.com\/people\/shizuka-ishigami"},{"@type":"Person","name":"Sumire MOROHOSHI","url":"https:\/\/www.anime-planet.com\/people\/sumire-morohoshi"},{"@type":"Person","name":"Yoshino AOYAMA","url":"https:\/\/www.anime-planet.com\/people\/yoshino-aoyama"},{"@type":"Person","name":"Yuko KAIDA","url":"https:\/\/www.anime-planet.com\/people\/yuko-kaida"},{"@type":"Person","name":"Yuuko MORI","url":"https:\/\/www.anime-planet.com\/people\/yuuko-mori"}],"director":[{"@type":"Person","name":"Mamoru KANBE","url":"https:\/\/www.anime-planet.com\/people\/mamoru-kanbe"}],"musicBy":[{"@type":"Person","name":"Takahiro OBATA","url":"https:\/\/www.anime-planet.com\/people\/takahiro-obata"}],"character":[{"@type":"Person","name":"Don","url":"https:\/\/www.anime-planet.com\/characters\/don-the-promised-neverland"},{"@type":"Person","name":"Emma","url":"https:\/\/www.anime-planet.com\/characters\/emma-the-promised-neverland"},{"@type":"Person","name":"Gilda","url":"https:\/\/www.anime-planet.com\/characters\/gilda-the-promised-neverland"},{"@type":"Person","name":"Isabella","url":"https:\/\/www.anime-planet.com\/characters\/isabella-the-promised-neverland"},{"@type":"Person","name":"Krone","url":"https:\/\/www.anime-planet.com\/characters\/krone-the-promised-neverland"},{"@type":"Person","name":"Norman","url":"https:\/\/www.anime-planet.com\/characters\/norman-the-promised-neverland"},{"@type":"Person","name":"Ray","url":"https:\/\/www.anime-planet.com\/characters\/ray-the-promised-neverland"}],"aggregateRating":{"@type":"AggregateRating","ratingCount":43889,"bestRating":5,"worstRating":0.5,"ratingValue":4.422,"reviewCount":168}}""")
                    assertThat(result.string("titleMeta")).isEqualTo("The Promised Neverland")
                    assertThat(result.string("thumbnail")).isEqualTo("https://cdn.anime-planet.com/anime/primary/the-promised-neverland-1-190x266.jpg?t=1625898174")
                    assertThat(result.string("typeEpisodesDuration")).isEqualTo("TV (12 eps)")
                    assertThat(result.string("iconYear")).isEqualTo("2019")
                    assertThat(result.string("seasonYear")).isEqualTo("Winter 2019")
                    assertThat(result.string("source")).isEqualTo("https://www.anime-planet.com/anime/the-promised-neverland")
                    assertThat(result.string("alternativeTitle")).isEqualTo("Alt title: Yakusoku no Neverland")
                    assertThat(result.listNotNull<String>("relatedAnime")).containsExactlyInAnyOrder(
                        "/anime/the-promised-neverland-2nd-season",
                        "/anime/the-promised-neverland-2nd-season-tokubetsu-hen-michishirube",
                    )
                    assertThat(result.listNotNull<Tag>("tags")).containsExactlyInAnyOrder(
                        "Fantasy",
                        "Horror",
                        "Mystery",
                        "Sci Fi",
                        "Shounen",
                        "Child Protagonists",
                        "Dark Fantasy",
                        "Isolated Society",
                        "Mind Games",
                        "noitaminA",
                        "Orphanage",
                        "Orphans",
                        "Outside World",
                        "Psychological",
                        "Thriller",
                        "Based on a Manga",
                        "Mature Themes,",
                        "Domestic Abuse,",
                        "Self-Harm",
                    )
                }
            }
        }

        @Nested
        inner class AnisearchExpressionsTests {

            @Test
            fun `correctly parse most cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/anisearch/most_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "jsonld" to "//script[@type='application/ld+json']/node()",
                        "image" to "//meta[@property='og:image']/@content",
                        "title" to "//meta[@property='og:title']/@content",
                        "type" to "//ul[@class='xlist row simple infoblock']//div[@class='type']",
                        "status" to "//div[@class='status']",
                        "duration" to "//ul[@class='xlist row simple infoblock']//time",
                        "tags" to "//section[@id='description']//ul[@class='cloud']//li//a/text()",
                        "source" to "//div[@id='content-outer']/@data-id",
                        "synonymsByLanguage" to "//div[@class='title']//strong/text()",
                        "synonymsBySubheader" to "//div[@class='title']//div[@class='grey']/text()",
                        "synonymsDivNoSpan" to "//div[@class='synonyms']",
                        "synonymsDivSpan" to "//div[@class='synonyms']//span[@id='text-synonyms']",
                        "synonymsItalic" to "//div[@class='synonyms']//i/text()",
                    ))

                    // then
                    assertThat(result.listNotNull<String>("jsonld").first()).isEqualTo("""{"@context":"https://schema.org","@type":"TVSeries","@id":"https://www.anisearch.com/anime/3633","name":"Death Note","url":"https://www.anisearch.com/anime/3633,death-note","image":"https://www.anisearch.com/images/anime/cover/3/3633_300.webp","description":"Information about the anime Death Note from studio MADHOUSE Inc. with the main genre Crime Fiction","numberOfEpisodes":"37","aggregateRating":{"@type":"AggregateRating","ratingValue":"4.5","ratingCount":"13312","worstRating":"0.1","bestRating":"5"},"startDate":"2006-10-04","endDate":"2007-06-27","genre":["Crime Fiction","Drama","Fantasy","Horror","Psychological Drama","Supernatural Drama","Thriller"]}""")
                    assertThat(result.string("image")).isEqualTo("https://www.anisearch.com/images/anime/cover/full/3/3633.webp")
                    assertThat(result.string("title")).isEqualTo("Death Note (Anime)")
                    assertThat(result.string("type")).isEqualTo("TV-Series, 37")
                    assertThat(result.listNotNull<String>("status").first()).isEqualTo("Completed")
                    assertThat(result.string("duration")).isEqualTo("23 min")
                    assertThat(result.listNotNull<Tag>("tags")).containsExactlyInAnyOrder(
                        "Crime Fiction",
                        "Thriller",
                        "Crime Fiction",
                        "Drama",
                        "Fantasy",
                        "Horror",
                        "Psychological Drama",
                        "Supernatural Drama",
                        "Thriller",
                        "Contemporary Fantasy",
                        "Detective",
                        "Genius",
                        "Hero of Strong Character",
                        "Kamis",
                        "Policeman",
                        "Present",
                        "Twisted Story",
                        "University",
                    )
                    assertThat(result.string("source")).isEqualTo("3633")
                    assertThat(result.listNotNull<Title>("synonymsByLanguage").first()).isEqualTo("Death Note")
                    assertThat(result.string("synonymsBySubheader")).isEqualTo("デスノート")
                    assertThat(result.string("synonymsDivNoSpan")).isEqualTo("DN")
                }
            }

            @Test
            fun `synonyms cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/anisearch/synonyms_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "synonymsDivSpan" to "//div[@class='synonyms']//span[@id='text-synonyms']",
                        "synonymsItalic" to "//div[@class='synonyms']//i/text()",
                    ))

                    // then
                    assertThat(result.listNotNull<Title>("synonymsDivSpan")).containsExactlyInAnyOrder(
                        ",",
                        ", Inu × Boku Secret Service: Miketsukami-kun‘s Transformations / Switch / Playing House,",
                        ", Inu x Boku SS: Miketsukami-kun Henka / Switch / Omamagoto",
                    )
                    assertThat(result.listNotNull<Title>("synonymsItalic")).containsExactlyInAnyOrder(
                        "Inu x Boku SS Special",
                        "Youko x Boku SS Special",
                        "Inu x Boku Secret Service: Miketsukami‘s Metamorphosis / Switch / Playing House",
                        "Inu x Boku Secret Service: Miketsukami-kun‘s Transformations / Switch / Playing House",
                    )
                }
            }
        }

        @Nested
        inner class LivechartExpressionsTests {

            @Test
            fun `correctly parse most cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/livechart/most_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "jsonld" to "//script[@type='application/ld+json']/node()",
                        "title" to "//meta[@property='og:title']/@content",
                        "image" to "//meta[@property='og:image']/@content",
                        "episodesDiv" to "//div[contains(text(), 'Episodes')]/../text()",
                        "episodesCountdown" to "//div[@data-controller='countdown-bar']//div[contains(text(), 'EP')]/text()",
                        "type" to "//div[contains(text(), 'Format')]/..",
                        "status" to "//div[contains(text(), 'Status')]/..",
                        "duration" to "//div[contains(text(), 'Run time')]/..",
                        "season" to "//div[contains(text(), 'Season')]/../a/text()",
                        "year" to "//div[contains(text(), 'Premiere')]/following-sibling::*",
                        "relatedAnime" to "//div[@data-controller='carousel']//article/a/@href",
                        "tags" to "//div[contains(text(), 'Tags')]/..//a[@data-anime-details-target='tagChip']",
                        "sourceDiv" to "//div[@data-anime-details-id]/@data-anime-details-id",
                        "sourceMeta" to "//meta[@property='og:url']/@content",
                    ))

                    // then
                    assertThat(result.listNotNull<String>("jsonld").first()).isEqualTo("""{"@context":"http://schema.org","@type":"TVSeries","url":"https://www.livechart.me/anime/3437","genre":["Detective","Mystery","Psychological","Supernatural","Suspense"],"name":"Death Note","image":"https://u.livechart.me/anime/3437/poster_image/ea9acd1ccea844fd9c4debde5e8e631e.png/large.jpg","description":"Bored with his deteriorating world and the laconic way of his fellows, shinigami Ryuuk drops his Death Note on Earth and watches to see if it stirs up anything interesting. His plan succeeds beyond his wildest expectations when the Death Note is found by brilliant high school senior Light Yagami, who is also bored with a world he considers rotten. Although initially he regards the book as a prank, Light soon discovers, through experimentation, that the book&#39;s claim is true: picture a person in your mind as you write the person&#39;s name in the Death Note, and that person dies 40 seconds later of a heart attack (although a different time frame and manner of death can be specified). Armed with that power, Light sets out on a quest he sees as noble: make the world a better place by eliminating all its criminals using the Death Note. Soon cast as the mysterious &quot;Kira&quot; (a Japanese pronunciation of the English &quot;killer&quot;) in the media and on the Internet, some take exception to his playing god, most notably the police and the enigmatic master detective L, who resolves to do everything in his power to stop Kira. Light counters by doing everything in his power to prevent people from identifying or interfering with him, even if that means getting rid of people investigating him.","numberOfEpisodes":37,"datePublished":"2006-10-03","alternateName":["デスノート"],"productionCompany":[{"@type":"Organization","@id":"https://www.livechart.me/studios/65","url":"https://www.livechart.me/studios/65","name":"MADHOUSE"}],"aggregateRating":{"@type":"AggregateRating","ratingCount":6900,"bestRating":10,"worstRating":1,"ratingValue":"8.66"}}""")
                    assertThat(result.string("title")).isEqualTo("Death Note")
                    assertThat(result.string("image")).isEqualTo("https://u.livechart.me/anime/3437/poster_image/ea9acd1ccea844fd9c4debde5e8e631e.png/large.jpg")
                    assertThat(result.listNotNull<String>("episodesDiv").first()).isEqualTo("Episodes  0 / 37")
                    assertThat(result.string("type")).isEqualTo("TV")
                    assertThat(result.string("status")).isEqualTo("Finished")
                    assertThat(result.string("duration")).isEqualTo("23m")
                    assertThat(result.listNotNull<String>("season").first()).isEqualTo("Season Fall 2006")
                    assertThat(result.string("year")).isEqualTo("October 4, 2006")
                    assertThat(result.string("relatedAnime")).isEqualTo("/anime/3808")
                    assertThat(result.listNotNull<Tag>("tags")).containsExactlyInAnyOrder(
                        "Detective",
                        "Mystery",
                        "Psychological",
                        "Supernatural",
                        "Suspense",
                        "Shounen",
                    )
                    assertThat(result.string("sourceDiv")).isEqualTo("3437")
                    assertThat(result.string("sourceMeta")).isEqualTo("https://www.livechart.me/anime/3437")
                }
            }

            @Test
            fun `episodes countdown case`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/livechart/episodes_countdown_case.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                            "episodesCountdown" to "//div[@data-controller='countdown-bar']//div[contains(text(), 'EP')]/text()",
                        )
                    )

                    // then
                    assertThat(result.string("episodesCountdown")).isEqualTo("EP6")

                }
            }
        }

        @Nested
        inner class MyanimelistExpressionsTests {

            @Test
            fun `correctly parse most cases`() {
                runBlocking {
                    // given
                    val html = loadTestResource<String>("DataExtractorTest/myanimelist/most_cases.html")

                    // when
                    val result = JsoupCssSelectorDataExtractor.extract(html, mapOf(
                        "title" to "//meta[@property='og:title']/@content",
                        "episodes" to "//td[contains(text(), 'Episodes')]/following-sibling::td/a/text()",
                        "source" to "//meta[@property='og:url']/@content",
                        "status" to "//td[contains(text(), 'Status')]/following-sibling::*/text()",
                        "tags" to "//span[@itemprop='genre']/text()",
                        "type" to "//td[contains(text(), 'Type')]/following-sibling::*/text()",
                        "duration" to "//td[contains(text(), 'Duration')]/following-sibling::*/text()",
                        "premiered" to "//td[contains(text(), 'Premiered')]/following-sibling::*/text()",
                        "aired" to "//td[contains(text(), 'Aired')]/following-sibling::*/text()",
                        "picture" to "//div[contains(@class, 'status-block')]/div[@itemprop='image']/@content",
                        "relatedAnime" to "//div[@id='related-manga']/table/tbody//a[contains(@href, 'https://myanimelist.net/anime/')]/@href",
                        "synonyms" to "//h2[contains(text(), 'Information')]/following-sibling::*//tr[0]/td[1]",
                    ))

                    // then
                    assertThat(result.string("title")).isEqualTo("Death Note")
                    assertThat(result.string("episodes")).isEqualTo("37")
                    assertThat(result.string("source")).isEqualTo("https://myanimelist.net/anime/1535/Death_Note")
                    assertThat(result.string("status")).isEqualTo("Finished Airing")
                    assertThat(result.listNotNull<Tag>("tags")).containsExactlyInAnyOrder(
                        "Supernatural",
                        "Suspense",
                        "Detective",
                        "Psychological",
                        "Shounen",
                    )
                    assertThat(result.string("type")).isEqualTo("TV")
                    assertThat(result.string("duration")).isEqualTo("23 min. per ep.")
                    assertThat(result.string("premiered")).isEqualTo("Fall 2006")
                    assertThat(result.string("aired")).isEqualTo("Oct 4, 2006 to Jun 27, 2007")
                    assertThat(result.string("picture")).isEqualTo("https://cdn.myanimelist.net/images/anime/9/9453.jpg")
                    assertThat(result.string("relatedAnime")).isEqualTo("https://myanimelist.net/anime/2994/Death_Note__Rewrite")
                    assertThat(result.listNotNull<Title>("synonyms")).containsExactlyInAnyOrder(
                        "Death Note",
                        "DN",
                        "デスノート",
                    )
                }
            }
        }
    }
}