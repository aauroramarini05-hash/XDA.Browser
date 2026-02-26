package com.xdust.auryxbrowser

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var goButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        goButton = findViewById(R.id.goButton)

        webView.webViewClient = WebViewClient()

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        webView.loadUrl("https://www.google.com")

        goButton.setOnClickListener {
            var url = urlBar.text.toString()

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }

            webView.loadUrl(url)
        }
    }
}
