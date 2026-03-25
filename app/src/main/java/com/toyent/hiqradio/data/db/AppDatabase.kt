package com.toyent.hiqradio.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.toyent.hiqradio.data.model.FavoriteStation
import com.toyent.hiqradio.data.model.MyStation
import com.toyent.hiqradio.data.model.RecentlyPlayed

/**
 * Room database for the app
 */
@Database(
    entities = [
        FavoriteStation::class,
        RecentlyPlayed::class,
        MyStation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun myStationDao(): MyStationDao

    companion object {
        private const val DATABASE_NAME = "hiqradio_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
