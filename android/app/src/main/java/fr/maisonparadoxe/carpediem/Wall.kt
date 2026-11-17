package fr.maisonparadoxe.carpediem

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import java.io.File

/** Réglages et actions partagés entre l'appli, le fond d'écran animé et la tuile des réglages rapides. */
object Wall {
    private const val PREFS = "carpe"

    fun opacity(c: Context): Int = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("opacity", 75)

    fun setOpacity(c: Context, v: Int) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("opacity", v.coerceIn(30, 100)).apply()
    }

    fun imageFile(c: Context): File = File(c.filesDir, "classic.jpg")

    fun hasImage(c: Context): Boolean = imageFile(c).exists()

    /** Vrai si Carpe Diem est le fond d'écran animé actuellement en place. */
    fun isActive(c: Context): Boolean {
        val info = WallpaperManager.getInstance(c).wallpaperInfo
        return info != null && info.packageName == c.packageName
    }

    /** Ouvre l'écran système qui demande de confirmer le fond d'écran animé. */
    fun enableIntent(c: Context): Intent {
        val i = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        i.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(c, PondWallpaperService::class.java)
        )
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return i
    }

    /** Remet l'image habituelle (celle choisie dans l'appli), ou le fond d'écran par défaut du téléphone. */
    fun disable(c: Context) {
        val wm = WallpaperManager.getInstance(c)
        try {
            val f = imageFile(c)
            if (f.exists()) {
                val bmp = BitmapFactory.decodeFile(f.path)
                if (bmp != null) {
                    wm.setBitmap(bmp)
                    return
                }
            }
            wm.clear()
        } catch (e: Exception) {
            // rien de plus à faire : le fond d'écran reste tel quel
        }
    }

    /** Copie l'image choisie dans l'appli, à la taille de l'écran. */
    fun saveImage(c: Context, uri: Uri): Boolean {
        try {
            val dm = c.resources.displayMetrics
            val maxSide = maxOf(dm.widthPixels, dm.heightPixels)
            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
            val opts = BitmapFactory.Options()
            opts.inSampleSize = sample
            val bmp = c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            if (bmp == null) return false
            imageFile(c).outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}

/** Le jeu est servi depuis les fichiers de l'appli (aucun réseau nécessaire) sous une adresse https fixe. */
object Pages {
    const val HOST = "appassets.androidplatform.net"
    const val GAME = "https://appassets.androidplatform.net/assets/index.html"
    const val WALL = "https://appassets.androidplatform.net/assets/index.html?wallpaper=1"

    fun client(c: Context): WebViewClient {
        val loader = WebViewAssetLoader.Builder()
            .setDomain(HOST)
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(c))
            .build()
        return object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return loader.shouldInterceptRequest(request.url)
            }
        }
    }
}
