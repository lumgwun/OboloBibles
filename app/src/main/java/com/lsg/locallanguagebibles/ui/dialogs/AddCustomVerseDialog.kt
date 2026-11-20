package com.lsg.locallanguagebibles.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddCustomVerseDialog(
    currentBookCode: String,
    currentBookName: String,
    currentChapter: Int,
    onAddVerse: (bookCode: String, bookName: String, chapter: Int, verse: Int, text: String) -> Unit,
    onDismiss: () -> Unit
) {
    var bookCode by remember { mutableStateOf(currentBookCode) }
    var bookName by remember { mutableStateOf(currentBookName) }
    var chapterStr by remember { mutableStateOf(currentChapter.toString()) }
    var verseStr by remember { mutableStateOf("1") }
    var verseText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add / Contribute Verse") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bookName,
                    onValueChange = { bookName = it },
                    label = { Text("Book Name (e.g. Matyu)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chapterStr,
                        onValueChange = { chapterStr = it },
                        label = { Text("Chapter") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = verseStr,
                        onValueChange = { verseStr = it },
                        label = { Text("Verse") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = verseText,
                    onValueChange = { verseText = it },
                    label = { Text("Verse Text (in Obolo or English)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chap = chapterStr.toIntOrNull()
                    val vNum = verseStr.toIntOrNull()
                    if (chap == null || vNum == null || verseText.isBlank()) {
                        errorMessage = "Please enter valid chapter, verse, and text."
                    } else {
                        onAddVerse(
                            bookCode.ifBlank { "CUS" },
                            bookName.ifBlank { "Custom" },
                            chap,
                            vNum,
                            verseText.trim()
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Save Verse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
