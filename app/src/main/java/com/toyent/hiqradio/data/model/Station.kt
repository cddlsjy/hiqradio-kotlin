package com.toyent.hiqradio.data.model

import com.google.gson.annotations.SerializedName

/**
 * Station data model representing a radio station
 */
data class Station(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("stationuuid")
    val stationUuid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("url_resolved")
    val urlResolved: String,

    @SerializedName("homepage")
    val homepage: String? = null,

    @SerializedName("favicon")
    val favicon: String? = null,

    @SerializedName("tags")
    val tags: String? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("countrycode")
    val countryCode: String? = null,

    @SerializedName("state")
    val state: String? = null,

    @SerializedName("language")
    val language: String? = null,

    @SerializedName("codec")
    val codec: String? = null,

    @SerializedName("bitrate")
    val bitrate: Int? = null,

    @SerializedName("is_custom")
    val isCustom: Int = 0,

    @SerializedName("votes")
    val votes: Int? = null,

    @SerializedName("clickcount")
    val clickCount: Int? = null
) {
    fun getDisplayName(): String = name.trim()

    fun getDisplayTags(): String? = tags?.trim()

    fun isFavorite(): Boolean = isCustom == 1
}

/**
 * Country data model
 */
data class Country(
    @SerializedName("name")
    val name: String,

    @SerializedName("iso_3166_1")
    val isoCode: String,

    @SerializedName("stationcount")
    val stationCount: Int
)

/**
 * Language data model
 */
data class Language(
    @SerializedName("name")
    val name: String,

    @SerializedName("iso_639")
    val isoCode: String,

    @SerializedName("stationcount")
    val stationCount: Int
)

/**
 * State/Region data model
 */
data class State(
    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("stationcount")
    val stationCount: Int
)

/**
 * Tag data model
 */
data class Tag(
    @SerializedName("name")
    val name: String,

    @SerializedName("stationcount")
    val stationCount: Int
)

/**
 * Search result wrapper
 */
data class SearchResult<T>(
    val data: List<T>,
    val total: Int = data.size
)
