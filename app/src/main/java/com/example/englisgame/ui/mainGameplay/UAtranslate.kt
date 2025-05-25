package com.example.englisgame.ui.mainGameplay   // ← такий самий пакет, як у MainGameplay

import android.content.Context
import com.example.englishgame.database.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Легковажний фасад для DatabaseHelper.UAtranslate(...):
 *   • виконує запит у фоновому потоці;
 *   • повертає переклад через callback на головному потоці.
 *
 * Використання:
 *     uaTranslator.translate(botWord) { ua ->
 *         // показати ua (або повідомлення, що null)
 *     }
 */
class UATranslate(
    private val context: Context,
    private val scope: CoroutineScope   // наприклад, lifecycleScope
) {

    // Один helper на увесь життєвий цикл UATranslate
    private val dbHelper by lazy { DatabaseHelper(context) }

    /**
     * @param word      англійське слово
     * @param onResult  повертає переклад або null (якщо нічого не знайдено)
     */
    fun translate(word: String, onResult: (String?) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val ua = dbHelper.UAtranslate(word.trim())

            // Переходимо на головний потік і віддаємо результат
            withContext(Dispatchers.Main) { onResult(ua) }
        }
    }
}
