package com.example.widgetmaker

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Serializable
data class TodoItem(val id: String, val text: String, val done: Boolean = false)

@Serializable
data class TodoList(val id: String, val name: String, val items: List<TodoItem> = emptyList())

@Serializable
data class Note(val id: String, val title: String, val body: String = "")

@Serializable
data class Tracker(
    val id: String,
    val name: String,
    val unit: String = "",
    val value: Int = 0,
    val goal: Int = 8,
    val step: Int = 1,
    val resetDaily: Boolean = true,
    val day: String = ""
)

@Serializable
data class Countdown(val id: String, val name: String, val date: String)

@Serializable
data class AppData(
    val todoLists: List<TodoList> = emptyList(),
    val notes: List<Note> = emptyList(),
    val trackers: List<Tracker> = emptyList(),
    val countdowns: List<Countdown> = emptyList()
)

fun newId(): String = UUID.randomUUID().toString().take(8)

fun todayString(): String = LocalDate.now().toString()

fun Tracker.current(): Int = if (resetDaily && day != todayString()) 0 else value

fun Countdown.daysLeft(): Int? = runCatching {
    ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date)).toInt()
}.getOrNull()

/** Content shown inside to-do / note / tracker / countdown elements. */
object AppDataStore {
    private const val PREFS = "app_data"
    private const val KEY = "data"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): AppData {
        val raw = prefs(context).getString(KEY, null)
        val parsed = raw?.let { runCatching { json.decodeFromString(AppData.serializer(), it) }.getOrNull() }
        if (parsed != null) return parsed
        val seeded = defaults()
        save(context, seeded)
        return seeded
    }

    fun save(context: Context, data: AppData) {
        prefs(context).edit().putString(KEY, json.encodeToString(AppData.serializer(), data)).apply()
    }

    fun toggleTodo(context: Context, listId: String, itemId: String) {
        val d = load(context)
        save(context, d.copy(todoLists = d.todoLists.map { list ->
            if (list.id != listId) list
            else list.copy(items = list.items.map { i -> if (i.id == itemId) i.copy(done = !i.done) else i })
        }))
    }

    fun stepTracker(context: Context, trackerId: String, delta: Int) {
        val d = load(context)
        save(context, d.copy(trackers = d.trackers.map { t ->
            if (t.id != trackerId) t
            else t.copy(value = (t.current() + delta * t.step).coerceAtLeast(0), day = todayString())
        }))
    }

    private fun defaults() = AppData(
        todoLists = listOf(
            TodoList(
                id = "todo-main", name = "To-do",
                items = listOf(
                    TodoItem("i1", "Plan the day"),
                    TodoItem("i2", "Reply to emails"),
                    TodoItem("i3", "Workout"),
                    TodoItem("i4", "Read 20 pages")
                )
            )
        ),
        notes = listOf(Note("note-main", "Quick note", "Tap Data in the app to edit this note. Widgets update instantly.")),
        trackers = listOf(
            Tracker("tracker-water", "Water", unit = "cups", goal = 8, step = 1),
            Tracker("tracker-study", "Study", unit = "h", goal = 4, step = 1)
        ),
        countdowns = listOf(Countdown("countdown-main", "Exam", LocalDate.now().plusDays(30).toString()))
    )
}
