package com.example.englisgame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.englisgame.ui.mainMenu.MainMenu



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Запускаємо MainMenu при запуску MainActivity
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        
        // Закриваємо MainActivity, щоб не повертатися наза
        finish()
    }
}
