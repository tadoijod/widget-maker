package com.example.widgetmaker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DesignWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedule(context)
    }
}

/** 4x2 (kept under its original name so widgets added earlier keep working). */
class DesignWidgetReceiver : BaseReceiver()
class Widget2x1Receiver : BaseReceiver()
class Widget4x1Receiver : BaseReceiver()
class Widget2x2Receiver : BaseReceiver()
class Widget3x3Receiver : BaseReceiver()
class Widget4x3Receiver : BaseReceiver()
class Widget4x4Receiver : BaseReceiver()
class Widget2x4Receiver : BaseReceiver()

/** Refreshes widgets when charging state or battery state changes. */
class PowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                DesignWidget().updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }
}
