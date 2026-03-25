package com.toyent.hiqradio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Favorite station entity for local database
 */
@Entity(tableName = "favorites")
data class FavoriteStation(
    @PrimaryKey
    val stationUuid: String,
    val name: String,
    val urlResolved: String,
    val homepage: String? = null,
    val favicon: String? = null,
    val tags: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val state: String? = null,
    val language: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val addedTime: Long = System.currentTimeMillis()
)

/**
 * Recently played station entity
 */
@Entity(tableName = "recently_played")
data class RecentlyPlayed(
    @PrimaryKey
    val stationUuid: String,
    val name: String,
    val urlResolved: String,
    val homepage: String? = null,
    val favicon: String? = null,
    val tags: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val state: String? = null,
    val language: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val playedTime: Long = System.currentTimeMillis()
)

/**
 * Custom station entity
 */
@Entity(tableName = "my_stations")
data class MyStation(
    @PrimaryKey
    val stationUuid: String,
    val name: String,
    val urlResolved: String,
    val homepage: String? = null,
    val favicon: String? = null,
    val tags: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val state: String? = null,
    val language: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val addedTime: Long = System.currentTimeMillis()
)

/**
 * App settings model
 */
data class AppSettings(
    var themeMode: ThemeMode = ThemeMode.SYSTEM,
    var locale: String = "",
    var autoStart: Boolean = false,
    var stopTimer: Long = 0,
    var lastPlayedStationUuid: String? = null,
    var lastPlayedStationName: String? = null,
    var lastPlayedStationUrl: String? = null
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
