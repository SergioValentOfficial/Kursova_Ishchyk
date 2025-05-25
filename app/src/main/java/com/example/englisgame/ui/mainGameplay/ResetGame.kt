package com.example.englisgame.ui.mainGameplay

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import com.example.englisgame.R
import com.example.englishgame.database.DatabaseHelper

class ResetGame(
    private val activity: AppCompatActivity,
    private val textTimer: TextView,
    private val startGameButton: Button,
    private val healthText: TextView,
    private val textCurrentLetter: TextView
) {
    var isGameOver: Boolean = false
    val health = Health(textTimer, healthText, this)

    companion object {
        private var instance: ResetGame? = null

        fun getInstance(
            activity: AppCompatActivity,
            textTimer: TextView,
            startGameButton: Button,
            healthText: TextView,
            textCurrentLetter: TextView
        ): ResetGame {
            if (instance == null) {
                instance = ResetGame(activity, textTimer, startGameButton, healthText, textCurrentLetter)
            }
            return instance!!
        }
    }

    fun endGame() {
        isGameOver = true
        textTimer.text = "Життя закінчились!"

        // замість внутрішнього рахунку — беремо з MainGameplay
        if (activity is MainGameplay) {
            textCurrentLetter.text = "Слів: ${activity.playerWordCount}"
        }


        startGameButton.text = "Спробувати\nзнову"

        startGameButton.setOnClickListener {
            restartGame()
        }
    }

    private fun restartGame() {
        isGameOver = false
        health.resetLives()

        DatabaseHelper(activity).resetUsedFlags()

        textTimer.text = ""
        healthText.text = "HP: 3"
        textCurrentLetter.text = ""
        startGameButton.text = "Почати гру"

        if (activity is MainGameplay) {
            activity.resetGameUI()

            startGameButton.setOnClickListener {
                val inputWord = activity.findViewById<EditText>(R.id.editTextGameplay).text.toString().trim()
                activity.setInputWordAndCheck(inputWord)
            }
        }
    }
}
