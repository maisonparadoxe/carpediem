package fr.maisonparadoxe.carpediem

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val reqImage = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.setBackgroundColor(Color.BLACK)
        web.webViewClient = Pages.client(this)
        web.addJavascriptInterface(Bridge(), "CarpeAndroid")
        web.loadUrl(Pages.GAME)
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }

    @Suppress("DEPRECATION")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqImage && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) Wall.saveImage(this, uri)
            web.evaluateJavascript("window.carpeRefresh&&carpeRefresh()", null)
        }
    }

    /** Ce que la page du jeu peut demander à l'appli (réglages Android). */
    inner class Bridge {
        @JavascriptInterface
        fun isActive(): Boolean = Wall.isActive(this@MainActivity)

        @JavascriptInterface
        fun hasImage(): Boolean = Wall.hasImage(this@MainActivity)

        @JavascriptInterface
        fun getOpacity(): Int = Wall.opacity(this@MainActivity)

        @JavascriptInterface
        fun setOpacity(v: Int) {
            Wall.setOpacity(this@MainActivity, v)
        }

        @JavascriptInterface
        fun toggle() {
            runOnUiThread {
                if (Wall.isActive(this@MainActivity)) {
                    Wall.disable(this@MainActivity)
                } else {
                    startActivity(Wall.enableIntent(this@MainActivity))
                }
            }
        }

        @JavascriptInterface
        fun pickImage() {
            runOnUiThread {
                val pick = Intent(Intent.ACTION_GET_CONTENT)
                pick.type = "image/*"
                @Suppress("DEPRECATION")
                startActivityForResult(Intent.createChooser(pick, "Image de fond"), reqImage)
            }
        }
    }
}
