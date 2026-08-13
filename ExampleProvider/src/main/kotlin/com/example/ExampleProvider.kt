package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.app
import kotlinx.serialization.Serializable

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://graphql.anilist.co"
    override var name = "AniList TR"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "tr"
    override val hasMainPage = false

    @Serializable
    data class AniListResponse(
        val data: Data
    )

    @Serializable
    data class Data(
        val Page: Page
    )

    @Serializable
    data class Page(
        val media: List<Media>
    )

    @Serializable
    data class Media(
        val id: Int,
        val title: Title,
        val seasonYear: Int? = null,
        val coverImage: CoverImage? = null
    )

    @Serializable
    data class Title(
        val romaji: String? = null,
        val english: String? = null,
        val native: String? = null
    )

    @Serializable
    data class CoverImage(
        val large: String? = null
    )

    override suspend fun search(query: String): List<SearchResponse> {

        if (query.isBlank()) return emptyList()

        val graphql = """
            query SearchAnime(${"$"}search: String) {
                Page(perPage: 20) {
                    media(
                        search: ${"$"}search,
                        type: ANIME
                    ) {
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
            "query" to graphql,
            "variables" to mapOf(
                "search" to query
            )
        )

        val response = app.post(
            "https://graphql.anilist.co",
            json = body
        ).parsed<AniListResponse>()

        return response.data.Page.media.map { anime ->

            val title =
                anime.title.english
                    ?: anime.title.romaji
                    ?: anime.title.native
                    ?: "Bilinmeyen Anime"

            newMovieSearchResponse(
                name = title,
                url = "https://anilist.co/anime/${anime.id}",
                type = TvType.Movie
            ) {
                year = anime.seasonYear
                posterUrl = anime.coverImage?.large
            }
        }
    }
}
