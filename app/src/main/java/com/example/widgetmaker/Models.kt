package com.example.widgetmaker

import android.appwidget.AppWidgetManager
import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class WidgetDesign(
    val id: String,
    val name: String,
    val widthDp: Int = 250,
    val heightDp: Int = 110,
    val cornerRadiusDp: Int = 24,
    val background: Background = Background(),
    val elements: List<Element> = emptyList()
)

@Serializable
data class Background(
    val color: String = "#1E1E2E",
    val color2: String? = null,          // gradient end color (null = solid)
    val direction: String = "vertical",  // vertical | horizontal | diagonal
    val opacity: Int = 100,              // 0..100
    val borderColor: String = "#FFFFFF",
    val borderDp: Int = 0
)

@Serializable
data class Element(
    val type: String,   // text clock date battery progress todo note tracker countdown
    val text: String? = null,
    val source: String? = null,          // id of list / note / tracker / countdown
    val format: String? = null,          // clock / date pattern
    val xDp: Int = 0,
    val yDp: Int = 0,
    val widthDp: Int? = null,
    val lines: Int = 5,
    val align: String = "start",         // start | center | end
    val italic: Boolean = false,
    val underline: Boolean = false,
    val bgColor: String? = null,         // optional chip behind the element
    val padDp: Int = 0,
    val radiusDp: Int = 0,
    val style: Style = Style(),
    val action: Action? = null
)

@Serializable
data class Style(
    val color: String = "#FFFFFF",
    val sizeSp: Int = 16,
    val bold: Boolean = false
)

@Serializable
data class Action(val type: String, val value: String)   // type: openUrl

enum class WidgetSize(val label: String, val widthDp: Int, val heightDp: Int, val receiverName: String) {
    S2x1("2x1", 110, 40, "Widget2x1Receiver"),
    S4x1("4x1", 250, 40, "Widget4x1Receiver"),
    S2x2("2x2", 110, 110, "Widget2x2Receiver"),
    S4x2("4x2", 250, 110, "DesignWidgetReceiver"),
    S3x3("3x3", 180, 180, "Widget3x3Receiver"),
    S4x3("4x3", 250, 180, "Widget4x3Receiver"),
    S4x4("4x4", 250, 250, "Widget4x4Receiver"),
    S2x4("2x4", 110, 250, "Widget2x4Receiver");

    companion object {
        fun match(w: Int, h: Int): WidgetSize =
            entries.minByOrNull { Math.abs(it.widthDp - w) + Math.abs(it.heightDp - h) } ?: S4x2

        fun fromProvider(context: Context, appWidgetId: Int): WidgetSize? {
            val name = AppWidgetManager.getInstance(context)
                .getAppWidgetInfo(appWidgetId)?.provider?.className ?: return null
            return entries.firstOrNull { name.endsWith(it.receiverName) }
        }
    }
}

/** A library of designs plus a mapping from each widget instance to its design. */
object DesignStore {
    private const val PREFS = "designs"
    private const val KEY_LIBRARY = "library"
    private const val KEY_PENDING = "pending"

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val prettyJson = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }
    private val listSerializer = ListSerializer(WidgetDesign.serializer())

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun parse(raw: String): Result<WidgetDesign> =
        runCatching { json.decodeFromString(WidgetDesign.serializer(), raw) }

    fun toJson(design: WidgetDesign): String =
        prettyJson.encodeToString(WidgetDesign.serializer(), design)

    fun all(context: Context): List<WidgetDesign> {
        val raw = prefs(context).getString(KEY_LIBRARY, null)
        val stored = raw?.let { runCatching { json.decodeFromString(listSerializer, it) }.getOrNull() }
        if (!stored.isNullOrEmpty()) return stored
        val seeded = Templates.starterIndexes.mapNotNull { i ->
            Templates.all.getOrNull(i)?.let { t -> parse(t.second).getOrNull() }
        }
        writeAll(context, seeded)
        return seeded
    }

    private fun writeAll(context: Context, list: List<WidgetDesign>) {
        prefs(context).edit().putString(KEY_LIBRARY, json.encodeToString(listSerializer, list)).apply()
    }

    fun get(context: Context, id: String): WidgetDesign? = all(context).firstOrNull { it.id == id }

    fun save(context: Context, design: WidgetDesign) {
        val list = all(context).toMutableList()
        val index = list.indexOfFirst { it.id == design.id }
        if (index >= 0) list[index] = design else list.add(design)
        writeAll(context, list)
    }

    /** Saves a copy with a fresh id (used for templates and duplicates). */
    fun addCopy(context: Context, design: WidgetDesign, name: String = design.name): WidgetDesign {
        val copy = design.copy(id = UUID.randomUUID().toString(), name = name)
        save(context, copy)
        return copy
    }

    fun delete(context: Context, id: String) {
        val list = all(context).filter { it.id != id }
        if (list.isNotEmpty()) writeAll(context, list)
    }

    fun assign(context: Context, appWidgetId: Int, designId: String) {
        prefs(context).edit().putString("w_$appWidgetId", designId).apply()
    }

    fun forWidget(context: Context, appWidgetId: Int): WidgetDesign {
        val list = all(context)
        val id = prefs(context).getString("w_$appWidgetId", null)
        list.firstOrNull { it.id == id }?.let { return it }
        val size = WidgetSize.fromProvider(context, appWidgetId)
        return list.firstOrNull { size != null && WidgetSize.match(it.widthDp, it.heightDp) == size }
            ?: list.first()
    }

    /** Remember which design the next pinned widget should show (valid for 2 minutes). */
    fun setPending(context: Context, designId: String) {
        prefs(context).edit().putString(KEY_PENDING, "$designId|${System.currentTimeMillis()}").apply()
    }

    fun takePending(context: Context): String? {
        val raw = prefs(context).getString(KEY_PENDING, null) ?: return null
        prefs(context).edit().remove(KEY_PENDING).apply()
        val parts = raw.split("|")
        if (parts.size != 2) return null
        val t = parts[1].toLongOrNull() ?: return null
        return if (System.currentTimeMillis() - t < 120_000L) parts[0] else null
    }

    fun newDesign(): WidgetDesign = WidgetDesign(
        id = UUID.randomUUID().toString(),
        name = "New design",
        elements = listOf(
            Element(type = "clock", xDp = 16, yDp = 16, style = Style(sizeSp = 36, bold = true))
        )
    )
}
