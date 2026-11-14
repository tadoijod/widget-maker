package com.example.widgetmaker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Edit the content behind to-do, note, tracker and countdown elements. Saves as you type. */
@Composable
fun DataTab() {
    val context = LocalContext.current
    var data by remember { mutableStateOf(AppDataStore.load(context)) }
    val latest by rememberUpdatedState(data)

    // Debounced save + instant refresh of every widget.
    LaunchedEffect(data) {
        delay(300)
        AppDataStore.save(context, data)
        DesignWidget().updateAll(context)
    }
    // Make sure the last edit is never lost when leaving the tab quickly.
    DisposableEffect(Unit) {
        onDispose {
            AppDataStore.save(context, latest)
            CoroutineScope(Dispatchers.Default).launch { DesignWidget().updateAll(context) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Changes appear on your widgets instantly.", style = MaterialTheme.typography.bodySmall)

        Text("To-do lists", style = MaterialTheme.typography.titleMedium)
        data.todoLists.forEach { list ->
            TodoListEditor(
                list = list,
                onChange = { nl -> data = data.copy(todoLists = data.todoLists.map { if (it.id == list.id) nl else it }) },
                onDelete = { data = data.copy(todoLists = data.todoLists.filter { it.id != list.id }) }
            )
        }
        OutlinedButton(onClick = {
            data = data.copy(todoLists = data.todoLists + TodoList(newId(), "New list"))
        }) { Text("Add list") }

        Text("Notes", style = MaterialTheme.typography.titleMedium)
        data.notes.forEach { note ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = note.title,
                        onValueChange = { v -> data = data.copy(notes = data.notes.map { if (it.id == note.id) it.copy(title = v) else it }) },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note.body,
                        onValueChange = { v -> data = data.copy(notes = data.notes.map { if (it.id == note.id) it.copy(body = v) else it }) },
                        label = { Text("Note") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = { data = data.copy(notes = data.notes.filter { it.id != note.id }) }) {
                        Text("Delete note")
                    }
                }
            }
        }
        OutlinedButton(onClick = {
            data = data.copy(notes = data.notes + Note(newId(), "New note", ""))
        }) { Text("Add note") }

        Text("Trackers (water, study, habits, pages...)", style = MaterialTheme.typography.titleMedium)
        data.trackers.forEach { t ->
            TrackerEditor(
                t = t,
                onChange = { nt -> data = data.copy(trackers = data.trackers.map { if (it.id == t.id) nt else it }) },
                onDelete = { data = data.copy(trackers = data.trackers.filter { it.id != t.id }) }
            )
        }
        OutlinedButton(onClick = {
            data = data.copy(trackers = data.trackers + Tracker(newId(), "New tracker", "", 0, 8, 1))
        }) { Text("Add tracker") }

        Text("Countdowns (exam, deadline, trip...)", style = MaterialTheme.typography.titleMedium)
        data.countdowns.forEach { c ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = c.name,
                        onValueChange = { v -> data = data.copy(countdowns = data.countdowns.map { if (it.id == c.id) it.copy(name = v) else it }) },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = c.date,
                        onValueChange = { v -> data = data.copy(countdowns = data.countdowns.map { if (it.id == c.id) it.copy(date = v) else it }) },
                        label = { Text("Date (yyyy-mm-dd)") },
                        singleLine = true,
                        isError = c.daysLeft() == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = { data = data.copy(countdowns = data.countdowns.filter { it.id != c.id }) }) {
                        Text("Delete countdown")
                    }
                }
            }
        }
        OutlinedButton(onClick = {
            data = data.copy(
                countdowns = data.countdowns + Countdown(newId(), "New countdown", LocalDate.now().plusDays(7).toString())
            )
        }) { Text("Add countdown") }
    }
}

@Composable
private fun TodoListEditor(list: TodoList, onChange: (TodoList) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = list.name,
                onValueChange = { onChange(list.copy(name = it)) },
                label = { Text("List name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            list.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.done,
                        onCheckedChange = { b ->
                            onChange(list.copy(items = list.items.map { if (it.id == item.id) it.copy(done = b) else it }))
                        }
                    )
                    OutlinedTextField(
                        value = item.text,
                        onValueChange = { v ->
                            onChange(list.copy(items = list.items.map { if (it.id == item.id) it.copy(text = v) else it }))
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onChange(list.copy(items = list.items.filter { it.id != item.id })) }) {
                        Text("X")
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onChange(list.copy(items = list.items + TodoItem(newId(), ""))) }) {
                    Text("Add item")
                }
                TextButton(onClick = {
                    onChange(list.copy(items = list.items.filter { !it.done }))
                }) { Text("Clear done") }
                TextButton(onClick = onDelete) { Text("Delete list") }
            }
        }
    }
}

@Composable
private fun TrackerEditor(t: Tracker, onChange: (Tracker) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = t.name,
                onValueChange = { onChange(t.copy(name = it)) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = t.unit,
                    onValueChange = { onChange(t.copy(unit = it.take(8))) },
                    label = { Text("Unit") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = t.goal.toString(),
                    onValueChange = { v -> onChange(t.copy(goal = v.filter { it.isDigit() }.take(6).toIntOrNull() ?: 0)) },
                    label = { Text("Goal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = t.step.toString(),
                    onValueChange = { v -> onChange(t.copy(step = v.filter { it.isDigit() }.take(5).toIntOrNull() ?: 0)) },
                    label = { Text("Step") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reset every day")
                Switch(
                    checked = t.resetDaily,
                    onCheckedChange = { onChange(t.copy(resetDaily = it)) },
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    onChange(t.copy(value = (t.current() - t.step).coerceAtLeast(0), day = todayString()))
                }) { Text("-") }
                Text("Now: ${t.current()}/${t.goal} ${t.unit}")
                OutlinedButton(onClick = {
                    onChange(t.copy(value = t.current() + t.step, day = todayString()))
                }) { Text("+") }
                TextButton(onClick = { onChange(t.copy(value = 0, day = todayString())) }) { Text("Reset") }
            }
            TextButton(onClick = onDelete) { Text("Delete tracker") }
        }
    }
}
