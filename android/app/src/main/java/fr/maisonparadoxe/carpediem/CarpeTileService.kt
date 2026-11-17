package fr.maisonparadoxe.carpediem

import android.app.PendingIntent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** Tuile des réglages rapides : un geste pour activer ou désactiver Carpe Diem en fond d'écran. */
class CarpeTileService : TileService() {
    override fun onStartListening() {
        refresh()
    }

    @Suppress("DEPRECATION")
    override fun onClick() {
        if (Wall.isActive(this)) {
            Wall.disable(this)
            refresh()
        } else {
            val i = Wall.enableIntent(this)
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE))
            } else {
                startActivityAndCollapse(i)
            }
        }
    }

    private fun refresh() {
        val t = qsTile ?: return
        t.state = if (Wall.isActive(this)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.updateTile()
    }
}
