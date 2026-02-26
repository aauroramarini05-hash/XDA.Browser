package com.xdust.auryxbrowser

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)

        val goButton: ImageView = findViewById(R.id.goButton)

        webView.webViewClient = WebViewClient()
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        goButton.setOnClickListener {
            loadUrl(urlBar.text.toString())
        }

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

    private fun loadUrl(url: String) {
        var finalUrl = url
        if (!url.startsWith("http")) {
            finalUrl = "https://www.google.com/search?q=$url"
        }
        webView.loadUrl(finalUrl)
        urlBar.setText(finalUrl)
    }

    private fun handleIntent
