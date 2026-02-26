package com.xdust.auryxbrowser

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        drawerLayout = findViewById(R.id.drawerLayout)

        val goButton: ImageView = findViewById(R.id.goButton)
        val menuButton: ImageView = findViewById(R.id.menuButton)

        webView.webViewClient = WebViewClient()

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        // Bottone ricerca
        goButton.setOnClickListener {
            loadUrl(urlBar.text.toString())
        }

        // Apri drawer
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(android.view.Gravity.START)
        }

        // Bottoni Home
        findViewById<LinearLayout>(R.id.btnGoogle).setOnClickListener {
            loadUrl("https://www.google.com")
        }

        findViewById<LinearLayout>(R.id.btnYoutube).setOnClickListener {
            loadUrl("https://www.youtube.com")
        }

        findViewById<LinearLayout>(R.id.btnWikipedia).setOnClickListener {
            loadUrl("https://www.wikipedia.org")
        }

        findViewById<LinearLayout>(R.id.btnAuryx).setOnClickListener {
            loadUrl("https://github.com")
        }

        handleIntent()
    }

    private fun loadUrl(input: String) {
        var url = input.trim()

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://www.google.com/search?q=$url"
        }

        webView.loadUrl(url)
        urlBar.setText(url)
    }

    private fun handleIntent() {
        val data = intent?.data
        if (data != null) {
            webView.loadUrl(data.toString())
        }
    }
}
