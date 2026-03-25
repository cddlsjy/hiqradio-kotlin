package com.toyent.hiqradio.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.toyent.hiqradio.data.api.ApiClient
import com.toyent.hiqradio.data.db.AppDatabase
import com.toyent.hiqradio.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for radio stations data
 */
class RadioRepository(context: Context) {

    private val apiService = ApiClient.getApiService()
    private val database = AppDatabase.getInstance(context)
    private val favoriteDao = database.favoriteDao()
    private val recentlyPlayedDao = database.recentlyPlayedDao()
    private val myStationDao = database.myStationDao()

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "hiqradio_prefs",
        Context.MODE_PRIVATE
    )

    // ==================== API Methods ====================

    suspend fun getTopStations(limit: Int = 100): Result<List<Station>> {
        return tryWithServerSwitch {
            apiService.getTopStations(limit)
        }
    }

    suspend fun searchStations(
        name: String? = null,
        country: String? = null,
        countryCode: String? = null,
        state: String? = null,
        language: String? = null,
        tag: String? = null,
        tagList: String? = null,
        bitrateMin: Int? = null,
        bitrateMax: Int? = null,
        order: String = "votes",
        offset: Int = 0,
        limit: Int = 100
    ): Result<List<Station>> {
        return tryWithServerSwitch {
            apiService.searchStations(
                name = name,
                country = country,
                countryCode = countryCode,
                state = state,
                language = language,
                tag = tag,
                tagList = tagList,
                bitrateMin = bitrateMin,
                bitrateMax = bitrateMax,
                order = order,
                offset = offset,
                limit = limit
            )
        }
    }

    suspend fun getCountries(): Result<List<Country>> {
        return tryWithServerSwitch {
            apiService.getCountries()
        }
    }

    suspend fun getLanguages(): Result<List<Language>> {
        return tryWithServerSwitch {
            apiService.getLanguages()
        }
    }

    suspend fun getTags(limit: Int = 100): Result<List<Tag>> {
        return tryWithServerSwitch {
            apiService.getTags(limit = limit)
        }
    }

    /**
     * Helper function to handle API calls with server switching
     */
    private suspend fun <T> tryWithServerSwitch(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            // Try next server
            ApiClient.switchServer()
            try {
                Result.success(block())
            } catch (e2: Exception) {
                // Try third server
                ApiClient.switchServer()
                try {
                    Result.success(block())
                } catch (e3: Exception) {
                    // All servers failed, reset to first server
                    ApiClient.resetServer()
                    Result.failure(e3)
                }
            }
        }
    }

    // ==================== Favorite Methods ====================

    fun getAllFavorites(): Flow<List<FavoriteStation>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun isFavorite(uuid: String): Boolean {
        return favoriteDao.isFavorite(uuid)
    }

    suspend fun addToFavorites(station: Station) {
        val favorite = FavoriteStation(
            stationUuid = station.stationUuid,
            name = station.name.trim(),
            urlResolved = station.urlResolved,
            homepage = station.homepage,
            favicon = station.favicon,
            tags = station.tags,
            country = station.country,
            countryCode = station.countryCode,
            state = station.state,
            language = station.language,
            codec = station.codec,
            bitrate = station.bitrate
        )
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun removeFromFavorites(uuid: String) {
        favoriteDao.deleteFavoriteByUuid(uuid)
    }

    suspend fun getFavoriteCount(): Int {
        return favoriteDao.getFavoriteCount()
    }

    // ==================== Recently Played Methods ====================

    fun getRecentlyPlayed(limit: Int = 50): Flow<List<RecentlyPlayed>> {
        return recentlyPlayedDao.getRecentlyPlayed(limit)
    }

    suspend fun addToRecentlyPlayed(station: Station) {
        val recently = RecentlyPlayed(
            stationUuid = station.stationUuid,
            name = station.name.trim(),
            urlResolved = station.urlResolved,
            homepage = station.homepage,
            favicon = station.favicon,
            tags = station.tags,
            country = station.country,
            countryCode = station.countryCode,
            state = station.state,
            language = station.language,
            codec = station.codec,
            bitrate = station.bitrate
        )
        recentlyPlayedDao.insertRecentlyPlayed(recently)
        // Keep only recent 50 items
        recentlyPlayedDao.keepRecentCount(50)
    }

    // ==================== My Stations Methods ====================

    fun getMyStations(): Flow<List<MyStation>> {
        return myStationDao.getAllMyStations()
    }

    suspend fun addMyStation(station: Station) {
        val myStation = MyStation(
            stationUuid = station.stationUuid,
            name = station.name.trim(),
            urlResolved = station.urlResolved,
            homepage = station.homepage,
            favicon = station.favicon,
            tags = station.tags,
            country = station.country,
            countryCode = station.countryCode,
            state = station.state,
            language = station.language,
            codec = station.codec,
            bitrate = station.bitrate
        )
        myStationDao.insertMyStation(myStation)
    }

    suspend fun deleteMyStation(uuid: String) {
        myStationDao.deleteMyStationByUuid(uuid)
    }

    // ==================== Settings Methods ====================

    fun getThemeMode(): ThemeMode {
        val mode = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun getLocale(): String {
        return prefs.getString("locale", "") ?: ""
    }

    fun setLocale(locale: String) {
        prefs.edit().putString("locale", locale).apply()
    }

    fun getAutoStart(): Boolean {
        return prefs.getBoolean("auto_start", false)
    }

    fun setAutoStart(autoStart: Boolean) {
        prefs.edit().putBoolean("auto_start", autoStart).apply()
    }

    fun getStopTimer(): Long {
        return prefs.getLong("stop_timer", 0)
    }

    fun setStopTimer(timer: Long) {
        prefs.edit().putLong("stop_timer", timer).apply()
    }

    fun saveLastPlayedStation(station: Station?) {
        if (station != null) {
            prefs.edit()
                .putString("last_station_uuid", station.stationUuid)
                .putString("last_station_name", station.name)
                .putString("last_station_url", station.urlResolved)
                .apply()
        } else {
            prefs.edit()
                .remove("last_station_uuid")
                .remove("last_station_name")
                .remove("last_station_url")
                .apply()
        }
    }

    fun getLastPlayedStation(): Station? {
        val uuid = prefs.getString("last_station_uuid", null) ?: return null
        val name = prefs.getString("last_station_name", null) ?: return null
        val url = prefs.getString("last_station_url", null) ?: return null

        return Station(
            stationUuid = uuid,
            name = name,
            urlResolved = url
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: RadioRepository? = null

        fun getInstance(context: Context): RadioRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RadioRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
