package com.xdust.auryxbrowser

import android.os.Bundle
import android.view.Gravity
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)

        val goButton: ImageView = findViewById(R.id.goButton)
        val menuButton: ImageView = findViewById(R.id.menuButton)

        webView.webViewClient = WebViewClient()

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        goButton.setOnClickListener {
            loadUrl(urlBar.text.toString())
        }

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }
    }

    private fun loadUrl(input: String) {
        var url = input.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://www.google.com/search?q=$url"
        }
        webView.loadUrl(url)
        urlBar.setText(url)
    }
}
