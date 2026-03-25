package com.toyent.hiqradio.data.db

import androidx.room.*
import com.toyent.hiqradio.data.model.FavoriteStation
import com.toyent.hiqradio.data.model.MyStation
import com.toyent.hiqradio.data.model.RecentlyPlayed
import kotlinx.coroutines.flow.Flow

/**
 * DAO for favorite stations
 */
@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedTime DESC")
    fun getAllFavorites(): Flow<List<FavoriteStation>>

    @Query("SELECT * FROM favorites ORDER BY addedTime DESC")
    suspend fun getAllFavoritesList(): List<FavoriteStation>

    @Query("SELECT * FROM favorites WHERE stationUuid = :uuid")
    suspend fun getFavoriteByUuid(uuid: String): FavoriteStation?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stationUuid = :uuid)")
    suspend fun isFavorite(uuid: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: FavoriteStation)

    @Delete
    suspend fun deleteFavorite(station: FavoriteStation)

    @Query("DELETE FROM favorites WHERE stationUuid = :uuid")
    suspend fun deleteFavoriteByUuid(uuid: String)

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}

/**
 * DAO for recently played stations
 */
@Dao
interface RecentlyPlayedDao {

    @Query("SELECT * FROM recently_played ORDER BY playedTime DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<RecentlyPlayed>>

    @Query("SELECT * FROM recently_played ORDER BY playedTime DESC LIMIT :limit")
    suspend fun getRecentlyPlayedList(limit: Int = 50): List<RecentlyPlayed>

    @Query("SELECT * FROM recently_played WHERE stationUuid = :uuid")
    suspend fun getRecentlyPlayedByUuid(uuid: String): RecentlyPlayed?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyPlayed(station: RecentlyPlayed)

    @Query("DELETE FROM recently_played WHERE stationUuid = :uuid")
    suspend fun deleteRecentlyPlayedByUuid(uuid: String)

    @Query("DELETE FROM recently_played")
    suspend fun deleteAllRecentlyPlayed()

    @Query("DELETE FROM recently_played WHERE stationUuid NOT IN (SELECT stationUuid FROM recently_played ORDER BY playedTime DESC LIMIT :keepCount)")
    suspend fun keepRecentCount(keepCount: Int = 50)
}

/**
 * DAO for custom/my stations
 */
@Dao
interface MyStationDao {

    @Query("SELECT * FROM my_stations ORDER BY addedTime DESC")
    fun getAllMyStations(): Flow<List<MyStation>>

    @Query("SELECT * FROM my_stations ORDER BY addedTime DESC")
    suspend fun getAllMyStationsList(): List<MyStation>

    @Query("SELECT * FROM my_stations WHERE stationUuid = :uuid")
    suspend fun getMyStationByUuid(uuid: String): MyStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyStation(station: MyStation)

    @Delete
    suspend fun deleteMyStation(station: MyStation)

    @Query("DELETE FROM my_stations WHERE stationUuid = :uuid")
    suspend fun deleteMyStationByUuid(uuid: String)

    @Query("DELETE FROM my_stations")
    suspend fun deleteAllMyStations()
}
