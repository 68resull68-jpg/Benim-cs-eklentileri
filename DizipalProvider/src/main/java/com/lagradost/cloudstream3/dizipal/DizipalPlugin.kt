package com.lagradost.cloudstream3.dizipal

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class DizipalPlugin: Plugin() {
    override fun getMainApi(): MainAPI { return Dizipal() }
}

class Dizipal : MainAPI() {
    override var mainUrl = "https://dizipal818.com"
    override var name = "Dizipal"
    override val hasMainPage = true
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.TvSeries)

    override val mainPage = mainPageOf(
        "$mainUrl/son-eklenenler/" to "Son Eklenenler"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if(page == 1) request.data else "${request.data}page/$page/"
        val doc = app.get(url).document
        val list = doc.select("div.dizibox").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, list)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.select("h3").text()
        val href = this.select("a").attr("href")
        val posterUrl = this.select("img").attr("data-src")
        
        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
        }
    }
}
