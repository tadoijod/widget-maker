package com.example.widgetmaker

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

/** One active design shared by all widget instances (per-widget designs come later). */
object DesignStore {
    private const val PREFS = "designs"
    private const val KEY = "active_design"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun parse(raw: String): Result<WidgetDesign> =
        runCatching { json.decodeFromString(WidgetDesign.serializer(), raw) }

    fun loadRaw(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: Templates.all.first().second

    fun saveRaw(context: Context, raw: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, raw).apply()
    }

    fun load(context: Context): WidgetDesign =
        parse(loadRaw(context)).getOrNull()
            ?: parse(Templates.all.first().second).getOrThrow()
}
