package com.toyent.hiqradio.data.api

import com.toyent.hiqradio.data.model.Country
import com.toyent.hiqradio.data.model.Language
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.data.model.State
import com.toyent.hiqradio.data.model.Tag
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Radio Browser API service interface
 */
interface RadioApiService {

    /**
     * Get top voted stations
     */
    @GET("json/stations/topvote/{limit}")
    suspend fun getTopStations(
        @Path("limit") limit: Int = 100
    ): List<Station>

    /**
     * Search stations by various criteria
     */
    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("nameExact") nameExact: Boolean = false,
        @Query("country") country: String? = null,
        @Query("countryExact") countryExact: Boolean = false,
        @Query("countrycode") countryCode: String? = null,
        @Query("state") state: String? = null,
        @Query("stateExact") stateExact: Boolean = false,
        @Query("language") language: String? = null,
        @Query("languageExact") languageExact: Boolean = false,
        @Query("tag") tag: String? = null,
        @Query("tagExact") tagExact: Boolean = false,
        @Query("tagList") tagList: String? = null,
        @Query("bitrateMin") bitrateMin: Int? = null,
        @Query("bitrateMax") bitrateMax: Int? = null,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<Station>

    /**
     * Get station by UUID
     */
    @GET("json/stations/byuuid/{uuid}")
    suspend fun getStationByUuid(
        @Path("uuid") uuid: String
    ): List<Station>

    /**
     * Get all countries
     */
    @GET("json/countries/")
    suspend fun getCountries(): List<Country>

    /**
     * Get country by code
     */
    @GET("json/countries/{code}")
    suspend fun getCountryByCode(
        @Path("code") code: String
    ): List<Country>

    /**
     * Get all country codes
     */
    @GET("json/countrycodes/")
    suspend fun getCountryCodes(): List<Country>

    /**
     * Get all states/regions
     */
    @GET("json/states/")
    suspend fun getStates(): List<State>

    /**
     * Get states by country
     */
    @GET("json/states/")
    suspend fun getStatesByCountry(
        @Query("country") country: String
    ): List<State>

    /**
     * Get all languages
     */
    @GET("json/languages/")
    suspend fun getLanguages(): List<Language>

    /**
     * Get all tags
     */
    @GET("json/tags/")
    suspend fun getTags(
        @Query("limit") limit: Int = 100,
        @Query("order") order: String = "stationcount",
        @Query("reverse") reverse: Boolean = true
    ): List<Tag>

    /**
     * Get all codecs
     */
    @GET("json/codecs/")
    suspend fun getCodecs(): List<Tag>
}
