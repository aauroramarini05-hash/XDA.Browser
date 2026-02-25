package com.xdust.auryxbrowser

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.DownloadListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val tabs = mutableListOf<WebView>()
    private var currentTabIndex = 0

    private lateinit var container: FrameLayout
    private lateinit var edUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.webContainer)
        edUrl = findViewById(R.id.edUrl)

        val btnGo = findViewById<Button>(R.id.btnGo)
        val btnNewTab = findViewById<Button>(R.id.btnNewTab)
        val btnFavorite = findViewById<Button>(R.id.btnFavorite)

        btnGo.setOnClickListener {
            loadInput(edUrl.text.toString())
        }

        btnNewTab.setOnClickListener {
            createNewTab("https://www.google.com")
        }

        btnFavorite.setOnClickListener {
            saveFavorite(getCurrentWebView().url ?: "")
            Toast.makeText(this, "Saved to favorites", Toast.LENGTH_SHORT).show()
        }

        createNewTab("https://www.google.com")
    }

    private fun createNewTab(url: String) {
        val webView = WebView(this)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        webView.setDownloadListener { url, _, _, _, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }

        webView.loadUrl(url)

        tabs.add(webView)
        currentTabIndex = tabs.size - 1

        container.removeAllViews()
        container.addView(webView)
    }

    private fun getCurrentWebView(): WebView {
        return tabs[currentTabIndex]
    }

    private fun loadInput(input: String) {
        val webView = getCurrentWebView()
        val text = input.trim()

        if (text.startsWith("http://") || text.startsWith("https://")) {
            webView.loadUrl(text)
        } else if (text.contains(".")) {
            webView.loadUrl("https://$text")
        } else {
            webView.loadUrl("https://www.google.com/search?q=${Uri.encode(text)}")
        }
    }

    private fun saveFavorite(url: String) {
        val prefs = getSharedPreferences("favorites", MODE_PRIVATE)
        val set = prefs.getStringSet("urls", mutableSetOf())!!.toMutableSet()
        set.add(url)
        prefs.edit().putStringSet("urls", set).apply()
    }

    override fun onBackPressed() {
        val webView = getCurrentWebView()
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
