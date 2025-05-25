package com.example.englisgame.ui.mainGameplay

import android.widget.TextView

class Health(
    private val textView: TextView,
    private val healthText: TextView, // Відображення кількості життів
    private val resetGame: ResetGame
) {
    private var lives: Int = 3

    fun decreaseLife() {
        if (lives > 0) {
            lives--
            updateLivesDisplay()
        }

        if (isGameOver()) {
            resetGame.endGame()
        }
    }

    fun isGameOver(): Boolean {
        return lives <= 0
    }

    private fun updateLivesDisplay() {
        textView.text = if (isGameOver()) {
            "Життя закінчились!"
        } else {
            "Залишилось життів: $lives"
        }

        // Текстове відображення замість емодзі
        healthText.text = "lives: $lives"
    }

    fun resetLives() {
        lives = 3
        updateLivesDisplay()
    }
}
