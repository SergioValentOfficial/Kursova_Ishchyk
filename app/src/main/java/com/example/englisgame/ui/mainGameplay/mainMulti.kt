package com.example.englisgame.ui.mainGameplay

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.englisgame.R

class mainMulti : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_multi)

        // Отримуємо доступ до елементів XML
        val gameplayText: TextView = findViewById(R.id.gameplayText)
        val startGameButton: Button = findViewById(R.id.startGameButton)

        // Логіка кнопки
        startGameButton.setOnClickListener {
            gameplayText.text = "Гра розпочалася!"
        }
    }
}