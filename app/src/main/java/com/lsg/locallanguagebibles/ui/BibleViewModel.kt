package com.lsg.locallanguagebibles.ui

import android.app.Application
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lsg.locallanguagebibles.audio.AudioReaderManager
import com.lsg.obolobibles.data.local.BookCodeAndName
import com.lsg.obolobibles.data.model.BibleEntity
import com.lsg.obolobibles.data.model.BookmarkEntity
import com.lsg.obolobibles.data.model.TranslationInfo
import com.lsg.obolobibles.data.repository.OboloBibleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BibleViewModel(application: Application) : AndroidViewModel(application) {

    val repository = OboloBibleRepository(application)
    val audioReader = AudioReaderManager(application)

    val isImporting: StateFlow<Boolean> = repository.isImporting
    val importProgress: StateFlow<String> = repository.importProgress
    val availableTranslations: List<TranslationInfo> = repository.availableTranslations

    private val _selectedTranslationKey = MutableStateFlow("jw")
    val selectedTranslationKey: StateFlow<String> = _selectedTranslationKey.asStateFlow()

    private val _selectedBookCode = MutableStateFlow("MAT")
    val selectedBookCode: StateFlow<String> = _selectedBookCode.asStateFlow()

    private val _selectedBookName = MutableStateFlow("Matyu")
    val selectedBookName: StateFlow<String> = _selectedBookName.asStateFlow()

    private val _selectedChapter = MutableStateFlow(1)
    val selectedChapter: StateFlow<Int> = _selectedChapter.asStateFlow()

    private val _maxChapterCount = MutableStateFlow(28)
    val maxChapterCount: StateFlow<Int> = _maxChapterCount.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(18f)
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val verses: StateFlow<List<BibleEntity>> = combine(
        _selectedTranslationKey,
        _selectedBookCode,
        _selectedChapter
    ) { translationKey, bookCode, chapter ->
        Triple(translationKey, bookCode, chapter)
    }.flatMapLatest { (translationKey, bookCode, chapter) ->
        repository.getVerses(translationKey, bookCode, chapter)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableBooks: StateFlow<List<BookCodeAndName>> = _selectedTranslationKey.flatMapLatest { key ->
        repository.getAvailableBooks(key)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchResults: StateFlow<List<BibleEntity>> = combine(
        _searchQuery,
        _selectedTranslationKey
    ) { query, translation ->
        Pair(query, translation)
    }.flatMapLatest { (query, translation) ->
        if (query.length < 2) flowOf(emptyList())
        else repository.searchVerses(query, translation)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val audioState = audioReader.audioState

    init {
        viewModelScope.launch {
            repository.ensureDatabaseInitialized()
            updateMaxChapters()
        }
    }

    fun selectTranslation(key: String) {
        _selectedTranslationKey.value = key
        viewModelScope.launch {
            updateMaxChapters()
        }
    }

    fun selectBook(code: String, name: String) {
        _selectedBookCode.value = code
        _selectedBookName.value = name
        _selectedChapter.value = 1
        viewModelScope.launch {
            updateMaxChapters()
        }
    }

    fun selectChapter(chapter: Int) {
        _selectedChapter.value = chapter
    }

    fun nextChapter() {
        if (_selectedChapter.value < _maxChapterCount.value) {
            _selectedChapter.value++
        }
    }

    fun previousChapter() {
        if (_selectedChapter.value > 1) {
            _selectedChapter.value--
        }
    }

    private suspend fun updateMaxChapters() {
        val count = repository.getChapterCount(_selectedTranslationKey.value, _selectedBookCode.value)
        _maxChapterCount.value = if (count > 0) count else 1
    }

    fun setFontSize(size: Float) {
        _fontSizeSp.value = size.coerceIn(12f, 36f)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(verse: BibleEntity) {
        viewModelScope.launch {
            repository.addBookmark(
                translationKey = verse.translationKey,
                bookCode = verse.bookCode,
                bookName = verse.bookName,
                chapter = verse.chapter,
                verse = verse.verse,
                verseText = verse.text
            )
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    fun addCustomVerse(
        bookCode: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        text: String
    ) {
        viewModelScope.launch {
            repository.addCustomVerse(
                translationKey = _selectedTranslationKey.value,
                translationName = availableTranslations.find { it.key == _selectedTranslationKey.value }?.displayName ?: "Custom",
                bookCode = bookCode,
                bookName = bookName,
                chapter = chapter,
                verse = verse,
                text = text
            )
        }
    }

    fun startAudioPlayback(startIndex: Int = 0) {
        audioReader.startPlaylist(verses.value, startIndex)
    }

    fun pauseOrResumeAudio() {
        audioReader.pauseOrResume()
    }

    fun stopAudio() {
        audioReader.stop()
    }

    fun setSpeechRate(rate: Float) {
        audioReader.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        audioReader.setPitch(pitch)
    }

    fun setVoice(voice: Voice) {
        audioReader.setVoice(voice)
    }

    override fun onCleared() {
        super.onCleared()
        audioReader.shutdown()
    }
}
