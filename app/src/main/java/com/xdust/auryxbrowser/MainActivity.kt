package com.xdust.auryxbrowser

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // IDs (devono esistere nel layout: nel tuo ci sono)
        drawerLayout = findViewById(R.id.drawerLayout)
        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)

        val goButton: ImageView = findViewById(R.id.goButton)
        val menuButton: ImageView = findViewById(R.id.menuButton)

        // WebView safe setup
        webView.webViewClient = WebViewClient()
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        // Toolbar buttons
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        goButton.setOnClickListener {
            val input = urlBar.text?.toString().orEmpty()
            loadInput(input)
        }

        // Home buttons
        findViewById<LinearLayout>(R.id.btnGoogle).setOnClickListener { webView.loadUrl("https://www.google.com") }
        findViewById<LinearLayout>(R.id.btnYoutube).setOnClickListener { webView.loadUrl("https://www.youtube.com") }
        findViewById<LinearLayout>(R.id.btnWikipedia).setOnClickListener { webView.loadUrl("https://www.wikipedia.org") }
        findViewById<LinearLayout>(R.id.btnAuryx).setOnClickListener { webView.loadUrl("https://auryxtrends.it") }

        // 🔥 Gestione link esterni (quando vieni aperto come browser)
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        if (intent != null) handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(i: android.content.Intent) {
        try {
            val data: Uri? = i.data
            if (data != null) {
                // Apre direttamente il link richiesto dal sistema
                val url = data.toString()
                urlBar.setText(url)
                webView.loadUrl(url)
            } else {
                // Nessun link: resta in home (non carichiamo nulla di strano)
                // opzionale: carica una pagina neutra
                // webView.loadUrl("https://www.google.com")
            }
        } catch (_: Throwable) {
            // non crashare mai
        }
    }

    private fun loadInput(inputRaw: String) {
        val input = inputRaw.trim()
        if (input.isEmpty()) return

        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> {
                val q = URLEncoder.encode(input, "UTF-8")
                "https://www.google.com/search?q=$q"
            }
        }

        try {
            urlBar.setText(url)
            webView.loadUrl(url)
        } catch (_: Throwable) {
        }
    }
}
