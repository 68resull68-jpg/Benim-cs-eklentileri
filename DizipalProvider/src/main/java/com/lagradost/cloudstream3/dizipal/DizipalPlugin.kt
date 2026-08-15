package com.lagradost.cloudstream3.dizipal

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newEpisode
import com.lagradost.cloudstream3.utils.newHomePageResponse
import com.lagradost.cloudstream3.utils.newMovieLoadResponse
import com.lagradost.cloudstream3.utils.newMovieSearchResponse
import com.lagradost.cloudstream3.utils.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.newTvSeriesSearchResponse
import org.jsoup.nodes.Element

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
        return newHomePageResponse(request.name, home)
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
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = poster }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = poster }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        
        val title = document.selectFirst("h1")?.text()?.trim() ?: ""
        val poster = document.selectFirst("div.poster img")?.attr("src")
        val plot = document.selectFirst("div.text")?.text()?.trim()
        
        val episodeElements = document.select("div.episode a, ul li.episode a")
        val episodes = episodeElements.map { 
            newEpisode(it.attr("href")) {
                name = it.text().trim()
            }
        }.reversed()

        return if(episodes.isEmpty()) {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: Callback<SubtitleFile>,
        callback: Callback<ExtractorLink>
    ): Boolean {
        val document = app.get(data).document
        document.select("iframe").forEach {
            val src = it.attr("src")
            if(src.isNotBlank()) loadExtractor(src, mainUrl, subtitleCallback, callback)
        }
        return true
    }
}
