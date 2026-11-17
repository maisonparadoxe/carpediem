package fr.maisonparadoxe.carpediem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.view.View
import android.webkit.WebView

/**
 * Fond d'écran animé : le même jeu, sans interface, dessiné ~30 fois par seconde.
 * Il lit la partie enregistrée par l'appli (sans jamais l'écraser) et se met en pause quand il n'est pas visible.
 */
class PondWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = PondEngine()

    inner class PondEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var web: WebView? = null
        private var w = 0
        private var h = 0
        private var shown = false
        private var backdrop: Bitmap? = null
        private var alpha = 255
        private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        private val ticker = object : Runnable {
            override fun run() {
                if (!shown) return
                drawFrame()
                handler.postDelayed(this, 33)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val v = WebView(applicationContext)
            v.settings.javaScriptEnabled = true
            v.settings.domStorageEnabled = true
            v.setBackgroundColor(Color.BLACK)
            v.webViewClient = Pages.client(applicationContext)
            web = v
            v.loadUrl(Pages.WALL)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            w = width
            h = height
            layoutWeb()
            drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            shown = visible
            handler.removeCallbacks(ticker)
            val v = web ?: return
            if (visible) {
                loadSettings()
                v.onResume()
                v.loadUrl(Pages.WALL)   // recharge la partie enregistrée par l'appli
                layoutWeb()
                handler.post(ticker)
            } else {
                v.evaluateJavascript("window.carpePause&&carpePause(true)", null)
                v.onPause()
            }
        }

        override fun onDestroy() {
            handler.removeCallbacks(ticker)
            web?.destroy()
            web = null
            super.onDestroy()
        }

        private fun layoutWeb() {
            val v = web ?: return
            if (w <= 0 || h <= 0) return
            v.measure(
                View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
            )
            v.layout(0, 0, w, h)
        }

        private fun loadSettings() {
            alpha = Wall.opacity(applicationContext) * 255 / 100
            backdrop = if (Wall.hasImage(applicationContext)) {
                BitmapFactory.decodeFile(Wall.imageFile(applicationContext).path)
            } else {
                null
            }
        }

        private fun drawCover(c: Canvas, bg: Bitmap) {
            val s = maxOf(w.toFloat() / bg.width, h.toFloat() / bg.height)
            val dw = bg.width * s
            val dh = bg.height * s
            val dst = RectF((w - dw) / 2f, (h - dh) / 2f, (w + dw) / 2f, (h + dh) / 2f)
            c.drawBitmap(bg, null, dst, paint)
        }

        private fun drawFrame() {
            val v = web ?: return
            val holder = surfaceHolder
            var c: Canvas? = null
            try {
                c = holder.lockCanvas()
                if (c == null) return
                c.drawColor(Color.BLACK)
                val bg = backdrop
                if (bg != null && alpha < 255) {
                    drawCover(c, bg)
                    val layer = c.saveLayerAlpha(0f, 0f, w.toFloat(), h.toFloat(), alpha)
                    v.draw(c)
                    c.restoreToCount(layer)
                } else {
                    v.draw(c)
                }
            } catch (e: Exception) {
                // une image ratée n'est pas grave, la suivante arrive dans 33 ms
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c)
            }
        }
    }
}
