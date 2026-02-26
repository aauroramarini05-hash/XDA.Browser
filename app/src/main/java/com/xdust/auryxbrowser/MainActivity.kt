package com.xdust.auryxbrowser

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var homeLayout: LinearLayout
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var btnMenu: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnGo: ImageButton

    // Drawer buttons (DEVONO ESISTERE con questi ID nel layout)
    private lateinit var btnDesktopMode: View
    private lateinit var btnIncognitoMode: View
    private lateinit var btnHomepage: View

    // Drawer footer
    private lateinit var tvVersion: TextView
    private lateinit var tvUpdate: TextView

    private var incognitoEnabled = false
    private var desktopEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Core views
        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        homeLayout = findViewById(R.id.homeLayout)
        drawerLayout = findViewById(R.id.drawerLayout)

        // Top bar buttons
        btnMenu = findViewById(R.id.btnMenu)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnGo = findViewById(R.id.btnGo)

        // Drawer items
        btnDesktopMode = findViewById(R.id.btnDesktopMode)
        btnIncognitoMode = findViewById(R.id.btnIncognitoMode)
        btnHomepage = findViewById(R.id.btnHomepage)

        // Drawer footer
        tvVersion = findViewById(R.id.tvVersion)
        tvUpdate = findViewById(R.id.tvUpdate)

        tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        tvUpdate.visibility = View.GONE

        setupWebView()
        setupToolbar()
        setupAddressBar()
        setupDrawerActions()

        // Controllo aggiornamenti GitHub (mostra "Aggiornamento disponibile")
        checkGitHubUpdateBadge()
    }

    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (!url.isNullOrBlank() && webView.visibility == View.VISIBLE) {
                    addressBar.setText(url)
                }
            }
        }
        webView.webChromeClient = WebChromeClient()

        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.builtInZoomControls = true
        s.displayZoomControls = false

        applyModesToWebView()
    }

    private fun setupToolbar() {
        btnMenu.setOnClickListener { drawerLayout.openDrawer(Gravity.START) }

        btnRefresh.setOnClickListener {
            if (webView.visibility == View.VISIBLE) webView.reload()
        }

        btnGo.setOnClickListener {
            submitAddressBar()
        }
    }

    private fun setupAddressBar() {
        // ENTER / GO da tastiera
        addressBar.setOnEditorActionListener { _, actionId, event ->
            val enterPressed = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                submitAddressBar()
                true
            } else {
                false
            }
        }
    }

    private fun submitAddressBar() {
        val raw = addressBar.text?.toString()?.trim().orEmpty()
        if (raw.isBlank()) return

        val url = normalizeInputToUrl(raw)
        loadUrl(url)
    }

    private fun normalizeInputToUrl(input: String): String {
        val t = input.trim()

        // Se sembra un dominio (contiene un punto) e non ha spazi -> prova https://
        val looksLikeDomain = t.contains(".") && !t.contains(" ")
        if (t.startsWith("http://") || t.startsWith("https://")) return t
        if (looksLikeDomain) return "https://$t"

        // Altrimenti è una ricerca
        val q = java.net.URLEncoder.encode(t, "UTF-8")
        return "https://www.google.com/search?q=$q"
    }

    private fun loadUrl(url: String) {
        homeLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
        addressBar.setText(url)
    }

    private fun setupDrawerActions() {
        btnDesktopMode.setOnClickListener {
            desktopEnabled = !desktopEnabled
            applyModesToWebView()
            if (webView.visibility == View.VISIBLE) webView.reload()
            drawerLayout.closeDrawer(Gravity.START)
        }

        btnIncognitoMode.setOnClickListener {
            incognitoEnabled = !incognitoEnabled
            applyModesToWebView()

            // pulizia “hard” quando entri in incognito
            if (incognitoEnabled) {
                clearWebData()
            }

            drawerLayout.closeDrawer(Gravity.START)
        }

        btnHomepage.setOnClickListener {
            showHomepage()
            drawerLayout.closeDrawer(Gravity.START)
        }
    }

    private fun showHomepage() {
        webView.visibility = View.GONE
        homeLayout.visibility = View.VISIBLE
    }

    private fun applyModesToWebView() {
        val s = webView.settings

        // Desktop mode
        if (desktopEnabled) {
            s.userAgentString =
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
            s.useWideViewPort = true
            s.loadWithOverviewMode = true
        } else {
            s.userAgentString = null // default
        }

        // Incognito mode (base ma concreta)
        if (incognitoEnabled) {
            s.cacheMode = WebSettings.LOAD_NO_CACHE
            s.saveFormData = false
            s.setSupportZoom(true)
            CookieManager.getInstance().setAcceptCookie(false)
        } else {
            s.cacheMode = WebSettings.LOAD_DEFAULT
            CookieManager.getInstance().setAcceptCookie(true)
        }
    }

    private fun clearWebData() {
        try {
            webView.clearHistory()
            webView.clearCache(true)
            WebStorage.getInstance().deleteAllData()

            val cm = CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
        } catch (_: Throwable) {
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START)
            return
        }

        if (webView.visibility == View.VISIBLE && webView.canGoBack()) {
            webView.goBack()
            return
        }

        if (webView.visibility == View.VISIBLE) {
            showHomepage()
            return
        }

        super.onBackPressed()
    }

    /**
     * Mostra badge giallo "Aggiornamento disponibile" se l'ultima release GitHub
     * ha una versione più nuova di BuildConfig.VERSION_NAME.
     */
    private fun checkGitHubUpdateBadge() {
        val prefs = getSharedPreferences("updates", MODE_PRIVATE)

        // evitare chiamate continue: 1 volta ogni 6 ore
        val last = prefs.getLong("last_check", 0L)
        val now = System.currentTimeMillis()
        if (now - last < 6 * 60 * 60 * 1000L) return
        prefs.edit().putLong("last_check", now).apply()

        val current = BuildConfig.VERSION_NAME

        thread {
            val latest = fetchLatestReleaseVersion(
                owner = "aauroramarini05-hash",
                repo = "XDA.Browser"
            )

            if (latest != null && isNewerVersion(latest, current)) {
                runOnUiThread {
                    tvUpdate.text = "Aggiornamento disponibile"
                    tvUpdate.visibility = View.VISIBLE
                }
            } else {
                runOnUiThread { tvUpdate.visibility = View.GONE }
            }
        }
    }

    private fun fetchLatestReleaseVersion(owner: String, repo: String): String? {
        val url = URL("https://api.github.com/repos/$owner/$repo/releases/latest")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "AuryxBrowser")
        }

        return try {
            val code = conn.responseCode
            if (code !in 200..299) return null
            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)

            // Prefer tag_name (es: v1.226.01) altrimenti name
            val tag = json.optString("tag_name", "")
            val name = json.optString("name", "")

            val raw = if (tag.isNotBlank()) tag else name
            raw.trim().removePrefix("v").removePrefix("V")
        } catch (_: Throwable) {
            null
        } finally {
            conn.disconnect()
        }
    }

    // confronto versioni tipo 1.226.01
    private fun isNewerVersion(latest: String, current: String): Boolean {
        fun parse(v: String): List<Int> =
            v.split(".").mapNotNull { it.toIntOrNull() }

        val a = parse(latest)
        val b = parse(current)
        val max = maxOf(a.size, b.size)
        for (i in 0 until max) {
            val ai = a.getOrElse(i) { 0 }
            val bi = b.getOrElse(i) { 0 }
            if (ai > bi) return true
            if (ai < bi) return false
        }
        return false
    }
}
