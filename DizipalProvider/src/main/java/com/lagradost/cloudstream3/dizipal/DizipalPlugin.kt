package com.lagradost.cloudstream3.dizipal

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class DizipalProvider : MainAPI() {
    override var mainUrl = "https://dizipal818.com"
    override var name = "Dizipal"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Movie)

    override val mainPage = mainPageOf(
        "$mainUrl/son-eklenenler/" to "Son Eklenenler",
        "$mainUrl/filmler/" to "Filmler", 
        "$mainUrl/diziler/" to "Diziler"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(if(page == 1) request.data else "${request.data}page/$page/").document
        val home = document.select("div.col-md-2.col-sm-3.col-6").mapNotNull { it.toSearchResult() }
        return HomePageResponse(request.name, home) // newHomePageResponse -> HomePageResponse
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.col-md-2.col-sm-3.col-6").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val a = this.selectFirst("a") ?: return null
        val title = a.attr("title")
        val href = a.attr("href")
        val poster = this.selectFirst("img")?.attr("data-src") ?: this.selectFirst("img")?.attr("src")
        val isSeries = href.contains("/dizi-")
        
        return if(isSeries) {
            TvSeriesSearchResponse(title, href, TvType.TvSeries, posterUrl = poster) // newTvSeriesSearchResponse -> TvSeriesSearchResponse
        } else {
            MovieSearchResponse(title, href, TvType.Movie, posterUrl = poster) // newMovieSearchResponse -> MovieSearchResponse
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        
        val title = document.selectFirst("h1")?.text()?.trim() ?: ""
        val poster = document.selectFirst("div.poster img")?.attr("src")
        val plot = document.selectFirst("div.text")?.text()?.trim()
        
        val episodeElements = document.select("div.episode a, ul li.episode a")
        val episodes = episodeElements.map { 
            Episode(it.attr("href"), name = it.text().trim()) // newEpisode -> Episode
        }.reversed()

        return if(episodes.isEmpty()) {
            MovieLoadResponse(title, url, TvType.Movie, url, posterUrl = poster, plot = plot) // newMovieLoadResponse -> MovieLoadResponse
        } else {
            TvSeriesLoadResponse(title, url, TvType.TvSeries, episodes, posterUrl = poster, plot = plot) // newTvSeriesLoadResponse -> TvSeriesLoadResponse
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit, // Callback<SubtitleFile> -> (SubtitleFile) -> Unit
        callback: (ExtractorLink) -> Boolean // Callback<ExtractorLink> -> (ExtractorLink) -> Boolean
    ): Boolean {
        val document = app.get(data).document
        document.select("iframe").forEach {
            val src = it.attr("src")
            if(src.isNotBlank()) loadExtractor(src, mainUrl, subtitleCallback, callback)
        }
        return true
    }
}
