package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.newMovieSearchResponse

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://example.com/"
    override var name = "Benim Türkçe Film"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "tr"
    override val hasMainPage = false

    private data class Film(
        val title: String,
        val year: Int,
        val url: String,
        val poster: String?
    )

    private val films = listOf(
        Film(
            "Örnek Film 1",
            2024,
            "https://example.com/film-1",
            null
        ),
        Film(
            "Örnek Film 2",
            2023,
            "https://example.com/film-2",
            null
        ),
        Film(
            "Örnek Film 3",
            2022,
            "https://example.com/film-3",
            null
        )
    )

    override suspend fun search(query: String): List<SearchResponse> {
        if (query.isBlank()) return emptyList()

        return films
            .filter { it.title.contains(query, ignoreCase = true) }
            .map { film ->
                newMovieSearchResponse(
                    name = film.title,
                    url = film.url,
                    type = TvType.Movie
                ) {
                    year = film.year
                    posterUrl = film.poster
                }
            }
    }
}
