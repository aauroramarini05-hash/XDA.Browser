package com.xdust.auryxbrowser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etUrl: AutoCompleteTextView
    private lateinit var btnGo: Button
    private lateinit var btnNewTab: Button
    private lateinit var btnNextTab: Button
    private lateinit var btnCloseTab: Button
    private lateinit var btnFav: Button
    private lateinit var btnDesktop: Button
    private lateinit var webContainer: FrameLayout

    private val tabs = mutableListOf<WebView>()
    private var currentTabIndex = 0

    private var desktopModeEnabled = false
    private val UA_DESKTOP =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    private val prefs by lazy { getSharedPreferences("auryx_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUrl = findViewById(R.id.etUrl)
        btnGo = findViewById(R.id.btnGo)
        btnNewTab = findViewById(R.id.btnNewTab)
        btnNextTab = findViewById(R.id.btnNextTab)
        btnCloseTab = findViewById(R.id.btnCloseTab)
        btnFav = findViewById(R.id.btnFav)
        btnDesktop = findViewById(R.id.btnDesktop)
        webContainer = findViewById(R.id.webContainer)

        // Load desktop-mode preference
        desktopModeEnabled = prefs.getBoolean("desktop_mode", false)
        updateDesktopButton()

        // Setup autocomplete with a small local list (history + favorites)
        refreshAutocomplete()

        btnGo.setOnClickListener {
            loadFromBar()
        }

        etUrl.setOnEditorActionListener { _, actionId, event ->
            val enterPressed = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                loadFromBar()
                true
            } else {
                false
            }
        }

        btnNewTab.setOnClickListener {
            createNewTab("https://www.google.com")
        }

        btnNextTab.setOnClickListener {
            if (tabs.isNotEmpty()) {
                currentTabIndex = (currentTabIndex + 1) % tabs.size
                showCurrentTab()
                toast("Tab ${currentTabIndex + 1}/${tabs.size}")
            }
        }

        btnCloseTab.setOnClickListener {
            closeCurrentTab()
        }

        btnFav.setOnClickListener {
            val wv = getCurrentWebView() ?: return@setOnClickListener
            val url = wv.url ?: return@setOnClickListener
            addFavorite(url)
            refreshAutocomplete()
            toast("Aggiunto ai preferiti")
        }

        btnDesktop.setOnClickListener {
            desktopModeEnabled = !desktopModeEnabled
            prefs.edit().putBoolean("desktop_mode", desktopModeEnabled).apply()
            applyDesktopModeToCurrentTab(reload = true)
            updateDesktopButton()
        }

        // Start with one tab
        if (tabs.isEmpty()) {
            createNewTab("https://www.google.com")
        }
    }

    private fun loadFromBar() {
        val input = etUrl.text?.toString()?.trim().orEmpty()
        if (input.isBlank()) return

        val url = toSmartUrl(input)
        saveHistory(url)

        getCurrentWebView()?.loadUrl(url)
        refreshAutocomplete()
        etUrl.dismissDropDown()
    }

    private fun createNewTab(initialUrl: String) {
        val wv = WebView(this)
        wv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupWebView(wv)
        applyDesktopModeToWebView(wv)

        tabs.add(wv)
        currentTabIndex = tabs.size - 1
        showCurrentTab()

        wv.loadUrl(initialUrl)
        toast("Nuova tab (${tabs.size})")
    }

    private fun closeCurrentTab() {
        if (tabs.isEmpty()) return

        val wv = tabs.removeAt(currentTabIndex)
        try {
            wv.stopLoading()
            wv.loadUrl("about:blank")
            wv.clearHistory()
            wv.removeAllViews()
            wv.destroy()
        } catch (_: Exception) {}

        if (tabs.isEmpty()) {
            webContainer.removeAllViews()
            currentTabIndex = 0
            createNewTab("https://www.google.com")
            return
        }

        if (currentTabIndex >= tabs.size) currentTabIndex = tabs.size - 1
        showCurrentTab()
        toast("Tab chiusa. Rimaste: ${tabs.size}")
    }

    private fun showCurrentTab() {
        webContainer.removeAllViews()
        val wv = getCurrentWebView() ?: return
        webContainer.addView(wv)

        // Update bar with current URL (if any)
        val url = wv.url
        if (!url.isNullOrBlank()) etUrl.setText(url)
    }

    private fun getCurrentWebView(): WebView? {
        if (tabs.isEmpty()) return null
        return tabs.getOrNull(currentTabIndex)
    }

    private fun setupWebView(webView: WebView) {
        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.loadsImagesAutomatically = true
        s.mediaPlaybackRequiresUserGesture = false
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = true

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (view == getCurrentWebView()) {
                    etUrl.setText(url)
                }
            }
        }

        // Download manager
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimeType)
                request.addRequestHeader("User-Agent", userAgent)
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                request.setDescription("Download in corso...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                toast("Download avviato")
            } catch (e: Exception) {
                toast("Errore download: ${e.message}")
            }
        }
    }

    // ---- Desktop mode ----

    private fun applyDesktopModeToCurrentTab(reload: Boolean) {
        val webView = getCurrentWebView() ?: return
        applyDesktopModeToWebView(webView)
        if (reload) webView.reload()
    }

    private fun applyDesktopModeToWebView(webView: WebView) {
        val s = webView.settings
        if (desktopModeEnabled) {
            s.userAgentString = UA_DESKTOP
            s.useWideViewPort = true
            s.loadWithOverviewMode = true
        } else {
            s.userAgentString = null // default mobile UA
            s.useWideViewPort = false
            s.loadWithOverviewMode = false
        }
    }

    private fun updateDesktopButton() {
        btnDesktop.text = if (desktopModeEnabled) "Desktop: ON" else "Desktop: OFF"
    }

    // ---- Favorites & History (basic) ----

    private fun addFavorite(url: String) {
        val set = prefs.getStringSet("favorites", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(url)
        prefs.edit().putStringSet("favorites", set).apply()
    }

    private fun saveHistory(url: String) {
        val set = prefs.getStringSet("history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(url)
        prefs.edit().putStringSet("history", set).apply()
    }

    private fun refreshAutocomplete() {
        val fav = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        val hist = prefs.getStringSet("history", emptySet()) ?: emptySet()
        val combined = (fav + hist).toList().sorted()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, combined)
        etUrl.setAdapter(adapter)

        etUrl.setOnItemClickListener { _, _, position, _ ->
            val chosen = adapter.getItem(position) ?: return@setOnItemClickListener
            etUrl.setText(chosen)
            etUrl
