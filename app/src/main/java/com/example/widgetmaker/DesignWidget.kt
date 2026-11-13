package com.example.widgetmaker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AColor
import android.net.Uri
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DesignWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val design = DesignStore.load(context)
        provideContent { DesignCanvas(design) }
    }
}

class DesignWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DesignWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedule(context)
    }
}

private fun String.toColor(): Color =
    runCatching { Color(AColor.parseColor(this)) }.getOrDefault(Color.White)

/** Free-form canvas: every element is layered in a Box and offset with padding. */
@Composable
fun DesignCanvas(design: WidgetDesign) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(design.background.color.toColor())
            .cornerRadius(design.cornerRadiusDp.dp),
        contentAlignment = Alignment.TopStart
    ) {
        design.elements.forEach { el ->
            Box(modifier = GlanceModifier.padding(start = el.xDp.dp, top = el.yDp.dp)) {
                RenderElement(el)
            }
        }
    }
}

@Composable
private fun RenderElement(el: Element) {
    val context = LocalContext.current
    var mod: GlanceModifier = GlanceModifier
    el.widthDp?.let { mod = mod.width(it.dp) }
    val action = el.action
    if (action != null && action.type == "openUrl") {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.value))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mod = mod.clickable(actionStartActivity(intent))
    }

    val color = ColorProvider(el.style.color.toColor())

    if (el.type == "progress") {
        LinearProgressIndicator(
            progress = batteryPercent(context) / 100f,
            modifier = mod,
            color = color,
            backgroundColor = ColorProvider(Color(0x33FFFFFF))
        )
        return
    }

    val value = when (el.type) {
        "clock" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        "date" -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
        "battery" -> "${batteryPercent(context)}%"
        else -> el.text.orEmpty()
    }

    Text(
        text = value,
        modifier = mod,
        style = TextStyle(
            color = color,
            fontSize = el.style.sizeSp.sp,
            fontWeight = if (el.style.bold) FontWeight.Bold else FontWeight.Normal
        )
    )
}

private fun batteryPercent(context: Context): Int {
    val i = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    return if (level >= 0 && scale > 0) level * 100 / scale else 0
}
