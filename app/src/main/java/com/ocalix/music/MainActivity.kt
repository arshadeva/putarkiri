package com.ocalix.music

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private val prefs by lazy { getSharedPreferences("ocalix", MODE_PRIVATE) }
    private val defaultUrl = "http://10.0.2.2:7777"
    private val backendUrl: String get() = prefs.getString("backend_url", defaultUrl)!!.trimEnd('/')

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    if (request?.isForMainFrame == true) showBackendError()
                }
            }
            webChromeClient = WebChromeClient()
            addJavascriptInterface(AndroidBridge(), "OcalixAndroid")
        }
        setContentView(FrameLayout(this).apply { addView(web, FrameLayout.LayoutParams(-1, -1)) })
        val configuredUrl = BuildConfig.BACKEND_BASE_URL.trim().trimEnd('/')
        val savedUrl = prefs.getString("backend_url", null)
        if (configuredUrl.isNotBlank() && (savedUrl.isNullOrBlank() || savedUrl == defaultUrl)) {
            prefs.edit().putString("backend_url", configuredUrl).apply()
        }
        if (prefs.getString("backend_url", null).isNullOrBlank()) showBackendSetup() else loadBackend()
    }

    private fun loadBackend() {
        val url = backendUrl
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            Toast.makeText(this, "Alamat backend harus diawali http:// atau https://", Toast.LENGTH_LONG).show()
            showBackendSetup(); return
        }
        web.loadUrl(url)
    }

    private fun showBackendSetup() {
        val input = EditText(this).apply {
            setText(prefs.getString("backend_url", defaultUrl))
            hint = "http://IP-PUBLIK:PORT"
            singleLine = true
        }
        AlertDialog.Builder(this)
            .setTitle("Hubungkan Ocalix Node.js")
            .setMessage("Masukkan alamat backend Node.js Ocalix. Tanpa domain, gunakan http://IP-PUBLIK:PORT sesuai Allocation server panel. Emulator Android lokal dapat memakai http://10.0.2.2:7777.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Simpan & buka") { _, _ ->
                val value = input.text.toString().trim().trimEnd('/')
                if (value.startsWith("http://") || value.startsWith("https://")) {
                    prefs.edit().putString("backend_url", value).apply(); loadBackend()
                } else showBackendSetup()
            }
            .setNeutralButton("Pakai alamat emulator") { _, _ ->
                prefs.edit().putString("backend_url", defaultUrl).apply(); loadBackend()
            }.show()
    }

    private fun showBackendError() {
        runOnUiThread {
            Toast.makeText(this, "Backend tidak terjangkau. Periksa URL dan server Node.js.", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        }
    }

    inner class AndroidBridge {
        @JavascriptInterface fun onPlayback(payload: String) {
            try {
                val json = org.json.JSONObject(payload)
                val title = json.optString("title", "Ocalix")
                val artist = json.optString("artist", "")
                val playing = json.optBoolean("playing", false)
                val intent = Intent(this@MainActivity, PlaybackKeepAliveService::class.java).apply {
                    action = if (playing) PlaybackKeepAliveService.ACTION_START else PlaybackKeepAliveService.ACTION_PAUSE
                    putExtra("title", title); putExtra("artist", artist)
                }
                if (Build.VERSION.SDK_INT >= 26) ContextCompat.startForegroundService(this@MainActivity, intent)
                else startService(intent)
            } catch (_: Exception) { }
        }
        @JavascriptInterface fun configureBackend() { runOnUiThread { showBackendSetup() } }
        @JavascriptInterface fun appVersion(): String = "Ocalix Android 2.0"
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::web.isInitialized) { web.removeJavascriptInterface("OcalixAndroid"); web.destroy() }
        super.onDestroy()
    }
}
