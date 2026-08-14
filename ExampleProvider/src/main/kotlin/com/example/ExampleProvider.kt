package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newMovieSearchResponse
import org.json.JSONObject

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://graphql.anilist.co"
    override var name = "Benim Film Eklentim"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "tr"
    override val hasMainPage = false

    override suspend fun search(query: String): List<SearchResponse> {

        if (query.isBlank()) return emptyList()

        val graphql = """
            query {
                Page(perPage: 20) {
                    media(search: "$query", type: ANIME) {
                        id
                        title {
                            romaji
                            english
                            native
                        }
                        seasonYear
                        coverImage {
                            large
                        }
                    }
                }
            }
        """.trimIndent()

        val body = mapOf(
            "query" to graphql
        )

        val response = app.post(
            "https://graphql.anilist.co",
            json = body
        ).text

        val json = JSONObject(response)
        val media = json
            .getJSONObject("data")
            .getJSONObject("Page")
            .getJSONArray("media")

        val results = mutableListOf<SearchResponse>()

        for (i in 0 until media.length()) {

            val anime = media.getJSONObject(i)

            val titleObject = anime.getJSONObject("title")

            val title =
                titleObject.optString("english").takeIf { it.isNotBlank() }
                    ?: titleObject.optString("romaji").takeIf { it.isNotBlank() }
                    ?: titleObject.optString("native")

            val id = anime.getInt("id")

            val year =
                if (!anime.isNull("seasonYear"))
                    anime.getInt("seasonYear")
                else
                    null

            val poster =
                if (!anime.isNull("coverImage"))
                    anime.getJSONObject("coverImage").optString("large")
                else
                    null

            results.add(
                newMovieSearchResponse(
                    name = title,
                    url = "https://anilist.co/anime/$id",
                    type = TvType.Movie
                ) {
                    this.year = year
                    this.posterUrl = poster
                }
            )
        }

        return results
    }
}
