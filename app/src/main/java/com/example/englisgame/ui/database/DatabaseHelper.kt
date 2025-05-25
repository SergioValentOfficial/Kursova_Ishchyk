package com.example.englishgame.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.ContentValues

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Slow"
        private const val DATABASE_VERSION = 1
        private lateinit var DATABASE_PATH: String
    }

    init {
        DATABASE_PATH = context.applicationInfo.dataDir + "/databases/"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Не потрібно, бо база копіюється з assets
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Не використовується
    }

    fun checkAndCopyDatabase() {
        val dbFile = File(DATABASE_PATH + DATABASE_NAME)
        if (dbFile.exists()) {
            dbFile.delete()
        }
        copyDatabase()
    }

    private fun copyDatabase() {
        try {
            context.assets.open(DATABASE_NAME).use { inputStream ->
                File(DATABASE_PATH).mkdirs()
                val outFileName = DATABASE_PATH + DATABASE_NAME
                FileOutputStream(outFileName).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun openDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE)
    }

    fun getWordByFirstLetter(letter: Char): String? {
        val db = openDatabase()
        var resultWord: String? = null
        val tableName = letter.uppercaseChar().toString()

        try {
            // Перевірка, чи існує така таблиця
            val cursorTable = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf(tableName)
            )

            if (cursorTable.moveToFirst()) {
                val cursorColumns = db.rawQuery("PRAGMA table_info($tableName)", null)
                val columnNames = mutableListOf<String>()
                while (cursorColumns.moveToNext()) {
                    columnNames.add(cursorColumns.getString(1))
                }
                cursorColumns.close()

                val lengthColumns = columnNames.filter { it != "used" }

                for (column in lengthColumns) {
                    val query = """
                        SELECT rowid, "$column" FROM "$tableName"
                        WHERE used = 0 AND "$column" IS NOT NULL
                        ORDER BY RANDOM()
                    """.trimIndent()

                    val cursor = db.rawQuery(query, null)
                    if (cursor.moveToFirst()) {
                        val rowId = cursor.getLong(0)
                        val word = cursor.getString(1)

                        resultWord = word

                        val values = ContentValues().apply {
                            put("used", 1)
                        }
                        db.update(tableName, values, "rowid = ?", arrayOf(rowId.toString()))
                        cursor.close()
                        break
                    }
                    cursor.close()
                }
            }
            cursorTable.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return resultWord
    }
    fun UAtranslate(word: String): String? {
        val db = openDatabase()
        var translation: String? = null

        if (word.isEmpty()) return null

        val tableName = word[0].uppercaseChar().toString()
        val lengthColumn = word.length.toString()

        try {
            val cursorTable = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf(tableName)
            )

            if (cursorTable.moveToFirst()) {
                val cursorColumns = db.rawQuery("PRAGMA table_info($tableName)", null)
                val columnNames = mutableListOf<String>()
                while (cursorColumns.moveToNext()) {
                    columnNames.add(cursorColumns.getString(1))
                }
                cursorColumns.close()

                if (lengthColumn in columnNames && "UA" in columnNames) {
                    val query = """
                    SELECT "UA" FROM "$tableName"
                    WHERE "$lengthColumn" = ? COLLATE NOCASE
                    LIMIT 1
                """.trimIndent()

                    val cursor = db.rawQuery(query, arrayOf(word))
                    if (cursor.moveToFirst()) {
                        translation = cursor.getString(0)
                    }
                    cursor.close()
                }
            }
            cursorTable.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return translation
    }
    fun wordCheck(word: String): String {
        if (word.isBlank()) return "!exist"

        val db = openDatabase()
        var result = "!exist"

        val tableName = word[0].uppercaseChar().toString()
        val lengthCol = word.length.toString()

        try {
            // 1. Переконуємося, що таблиця має стовпець потрібної довжини
            val colCursor = db.rawQuery(
                "SELECT 1 FROM pragma_table_info(\"$tableName\") WHERE name = ?",
                arrayOf(lengthCol)
            )
            if (!colCursor.moveToFirst()) { colCursor.close(); return result }
            colCursor.close()

            // 2. Шукаємо слово (без урахування регістру)
            val cur = db.rawQuery(
                """SELECT rowid, used
               FROM "$tableName"
               WHERE "$lengthCol" = ? COLLATE NOCASE
               LIMIT 1""",
                arrayOf(word)
            )

            if (cur.moveToFirst()) {
                val rowId = cur.getLong(0)
                val used  = cur.getInt(1)

                result = if (used == 0) {
                    val cv = ContentValues().apply { put("used", 1) }
                    db.update(tableName, cv, "rowid = ?", arrayOf(rowId.toString()))
                    "ok"
                } else "used"
            }
            cur.close()
        } catch (e: Exception) {
            e.printStackTrace()
            result = "error"
        } finally {
            db.close()
        }
        return result
    }

    fun resetUsedFlags() {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_%'",
            null
        )

        while (cursor.moveToNext()) {
            val tableName = cursor.getString(0)
            try {
                db.execSQL("UPDATE \"$tableName\" SET used = 0")
            } catch (e: Exception) {
                e.printStackTrace() // можливо таблиця не має поля used — ігноруємо
            }
        }

        cursor.close()
        db.close()
    }



}
