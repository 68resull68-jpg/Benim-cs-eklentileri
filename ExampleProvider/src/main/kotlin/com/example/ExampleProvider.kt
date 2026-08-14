package com.example

import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import org.json.JSONObject
import java.net.URLEncoder

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://archive.org"
    override var name = "Benim Film Eklentilerim"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "tr"
    override val hasMainPage = true

    override suspend fun search(query: String): List<SearchResponse> {
        if (query.isBlank()) return emptyList()

        val encoded = URLEncoder.encode(query, "UTF-8")

        val url =
            "$mainUrl/advancedsearch.php" +
            "?q=title%3A%28$encoded%29+AND+mediatype%3Amovies" +
            "&fl%5B%5D=identifier" +
            "&fl%5B%5D=title" +
            "&fl%5B%5D=year" +
            "&rows=20" +
            "&output=json"

        val response = app.get(url).text
        val json = JSONObject(response)

        val docs = json
            .getJSONObject("response")
            .getJSONArray("docs")

        val results = mutableListOf<SearchResponse>()

        for (i in 0 until docs.length()) {
            val item = docs.getJSONObject(i)

            val identifier = item.optString("identifier")
            if (identifier.isBlank()) continue

            val title = item.optString("title")
                .ifBlank { identifier }

            val year = item.optInt("year", 0)
                .takeIf { it > 0 }

            results.add(
                newMovieSearchResponse(
                    name = title,
                    url = "$mainUrl/details/$identifier",
                    type = TvType.Movie
                ) {
                    this.year = year
                    this.posterUrl = "$mainUrl/services/img/$identifier"
                }
            )
        }

        return results
    }

    override suspend fun load(url: String): LoadResponse {
        val identifier = url.substringAfterLast("/details/")

        val response = app.get(
            "$mainUrl/metadata/$identifier"
        ).text

        val json = JSONObject(response)

        val title = json.optString("title")
            .ifBlank { identifier }

        val description = if (json.has("description") && !json.isNull("description")) {
    json.get("description").toString()
        .takeIf { it.isNotBlank() }
} else {
    null
}

        val year = json.optInt("year", 0)
            .takeIf { it > 0 }

        return newMovieLoadResponse(
            name = title,
            url = url,
            type = TvType.Movie,
            dataUrl = url
        ) {
            this.posterUrl = "$mainUrl/services/img/$identifier"
            this.plot = description
            this.year = year
        }
    }
}
