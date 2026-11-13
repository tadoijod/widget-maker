package com.example.widgetmaker

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
data class Background(val color: String = "#1E1E2E")

@Serializable
data class Element(
    val type: String,          // text | clock | date | battery | progress
    val text: String? = null,
    val xDp: Int = 0,
    val yDp: Int = 0,
    val widthDp: Int? = null,
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

/** A library of designs plus a mapping from each widget instance to its design. */
object DesignStore {
    private const val PREFS = "designs"
    private const val KEY_LIBRARY = "library"

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
        val seeded = Templates.all.mapNotNull { parse(it.second).getOrNull() }
        writeAll(context, seeded)
        return seeded
    }

    private fun writeAll(context: Context, list: List<WidgetDesign>) {
        prefs(context).edit().putString(KEY_LIBRARY, json.encodeToString(listSerializer, list)).apply()
    }

    fun save(context: Context, design: WidgetDesign) {
        val list = all(context).toMutableList()
        val index = list.indexOfFirst { it.id == design.id }
        if (index >= 0) list[index] = design else list.add(design)
        writeAll(context, list)
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
        return list.firstOrNull { it.id == id } ?: list.first()
    }

    fun newDesign(): WidgetDesign = WidgetDesign(
        id = UUID.randomUUID().toString(),
        name = "New design",
        elements = listOf(
            Element(type = "clock", xDp = 16, yDp = 16, style = Style(sizeSp = 36, bold = true))
        )
    )
}
