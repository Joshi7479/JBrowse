package com.example.jbrowsev1

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    private var standart_suchmaschine_url = "https://duckduckgo.com"

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }


        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = android.webkit.WebViewClient()

        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadInput(urlBar.text.toString())
                true
            } else false
        }

        val buttonBack = findViewById<Button>(R.id.btnBack)
        val buttonReload = findViewById<Button>(R.id.btnReload)
        val buttonHome = findViewById<Button>(R.id.home)
        val buttonForward = findViewById<Button>(R.id.btnForward)
        val buttonSettings = findViewById<Button>(R.id.settings)

        val alleButtons = listOf(buttonBack, buttonReload, buttonHome, buttonForward, buttonSettings)

        // Klick-Listener zuweisen
        buttonBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        buttonReload.setOnClickListener { webView.reload() }
        buttonHome.setOnClickListener { webView.loadUrl(standart_suchmaschine_url) }
        buttonForward.setOnClickListener { webView.goForward() }
        buttonSettings.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
            }
        })

        lifecycleScope.launch {
            try {
                val preferences = dataStore.data.first()

                val gespeicherteUrl = preferences[MEIN_STRING_KEY]
                if (!gespeicherteUrl.isNullOrEmpty()) {
                    standart_suchmaschine_url = gespeicherteUrl
                }


                val gespeicherteFarbe = preferences[USER_COLOR_KEY]
                if (!gespeicherteFarbe.isNullOrEmpty()) {
                    val parsedColor = Color.parseColor(gespeicherteFarbe)

                    alleButtons.forEach { button ->
                        button.backgroundTintList = ColorStateList.valueOf(parsedColor)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Using standart theme!", Toast.LENGTH_SHORT).show()
            } finally {
                webView.loadUrl(standart_suchmaschine_url)
            }
        }
    }

    private fun loadInput(input: String) {
        val text = input.trim()
        val url = when {
            text.startsWith("http://") || text.startsWith("https://") -> text
            text.contains(".") && !text.contains(" ") -> "https://$text"
            else -> standart_suchmaschine_url + "/search?q=" + Uri.encode(text)
        }
        webView.loadUrl(url)
    }
}
