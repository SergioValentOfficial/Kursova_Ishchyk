package com.example.englisgame.ui.mainGameplay

import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.englisgame.R

class MyTimer(
    private val activity: MainGameplay,  // ⬅️ Додаємо активність
    private val textView: TextView
) {
    private var timer: CountDownTimer? = null

    fun start() {
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                textView.text = "Залишилось: $secondsLeft сек"
            }

            override fun onFinish() {
                val resetGame = ResetGame.getInstance(
                    activity,
                    activity.findViewById(R.id.textTimer),
                    activity.findViewById(R.id.startGameButton),
                    activity.findViewById(R.id.healthText),
                    activity.findViewById(R.id.currentLetter)
                )

                resetGame.health.decreaseLife()

                if (resetGame.health.isGameOver()) {
                    textView.text = "Життя закінчились!"
                    resetGame.endGame()
                } else {
                    textView.text = "Час вийшов!"
                    start()
                }
            }
        }.start()
    }

    fun cancel() {
        timer?.cancel()
    }
}
