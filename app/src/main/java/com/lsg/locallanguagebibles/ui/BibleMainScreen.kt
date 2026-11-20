package com.lsg.locallanguagebibles.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsg.locallanguagebibles.ui.components.*
import com.lsg.locallanguagebibles.ui.dialogs.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleMainScreen(
    viewModel: BibleViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isImporting by viewModel.isImporting.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()

    val selectedTranslationKey by viewModel.selectedTranslationKey.collectAsState()
    val selectedBookCode by viewModel.selectedBookCode.collectAsState()
    val selectedBookName by viewModel.selectedBookName.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val maxChapterCount by viewModel.maxChapterCount.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()

    val verses by viewModel.verses.collectAsState()
    val availableBooks by viewModel.availableBooks.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    var showTranslationDropdown by remember { mutableStateOf(false) }
    var showBookChapterPicker by remember { mutableStateOf(false) }
    var showAddVerseDialog by remember { mutableStateOf(false) }
    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    val currentTranslationInfo = viewModel.availableTranslations.find { it.key == selectedTranslationKey }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable { showTranslationDropdown = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentTranslationInfo?.displayName ?: "Obolo Bible",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Translation")
                        }
                        Text(
                            text = "$selectedBookName Chapter $selectedChapter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        DropdownMenu(
                            expanded = showTranslationDropdown,
                            onDismissRequest = { showTranslationDropdown = false }
                        ) {
                            viewModel.availableTranslations.forEach { trans ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(trans.displayName, fontWeight = FontWeight.Bold)
                                            Text(trans.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectTranslation(trans.key)
                                        showTranslationDropdown = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Book/Chapter Picker Pill
                    FilterChip(
                        selected = true,
                        onClick = { showBookChapterPicker = true },
                        label = { Text("$selectedBookName $selectedChapter") },
                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )

                    IconButton(onClick = { showSearchSheet = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    IconButton(onClick = { showBookmarksSheet = true }) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                    }

                    IconButton(onClick = {
                        if (audioState.isPlaying) {
                            viewModel.pauseOrResumeAudio()
                        } else {
                            viewModel.startAudioPlayback(0)
                        }
                    }) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Audio Reader",
                            tint = if (audioState.isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { showAddVerseDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom Verse")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                AudioControlBar(
                    audioState = audioState,
                    onPauseOrResume = { viewModel.pauseOrResumeAudio() },
                    onStop = { viewModel.stopAudio() },
                    onOpenSettings = { showVoiceSettingsDialog = true }
                )

                // Chapter Navigation Bar
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.previousChapter() },
                            enabled = selectedChapter > 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev Chapter")
                        }

                        // Font Size Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.setFontSize(fontSizeSp - 2f) }) {
                                Text("A-", fontWeight = FontWeight.Bold)
                            }
                            Text("${fontSizeSp.toInt()}sp", style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { viewModel.setFontSize(fontSizeSp + 2f) }) {
                                Text("A+", fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { viewModel.nextChapter() },
                            enabled = selectedChapter < maxChapterCount
                        ) {
                            Text("Next Chapter")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isImporting) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Preparing Obolo Bibles...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = importProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AnimatedContent(
                    targetState = Pair(selectedBookCode, selectedChapter),
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ChapterTransition"
                ) { (_, chapter) ->
                    if (verses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No verses found in this chapter.",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { showAddVerseDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Contribute / Add Verse")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$selectedBookName",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Chapter $chapter",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            itemsIndexed(verses, key = { _, v -> v.id }) { index, verse ->
                                val isBeingRead = audioState.currentVerseIndex == index
                                val isBookmarked = bookmarks.any {
                                    it.bookCode == verse.bookCode &&
                                            it.chapter == verse.chapter &&
                                            it.verse == verse.verse
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isBeingRead) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable {
                                            viewModel.startAudioPlayback(index)
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isBeingRead) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${verse.verse}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBeingRead) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = verse.text,
                                            fontSize = fontSizeSp.sp,
                                            lineHeight = (fontSizeSp * 1.4f).sp,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString("${verse.bookName} ${verse.chapter}:${verse.verse} - ${verse.text}"))
                                                Toast.makeText(context, "Copied verse to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy Verse",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.toggleBookmark(verse) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark Verse",
                                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showBookChapterPicker) {
        BookChapterPickerDialog(
            availableBooks = availableBooks,
            selectedBookCode = selectedBookCode,
            selectedChapter = selectedChapter,
            maxChapterCount = maxChapterCount,
            onBookSelected = { code, name -> viewModel.selectBook(code, name) },
            onChapterSelected = { chap -> viewModel.selectChapter(chap) },
            onDismiss = { showBookChapterPicker = false }
        )
    }

    if (showAddVerseDialog) {
        AddCustomVerseDialog(
            currentBookCode = selectedBookCode,
            currentBookName = selectedBookName,
            currentChapter = selectedChapter,
            onAddVerse = { code, name, chap, verse, text ->
                viewModel.addCustomVerse(code, name, chap, verse, text)
            },
            onDismiss = { showAddVerseDialog = false }
        )
    }

    if (showVoiceSettingsDialog) {
        VoiceSettingsDialog(
            audioState = audioState,
            onSpeechRateChange = { viewModel.setSpeechRate(it) },
            onPitchChange = { viewModel.setPitch(it) },
            onVoiceSelect = { viewModel.setVoice(it) },
            onDismiss = { showVoiceSettingsDialog = false }
        )
    }

    if (showSearchSheet) {
        SearchSheet(
            query = viewModel.searchQuery.collectAsState().value,
            results = searchResults,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onResultSelected = { verse ->
                viewModel.selectTranslation(verse.translationKey)
                viewModel.selectBook(verse.bookCode, verse.bookName)
                viewModel.selectChapter(verse.chapter)
            },
            onDismiss = { showSearchSheet = false }
        )
    }

    if (showBookmarksSheet) {
        BookmarksSheet(
            bookmarks = bookmarks,
            onSelectBookmark = { bm ->
                viewModel.selectTranslation(bm.translationKey)
                viewModel.selectBook(bm.bookCode, bm.bookName)
                viewModel.selectChapter(bm.chapter)
            },
            onDeleteBookmark = { bm -> viewModel.deleteBookmark(bm) },
            onDismiss = { showBookmarksSheet = false }
        )
    }
}
