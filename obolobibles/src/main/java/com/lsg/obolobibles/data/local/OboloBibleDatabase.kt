package com.lsg.obolobibles.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lsg.obolobibles.data.model.BibleEntity
import com.lsg.obolobibles.data.model.BookmarkEntity

@Database(
    entities = [BibleEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class OboloBibleDatabase : RoomDatabase() {

    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: OboloBibleDatabase? = null

        fun getDatabase(context: Context): OboloBibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OboloBibleDatabase::class.java,
                    "obolo_bibles_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
