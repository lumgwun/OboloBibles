package com.lsg.obolobibles.data.local

import androidx.room.*
import com.lsg.obolobibles.data.model.BibleEntity
import com.lsg.obolobibles.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(verse: BibleEntity): Long

    @Query("SELECT * FROM bible_verses WHERE translationKey = :translationKey AND bookCode = :bookCode AND chapter = :chapter ORDER BY verse ASC")
    fun getVerses(translationKey: String, bookCode: String, chapter: Int): Flow<List<BibleEntity>>

    @Query("SELECT DISTINCT bookCode, bookName, testament FROM bible_verses WHERE translationKey = :translationKey ORDER BY id ASC")
    fun getAvailableBooks(translationKey: String): Flow<List<BookCodeAndName>>

    @Query("SELECT MAX(chapter) FROM bible_verses WHERE translationKey = :translationKey AND bookCode = :bookCode")
    suspend fun getChapterCount(translationKey: String, bookCode: String): Int?

    @Query("SELECT * FROM bible_verses WHERE translationKey = :translationKey AND text LIKE '%' || :query || '%' ORDER BY bookCode, chapter, verse ASC LIMIT 100")
    fun searchVersesInTranslation(translationKey: String, query: String): Flow<List<BibleEntity>>

    @Query("SELECT * FROM bible_verses WHERE text LIKE '%' || :query || '%' ORDER BY translationKey, bookCode, chapter, verse ASC LIMIT 100")
    fun searchVersesAll(query: String): Flow<List<BibleEntity>>

    @Query("SELECT COUNT(*) FROM bible_verses WHERE translationKey = :translationKey")
    suspend fun getVerseCountForTranslation(translationKey: String): Int

    @Delete
    suspend fun deleteVerse(verse: BibleEntity)

    // Bookmarks & Notes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM user_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
}

data class BookCodeAndName(
    val bookCode: String,
    val bookName: String,
    val testament: String
)
