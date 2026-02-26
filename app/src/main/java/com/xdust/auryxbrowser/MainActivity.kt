package com.xdust.auryxbrowser

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var homeLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        homeLayout = findViewById(R.id.homeLayout)

        setupWebView()
        setupHomepageButtons()
        setupAddressBar()
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
    }

    private fun setupHomepageButtons() {

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
            loadUrl("https://www.google.com/search?q=Auryx+Trends")
        }
    }

    private fun setupAddressBar() {
        addressBar.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                var url = addressBar.text.toString()

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://www.google.com/search?q=$url"
                }

                loadUrl(url)
                true
            } else {
                false
            }
        }
    }

    private fun loadUrl(url: String) {
        homeLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
        addressBar.setText(url)
    }

    override fun onBackPressed() {
        if (webView.visibility == View.VISIBLE && webView.canGoBack()) {
            webView.goBack()
        } else if (webView.visibility == View.VISIBLE) {
            webView.visibility = View.GONE
            homeLayout.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}
