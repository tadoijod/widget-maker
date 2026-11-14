package com.example.widgetmaker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class DesignWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val design = DesignStore.forWidget(context, appWidgetId)
        val data = AppDataStore.load(context)
        val background = renderBackground(design)
        provideContent { DesignCanvas(design, data, background) }
    }
}

private fun String.toColor(): Color = Color(parseColorOr(this))

@Composable
fun DesignCanvas(design: WidgetDesign, data: AppData, background: Bitmap) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Image(
            provider = ImageProvider(background),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        design.elements.forEach { el ->
            Box(modifier = GlanceModifier.padding(start = el.xDp.dp, top = el.yDp.dp)) {
                RenderElement(design, el, data)
            }
        }
    }
}

private fun textStyle(el: Element, dim: Boolean = false, strike: Boolean = false): TextStyle {
    val base = el.style.color.toColor()
    val color = if (dim) base.copy(alpha = base.alpha * 0.5f) else base
    val deco: TextDecoration? = when {
        strike -> TextDecoration.LineThrough
        el.underline -> TextDecoration.Underline
        else -> null
    }
    val align: TextAlign? = when (el.align) {
        "center" -> TextAlign.Center
        "end" -> TextAlign.End
        else -> null
    }
    return TextStyle(
        color = ColorProvider(color),
        fontSize = el.style.sizeSp.sp,
        fontWeight = if (el.style.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (el.italic) FontStyle.Italic else null,
        textAlign = align,
        textDecoration = deco
    )
}

private val blockTypes = setOf("todo", "note", "tracker", "countdown")

@Composable
private fun RenderElement(design: WidgetDesign, el: Element, data: AppData) {
    val context = LocalContext.current

    var mod: GlanceModifier = GlanceModifier
    el.bgColor?.let { chip ->
        mod = mod.background(chip.toColor())
        if (el.radiusDp > 0) mod = mod.cornerRadius(el.radiusDp.dp)
    }
    if (el.padDp > 0) mod = mod.padding(el.padDp.dp)

    val width: Int? = el.widthDp
        ?: if (el.type in blockTypes) (design.widthDp - el.xDp - 10).coerceAtLeast(40) else null
    if (width != null) mod = mod.width(width.dp)

    val action = el.action
    if (action != null && action.type == "openUrl") {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.value))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mod = mod.clickable(actionStartActivity(intent))
    }

    when (el.type) {
        "clock", "date" -> Box(modifier = mod) { AndroidRemoteViews(clockRemoteViews(context, el)) }

        "progress" -> {
            val t = data.trackers.firstOrNull { it.id == el.source }
            val fraction = if (t != null) {
                (t.current().toFloat() / t.goal.coerceAtLeast(1)).coerceIn(0f, 1f)
            } else {
                batteryPercent(context) / 100f
            }
            LinearProgressIndicator(
                progress = fraction,
                modifier = mod,
                color = ColorProvider(el.style.color.toColor()),
                backgroundColor = ColorProvider(Color(0x33FFFFFF))
            )
        }

        "todo" -> TodoBlock(el, data, mod)

        "note" -> {
            val note = data.notes.firstOrNull { it.id == el.source }
            Text(
                text = note?.body ?: "Pick a note in the editor",
                modifier = mod,
                style = textStyle(el),
                maxLines = el.lines.coerceAtLeast(1)
            )
        }

        "tracker" -> TrackerBlock(el, data, mod)

        "countdown" -> {
            val c = data.countdowns.firstOrNull { it.id == el.source }
            val days = c?.daysLeft()
            val label = when {
                c == null -> "Pick a countdown"
                days == null -> "Bad date"
                days == 0 -> "Today"
                days < 0 -> "${-days}d ago"
                days == 1 -> "1 day"
                else -> "$days days"
            }
            Text(text = label, modifier = mod, style = textStyle(el))
        }

        else -> {
            val value = if (el.type == "battery") "${batteryPercent(context)}%" else el.text.orEmpty()
            Text(text = value, modifier = mod, style = textStyle(el))
        }
    }
}

@Composable
private fun TodoBlock(el: Element, data: AppData, mod: GlanceModifier) {
    val list = data.todoLists.firstOrNull { it.id == el.source }
    if (list == null) {
        Text(text = "Pick a list in the editor", modifier = mod, style = textStyle(el))
        return
    }
    val items = (list.items.filter { !it.done } + list.items.filter { it.done })
        .take(el.lines.coerceAtLeast(1))
    Column(modifier = mod) {
        if (items.isEmpty()) {
            Text(text = "Nothing to do", style = textStyle(el, dim = true))
        }
        items.forEach { item ->
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clickable(
                        actionRunCallback<ToggleTodoAction>(
                            actionParametersOf(ListKey to list.id, ItemKey to item.id)
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (item.done) "●" else "○", style = textStyle(el, dim = item.done))
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = item.text,
                    modifier = GlanceModifier.defaultWeight(),
                    style = textStyle(el, dim = item.done, strike = item.done),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TrackerBlock(el: Element, data: AppData, mod: GlanceModifier) {
    val t = data.trackers.firstOrNull { it.id == el.source }
    if (t == null) {
        Text(text = "Pick a tracker", modifier = mod, style = textStyle(el))
        return
    }
    val unit = if (t.unit.isBlank()) "" else " " + t.unit
    val label = "${t.current()}/${t.goal}$unit"
    Row(modifier = mod, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "-",
            modifier = GlanceModifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(
                    actionRunCallback<StepTrackerAction>(
                        actionParametersOf(TrackerKey to t.id, DeltaKey to -1)
                    )
                ),
            style = textStyle(el)
        )
        Text(text = label, style = textStyle(el))
        Text(
            text = "+",
            modifier = GlanceModifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(
                    actionRunCallback<StepTrackerAction>(
                        actionParametersOf(TrackerKey to t.id, DeltaKey to 1)
                    )
                ),
            style = textStyle(el)
        )
    }
}

private fun batteryPercent(context: Context): Int {
    val i = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    return if (level >= 0 && scale > 0) level * 100 / scale else 0
}
