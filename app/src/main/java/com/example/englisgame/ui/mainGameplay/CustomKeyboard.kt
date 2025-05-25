package com.example.englisgame.ui.mainGameplay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.setPadding
import kotlin.math.abs
import kotlin.math.max

class CustomKeyboard(
    private val context: Context,
    private val inputField: EditText
) {

    /** 3 ряди основної QWERTY-розкладки */
    private val keyboardRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("z","x","c","v","b","n","m")
    )

    /** Технічні змінні для auto-repeat */
    private val h = Handler(Looper.getMainLooper())
    private var isHeld = false

    /**
     * Створюємо клавіатуру усередині **GridLayout**.
     *
     * • Усі рядки — однакової висоти.
     * • `columnCount` — найбільша кількість кнопок у рядку (=10).
     * • Короткі ряди центруються заповнювачами-«пустишками».
     */
    @SuppressLint("ClickableViewAccessibility")
    fun createKeyboard(grid: GridLayout) {
        grid.removeAllViews()

        // Cкільки колонок потрібно, щоб влазили всі букви
        val maxCols = keyboardRows.maxOf { it.size }
        grid.columnCount = maxCols
        grid.rowCount    = keyboardRows.size + 1          // +1 ряд – кнопка ⇆

        var rowIndex = 0
        for (row in keyboardRows) {
            val gap = maxCols - row.size                         // скільки «вільних» комірок
            val leftGap  = gap / 2
            val rightGap = gap - leftGap

            // Ліві заповнювачі
            repeat(leftGap)  { grid.addView(makeSpacer()) }

            // Самі кнопки-букви
            row.forEach { key ->
                grid.addView(makeKeyButton(key) { append(key) })
            }

            // Праві заповнювачі
            repeat(rightGap) { grid.addView(makeSpacer()) }

            rowIndex++
        }

        // =========================== BACKSPACE ===============================
        val backspace = makeKeyButton("⌫") { backspace() }

        val lpDel = GridLayout.LayoutParams().apply {
            /* ставимо у середній (1-й) ряд, останню колонку */
            rowSpec    = GridLayout.spec(1)                // 1-й ряд (рахуємо з нуля)
            columnSpec = GridLayout.spec(maxCols - 1)      // остання колонка
            width  = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setGravity(android.view.Gravity.FILL)          // заповнює клітинку
        }
        backspace.layoutParams = lpDel
        grid.addView(backspace)

        // Ряд окремої великої кнопки «⇆»
        val cursorBtn = makeKeyButton("⇆") {}
        cursorBtn.setOnTouchListener { _, e -> handleCursorSwipe(e) }
        val lpCur = GridLayout.LayoutParams().apply {
            rowSpec    = GridLayout.spec(grid.rowCount - 1)     // останній ряд
            columnSpec = GridLayout.spec(0, maxCols)            // span на весь ряд
            width  = GridLayout.LayoutParams.MATCH_PARENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(0, 8, 0, 0)
        }
        cursorBtn.layoutParams = lpCur
        grid.addView(cursorBtn)
    }

    /** --- приватні допоміжні методи ---------------------------------------------------------- */

    private fun makeSpacer(): View = View(context).apply {
        layoutParams = GridLayout.LayoutParams().apply {
            width = 0; height = 0
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun makeKeyButton(
        text: String,
        onClick: () -> Unit
    ): Button = Button(context).apply {
        this.text = text
        setTextColor(Color.BLACK)
        setPadding(2)
        layoutParams = GridLayout.LayoutParams().apply {
            width  = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)   // вага = 1
        }

        setOnClickListener { onClick() }

        setOnLongClickListener {
            isHeld = true
            repeatStart(onClick)
            true
        }
        setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) {
                isHeld = false
                h.removeCallbacksAndMessages(null)
            }
            false
        }
    }

    /** повторяємо дію кожні 100 мс, поки кнопка тримається */
    private fun repeatStart(action: () -> Unit) {
        h.post(object : Runnable {
            override fun run() {
                if (isHeld) {
                    action()
                    h.postDelayed(this, 100)
                }
            }
        })
    }

    /** вставити символ у поточну позицію курсора */
    private fun append(s: String) {
        val start = inputField.selectionStart
        val end   = inputField.selectionEnd
        val txt   = inputField.text
        txt.replace(start, end, s)
        inputField.setSelection(start + s.length)
    }

    /** backspace – видалити символ ліворуч від курсора */
    private fun backspace() {
        val pos = inputField.selectionStart
        if (pos > 0) {
            inputField.text.delete(pos - 1, pos)
        }
    }

    /** --- swipe-керування курсором по великій кнопці ⇆ --------------------------- */

    private var startX = 0f
    private fun handleCursorSwipe(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> startX = e.x
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - startX
                if (abs(dx) > 50) {
                    if (dx > 0) moveRight() else moveLeft()
                    startX = e.x
                }
            }
        }
        return true
    }

    private fun moveLeft()  {
        val p = max(0, inputField.selectionStart - 1)
        inputField.setSelection(p)
    }
    private fun moveRight() {
        val p = inputField.selectionStart + 1
        if (p <= inputField.length()) inputField.setSelection(p)
    }
}
