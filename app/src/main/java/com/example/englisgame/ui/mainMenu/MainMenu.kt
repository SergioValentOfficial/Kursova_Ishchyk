package com.example.englisgame.ui.mainMenu

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.example.englisgame.R
import com.example.englisgame.ui.mainGameplay.CustomKeyboard
import com.example.englisgame.ui.mainGameplay.MainGameplay
import com.example.englisgame.ui.mainGameplay.mainMulti

class MainMenu : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        var indexArray = 0

        // Відновлюємо статус-бар
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val editTextNickName: EditText = findViewById(R.id.editTextNickName)
        val startGameButton: Button = findViewById(R.id.startGameButton)
        val startMultiButton: Button = findViewById(R.id.startMultiButton)
        val changeColorButton: Button = findViewById(R.id.changeTextColor)
        val keyboardContainer: LinearLayout = findViewById(R.id.keyboard_container)

        val colors = resources.obtainTypedArray(R.array.buttonColor)
        val colorList = IntArray(colors.length()) { index -> colors.getColor(index, 0) }
        colors.recycle()

        // задаємо дефолтні значення кольорів
        startMultiButton.backgroundTintList = ColorStateList.valueOf(colorList[indexArray])
        startGameButton.backgroundTintList = ColorStateList.valueOf(colorList[indexArray])
        changeColorButton.backgroundTintList = ColorStateList.valueOf(colorList[indexArray])

        // зміна кольорів
        changeColorButton.setOnClickListener {
            indexArray = (indexArray + 1) % colorList.size
            val newColor = ColorStateList.valueOf(colorList[indexArray])
            startMultiButton.backgroundTintList = newColor
            startGameButton.backgroundTintList = newColor
            changeColorButton.backgroundTintList = newColor
            keyboardContainer.backgroundTintList = newColor
        }

        // ========== КАСТОМНА КЛАВІАТУРА ==========
        val customKeyboard = CustomKeyboard(this, editTextNickName)
        val gridLayout = GridLayout(this).apply {
            rowCount = 4
            columnCount = 7
        }
        keyboardContainer.addView(gridLayout)
        customKeyboard.createKeyboard(gridLayout)
        keyboardContainer.visibility = LinearLayout.GONE

        editTextNickName.setOnTouchListener { _, _ ->
            editTextNickName.requestFocus()
            editTextNickName.isCursorVisible = true
            keyboardContainer.visibility = LinearLayout.VISIBLE
            true
        }
        editTextNickName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) keyboardContainer.visibility = LinearLayout.GONE
        }

        // Перехід до гри
        startGameButton.setOnClickListener {
            val nickname = editTextNickName.text.toString()
            val intent = Intent(this, MainGameplay::class.java)
            intent.putExtra("nickname_key", nickname)
            intent.putExtra("color_key", indexArray)
            startActivity(intent)
        }

        // Перехід до мультиплеєра
        startMultiButton.setOnClickListener {
            val intent = Intent(this, mainMulti::class.java)
            startActivity(intent)
        }
    }
}
