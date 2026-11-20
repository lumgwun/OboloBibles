package com.lsg.obolobibles.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bible_verses",
    indices = [
        Index(value = ["translationKey", "bookCode", "chapter"]),
        Index(value = ["translationKey", "bookCode", "chapter", "verse"])
    ]
)
data class BibleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val translationKey: String, // "protestant", "catholic", "jw", "custom"
    val translationName: String,
    val bookCode: String,      // e.g. "MAT", "GEN"
    val bookName: String,      // e.g. "Matyu", "Generis"
    val testament: String = "NT", // "OT" or "NT"
    val chapter: Int,
    val verse: Int,
    val text: String,
    val isCustom: Boolean = false
)

@Entity(tableName = "user_bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val translationKey: String,
    val bookCode: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val verseText: String,
    val note: String? = null,
    val highlightColorHex: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class BibleBookInfo(
    val code: String,
    val name: String,
    val testament: String,
    val chapterCount: Int
)

data class TranslationInfo(
    val key: String,
    val displayName: String,
    val description: String
)
