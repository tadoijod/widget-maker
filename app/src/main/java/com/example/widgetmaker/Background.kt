package com.example.widgetmaker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.content.Context
import android.widget.RemoteViews
import android.util.TypedValue

fun parseColorOr(hex: String, fallback: Int = Color.WHITE): Int =
    runCatching { Color.parseColor(hex) }.getOrDefault(fallback)

/** Draws the widget background (solid or gradient, opacity, rounded corners, border) as a bitmap. */
fun renderBackground(design: WidgetDesign): Bitmap {
    val scale = 2.5f
    val w = (design.widthDp * scale).toInt().coerceIn(40, 900)
    val h = (design.heightDp * scale).toInt().coerceIn(40, 900)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val bg = design.background

    val c1 = parseColorOr(bg.color)
    val c2 = bg.color2?.let { parseColorOr(it) } ?: c1
    val wf = w.toFloat()
    val hf = h.toFloat()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = when (bg.direction) {
        "horizontal" -> LinearGradient(0f, 0f, wf, 0f, c1, c2, Shader.TileMode.CLAMP)
        "diagonal" -> LinearGradient(0f, 0f, wf, hf, c1, c2, Shader.TileMode.CLAMP)
        else -> LinearGradient(0f, 0f, 0f, hf, c1, c2, Shader.TileMode.CLAMP)
    }
    paint.alpha = bg.opacity.coerceIn(0, 100) * 255 / 100

    val radius = design.cornerRadiusDp * scale
    canvas.drawRoundRect(RectF(0f, 0f, wf, hf), radius, radius, paint)

    if (bg.borderDp > 0) {
        val strokeW = bg.borderDp * scale
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = strokeW
        stroke.color = parseColorOr(bg.borderColor)
        val half = strokeW / 2f
        val r = (radius - half).coerceAtLeast(0f)
        canvas.drawRoundRect(RectF(half, half, wf - half, hf - half), r, r, stroke)
    }
    return bmp
}

/** Native TextClock so the time and date tick by themselves with zero delay. */
fun clockRemoteViews(context: Context, el: Element): RemoteViews {
    val layout = if (el.style.bold) R.layout.widget_text_clock_bold else R.layout.widget_text_clock
    val rv = RemoteViews(context.packageName, layout)
    val pattern = el.format?.takeIf { it.isNotBlank() }
        ?: if (el.type == "date") "EEE, d MMM" else "HH:mm"
    rv.setCharSequence(R.id.text_clock, "setFormat24Hour", pattern)
    rv.setCharSequence(R.id.text_clock, "setFormat12Hour", pattern)
    rv.setTextColor(R.id.text_clock, parseColorOr(el.style.color))
    rv.setTextViewTextSize(R.id.text_clock, TypedValue.COMPLEX_UNIT_SP, el.style.sizeSp.toFloat())
    return rv
}
