package com.lsg.obolobibles.data.repository

import android.content.Context
import com.lsg.obolobibles.data.local.BookCodeAndName
import com.lsg.obolobibles.data.local.OboloBibleDatabase
import com.lsg.obolobibles.data.model.BibleEntity
import com.lsg.obolobibles.data.model.BookmarkEntity
import com.lsg.obolobibles.data.model.TranslationInfo
import com.lsg.obolobibles.data.parser.OboloBibleParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class OboloBibleRepository(private val context: Context) {

    private val db = OboloBibleDatabase.getDatabase(context)
    private val dao = db.bibleDao()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importProgress = MutableStateFlow("Initializing...")
    val importProgress: StateFlow<String> = _importProgress.asStateFlow()

    val availableTranslations = listOf(
        TranslationInfo(
            key = "jw",
            displayName = "Obolo Jehovah's Witness Bible",
            description = "Obolo Jehovah's Witness Bible Edition"
        ),
        TranslationInfo(
            key = "protestant",
            displayName = "Obolo Protestant Bible",
            description = "Ikpa Usem Obolo (Protestant Edition)"
        ),
        TranslationInfo(
            key = "catholic",
            displayName = "Obolo Catholic Bible",
            description = "Ikpa Usem Obolo (Catholic Edition)"
        )
    )

    suspend fun ensureDatabaseInitialized() = withContext(Dispatchers.IO) {
        val countProt = dao.getVerseCountForTranslation("protestant")
        val countCath = dao.getVerseCountForTranslation("catholic")
        val countJw = dao.getVerseCountForTranslation("jw")

        if (countProt > 0 && countCath > 0 && countJw > 0) {
            return@withContext
        }

        _isImporting.value = true

        if (countProt == 0) {
            _importProgress.value = "Importing Obolo Protestant Bible..."
            val protVerses = OboloBibleParser.parseAssetFolder(
                context, "sab_protestant", "protestant", "Obolo Protestant Bible"
            )
            if (protVerses.isNotEmpty()) {
                dao.insertVerses(protVerses)
            }
        }

        if (countCath == 0) {
            _importProgress.value = "Importing Obolo Catholic Bible..."
            val cathVerses = OboloBibleParser.parseAssetFolder(
                context, "sab_catholic", "catholic", "Obolo Catholic Bible"
            )
            if (cathVerses.isNotEmpty()) {
                dao.insertVerses(cathVerses)
            }
        }

        if (countJw == 0) {
            _importProgress.value = "Importing Obolo JW Bible..."
            val jwVerses = OboloBibleParser.parseAssetFolder(
                context, "Obolo Jehovah Witness Bible", "jw", "Obolo JW Bible"
            )
            if (jwVerses.isNotEmpty()) {
                dao.insertVerses(jwVerses)
            }
        }

        _isImporting.value = false
    }

    fun getVerses(translationKey: String, bookCode: String, chapter: Int): Flow<List<BibleEntity>> {
        return dao.getVerses(translationKey, bookCode, chapter)
    }

    fun getAvailableBooks(translationKey: String): Flow<List<BookCodeAndName>> {
        return dao.getAvailableBooks(translationKey)
    }

    suspend fun getChapterCount(translationKey: String, bookCode: String): Int {
        return dao.getChapterCount(translationKey, bookCode) ?: 1
    }

    fun searchVerses(query: String, translationKey: String? = null): Flow<List<BibleEntity>> {
        return if (translationKey.isNullOrBlank()) {
            dao.searchVersesAll(query)
        } else {
            dao.searchVersesInTranslation(translationKey, query)
        }
    }

    suspend fun addCustomVerse(
        translationKey: String,
        translationName: String,
        bookCode: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        text: String
    ): Long = withContext(Dispatchers.IO) {
        val customVerse = BibleEntity(
            translationKey = translationKey,
            translationName = translationName,
            bookCode = bookCode,
            bookName = bookName,
            chapter = chapter,
            verse = verse,
            text = text,
            isCustom = true
        )
        dao.insertVerse(customVerse)
    }

    suspend fun addBookmark(
        translationKey: String,
        bookCode: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        verseText: String,
        note: String? = null,
        highlightHex: String? = null
    ) = withContext(Dispatchers.IO) {
        val bookmark = BookmarkEntity(
            translationKey = translationKey,
            bookCode = bookCode,
            bookName = bookName,
            chapter = chapter,
            verse = verse,
            verseText = verseText,
            note = note,
            highlightColorHex = highlightHex
        )
        dao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        dao.deleteBookmark(bookmark)
    }

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return dao.getAllBookmarks()
    }
}
