package com.example.englisgame.ui.mainGameplay

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.englisgame.R
import com.example.englishgame.database.DatabaseHelper
import com.example.englisgame.ui.mainGameplay.UATranslate

class MainGameplay : AppCompatActivity() {
    private var currentWord = " "
    private var inputWord = " "
    private var currentBufferWord = " "

    private lateinit var textViewNickName: TextView
    private lateinit var textGameplay: TextView
    private lateinit var textCurrentWord: TextView
    private lateinit var textCurrentLetter: TextView
    lateinit var startGameButton: Button
    private lateinit var editTextGameplay: EditText
    private lateinit var textResult: TextView
    private lateinit var textTimer: TextView
    private lateinit var btnShowTranslate: Button
    private lateinit var tvTranslation: TextView
    private lateinit var keyboardContainer: LinearLayout

    private var isPlayerTurn = true
    private var myTimer: MyTimer? = null
    private var lastBotWord: String? = null

    var playerWordCount = 0
    var playerUsedWordsCount = 0

    private lateinit var uaTranslator: UATranslate

    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_gameplay)

        val colorButton = intent.getIntExtra("color_key", 0)
        val colors = resources.obtainTypedArray(R.array.buttonColor)
        val colorList = IntArray(colors.length()) { index -> colors.getColor(index, 0) }
        colors.recycle()

        textViewNickName = findViewById(R.id.textViewNickName)
        textGameplay = findViewById(R.id.gameplayText)
        textCurrentWord = findViewById(R.id.currentWord)
        textCurrentLetter = findViewById(R.id.currentLetter)
        startGameButton = findViewById(R.id.startGameButton)
        editTextGameplay = findViewById(R.id.editTextGameplay)
        textResult = findViewById(R.id.Result)
        textTimer = findViewById(R.id.textTimer)
        btnShowTranslate = findViewById(R.id.btnShowTranslate)
        tvTranslation = findViewById(R.id.tvTranslation)
        keyboardContainer = findViewById(R.id.keyboard_container)

        uaTranslator = UATranslate(this, lifecycleScope)

        btnShowTranslate.visibility = View.GONE
        tvTranslation.visibility = View.GONE
        btnShowTranslate.setOnClickListener {
            lastBotWord?.let { word ->
                uaTranslator.translate(word) { ua ->
                    tvTranslation.text = ua ?: "Переклад не знайдено"
                    tvTranslation.visibility = View.VISIBLE
                }
            }
        }

        val customKeyboard = CustomKeyboard(this, editTextGameplay)
        val gridLayout = GridLayout(this).apply { rowCount = 4; columnCount = 7 }
        keyboardContainer.apply {
            addView(gridLayout)
            visibility = View.GONE
        }
        keyboardContainer.backgroundTintList = ColorStateList.valueOf(colorList[colorButton])
        customKeyboard.createKeyboard(gridLayout)

        editTextGameplay.setOnTouchListener { _, _ ->
            editTextGameplay.requestFocus()
            editTextGameplay.isCursorVisible = true
            keyboardContainer.visibility = View.VISIBLE
            true
        }
        editTextGameplay.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) keyboardContainer.visibility = View.GONE
        }

        val nickname = intent.getStringExtra("nickname_key") ?: "Гравець"
        startGameButton.backgroundTintList = ColorStateList.valueOf(colorList[colorButton])
        textViewNickName.text = nickname

        DatabaseHelper(this).checkAndCopyDatabase()

        startGameButton.setOnClickListener {
            inputWord = editTextGameplay.text.toString().trim()
            checkedWord()
        }
    }

    private fun checkedWord() {
        if (inputWord.isEmpty()) {
            textResult.text = "Введіть слово!"
            return
        }

        if (inputWord.first().toString() != currentWord.last().toString() && currentWord != " ") {
            textResult.text = "Слово починається не з тої букви!"
            return
        }

        val dbHelper = DatabaseHelper(this)
        when (dbHelper.wordCheck(inputWord)) {
            "used" -> textResult.text = "Це слово вже використовувалось!"
            "ok" -> {

                if (playerWordCount == 0) {
                    myTimer = MyTimer(this, textTimer).also { it.start() }
                }

                playerWordCount++


                startGameButton.text = "Відправити\nслово"
                textResult.text = "Слово прийнято!"
                textGameplay.text = "Гра триває!"
                textCurrentWord.text = "Поточне слово: $inputWord"
                editTextGameplay.text.clear()
                currentWord = inputWord
                textCurrentLetter.text = currentWord.last().toString()

                btnShowTranslate.visibility = View.GONE
                tvTranslation.visibility = View.GONE
                lastBotWord = null

                myTimer?.cancel()
                isPlayerTurn = false
                textTimer.postDelayed({ botTurn(inputWord.last()) }, 2000)
            }
            "!exist" -> textResult.text = "Цього слова немає в базі!"
            "error" -> textResult.text = "Помилка: база не знайдена!"
        }

        editTextGameplay.requestFocus()
        editTextGameplay.isCursorVisible = true
    }

    private fun botTurn(lastLetter: Char) {
        val botResponder = BotResponder(this)
        val botWord = botResponder.getWordStartingWith(lastLetter)

        if (botWord == null) {
            textTimer.text = "Бот програв!"
            textCurrentLetter.text = "Ваші слова: $playerWordCount"

            btnShowTranslate.visibility = View.GONE
            tvTranslation.visibility = View.GONE
            lastBotWord = null
            return
        }

        textResult.text = "Бот відповів: $botWord"
        textGameplay.text = "Гра триває!"
        textCurrentWord.text = "Поточне слово: $botWord"
        textCurrentLetter.text = botWord.last().toString()
        lastBotWord = botWord
        btnShowTranslate.visibility = View.VISIBLE
        tvTranslation.visibility = View.GONE
        currentWord = botWord
        myTimer?.cancel()
        myTimer = MyTimer(this, textTimer).also { it.start() }

        isPlayerTurn = true
        editTextGameplay.requestFocus()
        editTextGameplay.isCursorVisible = true
    }

    fun resetGameUI() {
        textResult.text = ""
        textGameplay.text = "Готові до гри!"
        textCurrentWord.text = ""
        textCurrentLetter.text = ""
        editTextGameplay.text.clear()
        btnShowTranslate.visibility = View.GONE
        tvTranslation.visibility = View.GONE
        currentWord = " "
        inputWord = " "
        isPlayerTurn = true
        playerWordCount = 0
        myTimer?.cancel()
    }
    fun setInputWordAndCheck(word: String) {
        inputWord = word
        checkedWord()
    }

}
