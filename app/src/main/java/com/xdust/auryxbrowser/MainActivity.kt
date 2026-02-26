package com.xdust.auryxbrowser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var drawerLayout: DrawerLayout

    private var desktopMode = false
    private var incognitoMode = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        drawerLayout = findViewById(R.id.drawerLayout)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Pulsanti homepage
        findViewById<Button>(R.id.btnGoogle).setOnClickListener {
            loadUrl("https://www.google.com")
        }

        findViewById<Button>(R.id.btnYouTube).setOnClickListener {
            loadUrl("https://www.youtube.com")
        }

        findViewById<Button>(R.id.btnWikipedia).setOnClickListener {
            loadUrl("https://www.wikipedia.org")
        }

        findViewById<Button>(R.id.btnAuryx).setOnClickListener {
            loadUrl("https://auryxtrends.it")
        }

        // Desktop Mode
        findViewById<Button>(R.id.btnDesktopMode).setOnClickListener {
            desktopMode = !desktopMode
            webView.settings.useWideViewPort = desktopMode
            webView.settings.loadWithOverviewMode = desktopMode
            webView.settings.userAgentString =
                if (desktopMode)
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36"
                else
                    WebSettings.getDefaultUserAgent(this)
        }

        // Incognito Mode
        findViewById<Button>(R.id.btnIncognito).setOnClickListener {
            incognitoMode = !incognitoMode

            if (incognitoMode) {
                webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webView.clearHistory()
                webView.clearCache(true)
                CookieManager.getInstance().removeAllCookies(null)
            } else {
                webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
            }
        }

        // History (semplice: torna indietro)
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        // Barra indirizzi
        addressBar.setOnEditorActionListener { _, _, _ ->
            var url = addressBar.text.toString()

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://www.google.com/search?q=$url"
            }

            loadUrl(url)
            true
        }

        // Pagina iniziale
        webView.loadUrl("https://www.google.com")
    }

    private fun loadUrl(url: String) {
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
