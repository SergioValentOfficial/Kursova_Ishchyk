package com.example.englisgame.ui.mainGameplay

import android.content.Context
import com.example.englishgame.database.DatabaseHelper

class BotResponder(private val context: Context) {

    fun getWordStartingWith(letter: Char): String? {
        val dbHelper = DatabaseHelper(context)
        return dbHelper.getWordByFirstLetter(letter)
    }
}
