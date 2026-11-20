package com.lsg.obolobibles.data.parser

import android.content.Context
import com.lsg.obolobibles.data.model.BibleEntity
import org.sil.app.lib.common.c.c
import org.sil.app.lib.common.c.k
import java.util.regex.Pattern

object OboloBibleParser {

    private val BOOK_NAMES = mapOf(
        "GEN" to Pair("Generis", "OT"),
        "EXO" to Pair("Eksodọs", "OT"),
        "LEV" to Pair("Levitikọs", "OT"),
        "NUM" to Pair("Nọmba", "OT"),
        "DEU" to Pair("Dọtẹronọmi", "OT"),
        "JOS" to Pair("Jọshua", "OT"),
        "JDG" to Pair("Oyoli", "OT"),
        "RUT" to Pair("Rut", "OT"),
        "1SA" to Pair("1 Samwẹlu", "OT"),
        "2SA" to Pair("2 Samwẹlu", "OT"),
        "1KI" to Pair("1 Ubọọn̄", "OT"),
        "2KI" to Pair("2 Ubọọn̄", "OT"),
        "1CH" to Pair("1 Ikpọikpọ", "OT"),
        "2CH" to Pair("2 Ikpọikpọ", "OT"),
        "EZR" to Pair("Ezira", "OT"),
        "NEH" to Pair("Nehemaya", "OT"),
        "EST" to Pair("Ester", "OT"),
        "JOB" to Pair("Job", "OT"),
        "PSA" to Pair("Isẹm", "OT"),
        "PRO" to Pair("Ikeyi", "OT"),
        "ECC" to Pair("Eklesiastes", "OT"),
        "SNG" to Pair("Ọsọ Isẹm", "OT"),
        "ISA" to Pair("Aizaya", "OT"),
        "JER" to Pair("Jeremaya", "OT"),
        "LAM" to Pair("Ọruukọ", "OT"),
        "EZK" to Pair("Ezekiel", "OT"),
        "DAN" to Pair("Daniel", "OT"),
        "HOS" to Pair("Hosea", "OT"),
        "JOL" to Pair("Joel", "OT"),
        "AMO" to Pair("Amọs", "OT"),
        "OBA" to Pair("Obadaya", "OT"),
        "JON" to Pair("Jona", "OT"),
        "MIC" to Pair("Maika", "OT"),
        "NAM" to Pair("Nahum", "OT"),
        "HAB" to Pair("Habakuk", "OT"),
        "ZEP" to Pair("Zefanaya", "OT"),
        "HAG" to Pair("Hagai", "OT"),
        "ZEC" to Pair("Zakaraya", "OT"),
        "MAL" to Pair("Malakai", "OT"),
        "MAT" to Pair("Matyu", "NT"),
        "MRK" to Pair("Mak", "NT"),
        "LUK" to Pair("Luk", "NT"),
        "JHN" to Pair("Jọn", "NT"),
        "ACT" to Pair("Ikpọtụka", "NT"),
        "ROM" to Pair("Rom", "NT"),
        "1CO" to Pair("1 Korint", "NT"),
        "2CO" to Pair("2 Korint", "NT"),
        "GAL" to Pair("Galatia", "NT"),
        "EPH" to Pair("Efesọs", "NT"),
        "PHP" to Pair("Filipai", "NT"),
        "COL" to Pair("Kolosi", "NT"),
        "1TH" to Pair("1 Tesalonika", "NT"),
        "2TH" to Pair("2 Tesalonika", "NT"),
        "1TI" to Pair("1 Timoti", "NT"),
        "2TI" to Pair("2 Timoti", "NT"),
        "TIT" to Pair("Taitọs", "NT"),
        "PHM" to Pair("Filemọn", "NT"),
        "HEB" to Pair("Hibriu", "NT"),
        "JAS" to Pair("Jems", "NT"),
        "1PE" to Pair("1 Pita", "NT"),
        "2PE" to Pair("2 Pita", "NT"),
        "1JN" to Pair("1 Jọn", "NT"),
        "2JN" to Pair("2 Jọn", "NT"),
        "3JN" to Pair("3 Jọn", "NT"),
        "JUD" to Pair("Judas", "NT"),
        "REV" to Pair("Iji-ere", "NT")
    )

    fun parseAssetFolder(
        context: Context,
        folderName: String,
        translationKey: String,
        translationName: String
    ): List<BibleEntity> {
        val result = mutableListOf<BibleEntity>()
        try {
            val files = context.assets.list("raw/data/$folderName") ?: emptyArray()
            for (fileName in files) {
                val decrypted = decryptAsset(context, folderName, fileName)
                if (!decrypted.isNullOrBlank()) {
                    val parsedVerses = parseContent(decrypted, translationKey, translationName)
                    result.addAll(parsedVerses)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun decryptAsset(context: Context, folderName: String, fileName: String): String? {
        return try {
            val stream = context.assets.open("raw/data/$folderName/$fileName")
            val bytes = stream.readBytes()
            stream.close()

            when (folderName) {
                "sab_protestant" -> c.initMobile(bytes, bytes.size)
                "sab_catholic" -> k.initMobile(bytes, bytes.size)
                else -> {
                    // Try catholic decrypt first, fallback to protestant or raw string
                    try {
                        k.initMobile(bytes, bytes.size)
                    } catch (e: Throwable) {
                        try {
                            c.initMobile(bytes, bytes.size)
                        } catch (e2: Throwable) {
                            // Raw bytes
                        }
                    }
                }
            }
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseContent(
        content: String,
        translationKey: String,
        translationName: String
    ): List<BibleEntity> {
        val verses = mutableListOf<BibleEntity>()
        try {
            var bookCode = ""
            var chapter = 1

            // USFM \id BOOK
            val idMatch = Pattern.compile("\\\\id\\s+([A-Z0-9]{3})").matcher(content)
            if (idMatch.find()) {
                bookCode = idMatch.group(1) ?: ""
            }

            // USFM \c N
            val cMatch = Pattern.compile("\\\\c\\s+(\\d+)").matcher(content)
            if (cMatch.find()) {
                chapter = cMatch.group(1)?.toIntOrNull() ?: 1
            }

            // HTML metadata fallback
            if (bookCode.isBlank()) {
                val htmlMetaMatch = Pattern.compile("id=\"([A-Z0-9]{3})[_-](\\d+)\"").matcher(content)
                if (htmlMetaMatch.find()) {
                    bookCode = htmlMetaMatch.group(1) ?: ""
                    chapter = htmlMetaMatch.group(2)?.toIntOrNull() ?: 1
                }
            }

            if (bookCode.isBlank()) return emptyList()

            val bookPair = BOOK_NAMES[bookCode]
            val bookName = bookPair?.first ?: bookCode
            val testament = bookPair?.second ?: "NT"

            // USFM verse parser: \v N verse text
            val usfmVerseMatcher = Pattern.compile("\\\\v\\s+(\\d+)\\s+([^\\\\<]+)").matcher(content)
            while (usfmVerseMatcher.find()) {
                val vNum = usfmVerseMatcher.group(1)?.toIntOrNull() ?: continue
                val vText = usfmVerseMatcher.group(2)?.trim() ?: continue
                val cleaned = cleanVerseText(vText)
                if (cleaned.isNotBlank()) {
                    verses.add(
                        BibleEntity(
                            translationKey = translationKey,
                            translationName = translationName,
                            bookCode = bookCode,
                            bookName = bookName,
                            testament = testament,
                            chapter = chapter,
                            verse = vNum,
                            text = cleaned
                        )
                    )
                }
            }

            // HTML verse parser: class="v">N</span> text
            if (verses.isEmpty()) {
                val htmlVerseMatcher = Pattern.compile("class=\"v(?:n)?\">(\\d+)</span>\\s*([^<]+)").matcher(content)
                while (htmlVerseMatcher.find()) {
                    val vNum = htmlVerseMatcher.group(1)?.toIntOrNull() ?: continue
                    val vText = htmlVerseMatcher.group(2)?.trim() ?: continue
                    val cleaned = cleanVerseText(vText)
                    if (cleaned.isNotBlank()) {
                        verses.add(
                            BibleEntity(
                                translationKey = translationKey,
                                translationName = translationName,
                                bookCode = bookCode,
                                bookName = bookName,
                                testament = testament,
                                chapter = chapter,
                                verse = vNum,
                                text = cleaned
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return verses
    }

    private fun cleanVerseText(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("<[^>]*>".toRegex(), "") // Strip any residual html tags
            .replace("\\\\f.*?\\\\f\\*".toRegex(), "") // Strip footnotes
            .replace("\\\\x.*?\\\\x\\*".toRegex(), "") // Strip cross references
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
