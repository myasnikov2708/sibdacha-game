package ru.sibdacha.game

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {

    private var state: GameState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loaded = Save.load(this) ?: GameState().also {
            it.say("Первый день. Купите саженцы в лавке и вскопайте грядку.")
        }
        state = loaded

        setContent {
            MaterialTheme {
                val s = remember { loaded }

                App(s) { url ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        // нет браузера — ничего не делаем
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        state?.let { Save.save(this, it) }
    }

    override fun onStop() {
        super.onStop()
        state?.let { Save.save(this, it) }
    }
}
