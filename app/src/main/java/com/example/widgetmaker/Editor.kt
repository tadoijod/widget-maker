package com.example.widgetmaker

import android.graphics.Color as AColor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val swatches = listOf(
    "#FFFFFF", "#000000", "#1E1E2E", "#A6E3A1", "#89B4FA",
    "#F9E2AF", "#F38BA8", "#FAB387", "#CBA6F7", "#F5E0DC"
)
private val elementTypes = listOf("text", "clock", "date", "battery", "progress", "todo", "note", "tracker", "countdown")
private val blockTypes = setOf("todo", "note", "tracker", "countdown")
private val clockFormats = listOf("HH:mm", "h:mm", "h:mm a", "hh:mm")
private val dateFormats = listOf("EEE, d MMM", "EEEE", "d MMMM yyyy", "dd/MM/yyyy")

internal fun colorOf(hex: String): Color = Color(parseColorOr(hex))

private fun validColor(hex: String): Boolean =
    runCatching { AColor.parseColor(hex) }.isSuccess

private fun formatNow(pattern: String): String =
    runCatching { SimpleDateFormat(pattern, Locale.getDefault()).format(Date()) }.getOrDefault("?")

// ---------------------------------------------------------------------------------------------
// Preview (approximation of the Glance rendering)
// ---------------------------------------------------------------------------------------------

@Composable
fun DesignPreview(
    design: WidgetDesign,
    modifier: Modifier = Modifier,
    selected: Int = -1,
    interactive: Boolean = false,
    onSelect: (Int) -> Unit = {},
    onMove: (Int, Int, Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val data = remember { AppDataStore.load(context) }
    val latestSelect by rememberUpdatedState(onSelect)
    val latestMove by rememberUpdatedState(onMove)

    val bg = design.background
    val c1 = colorOf(bg.color)
    val c2 = bg.color2?.let { colorOf(it) } ?: c1
    val brush = when (bg.direction) {
        "horizontal" -> Brush.horizontalGradient(listOf(c1, c2))
        "diagonal" -> Brush.linearGradient(listOf(c1, c2))
        else -> Brush.verticalGradient(listOf(c1, c2))
    }
    val shape = RoundedCornerShape(design.cornerRadiusDp.dp)
    var frame: Modifier = modifier
        .size(design.widthDp.dp, design.heightDp.dp)
        .clip(shape)
        .background(brush, alpha = bg.opacity.coerceIn(0, 100) / 100f)
    if (bg.borderDp > 0) {
        frame = frame.border(bg.borderDp.dp, colorOf(bg.borderColor), shape)
    }

    Box(modifier = frame) {
        design.elements.forEachIndexed { i, el ->
            var m: Modifier = Modifier.offset(x = el.xDp.dp, y = el.yDp.dp)
            if (interactive) {
                if (i == selected) {
                    m = m.border(1.dp, Color(0xFF2196F3))
                }
                m = m
                    .pointerInput(i) {
                        var accX = 0f
                        var accY = 0f
                        detectDragGestures(
                            onDragStart = {
                                accX = 0f
                                accY = 0f
                                latestSelect(i)
                            }
                        ) { change, drag ->
                            change.consume()
                            accX += drag.x
                            accY += drag.y
                            val dx = (accX / density).toInt()
                            val dy = (accY / density).toInt()
                            if (dx != 0 || dy != 0) {
                                latestMove(i, dx, dy)
                                accX -= dx * density
                                accY -= dy * density
                            }
                        }
                    }
                    .pointerInput(i) { detectTapGestures { latestSelect(i) } }
            }
            Box(modifier = m) { ElementContent(design, el, data) }
        }
    }
}

@Composable
private fun ElementContent(design: WidgetDesign, el: Element, data: AppData) {
    val color = colorOf(el.style.color)
    val ts = TextStyle(
        color = color,
        fontSize = el.style.sizeSp.sp,
        fontWeight = if (el.style.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (el.italic) FontStyle.Italic else FontStyle.Normal,
        textAlign = when (el.align) {
            "center" -> TextAlign.Center
            "end" -> TextAlign.End
            else -> TextAlign.Start
        },
        textDecoration = if (el.underline) TextDecoration.Underline else TextDecoration.None
    )

    var m: Modifier = Modifier
    el.bgColor?.let { m = m.background(colorOf(it), RoundedCornerShape(el.radiusDp.dp)) }
    m = m.padding(el.padDp.dp)
    val w: Int? = el.widthDp
        ?: if (el.type in blockTypes) (design.widthDp - el.xDp - 10).coerceAtLeast(40) else null
    if (w != null) m = m.width(w.dp)

    when (el.type) {
        "clock" -> Text(
            formatNow(el.format?.takeIf { it.isNotBlank() } ?: "HH:mm"),
            modifier = m, style = ts
        )
        "date" -> Text(
            formatNow(el.format?.takeIf { it.isNotBlank() } ?: "EEE, d MMM"),
            modifier = m, style = ts
        )
        "battery" -> Text("85%", modifier = m, style = ts)
        "progress" -> {
            val t = data.trackers.firstOrNull { it.id == el.source }
            val fraction = if (t != null) {
                (t.current().toFloat() / t.goal.coerceAtLeast(1)).coerceIn(0f, 1f)
            } else {
                0.85f
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = m,
                color = color,
                trackColor = Color(0x33FFFFFF)
            )
        }
        "todo" -> {
            val list = data.todoLists.firstOrNull { it.id == el.source }
            Column(modifier = m) {
                if (list == null) {
                    Text("Pick a list", style = ts)
                } else {
                    val items = (list.items.filter { !it.done } + list.items.filter { it.done })
                        .take(el.lines.coerceAtLeast(1))
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (item.done) "●" else "○", style = ts)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                item.text,
                                maxLines = 1,
                                style = if (item.done) {
                                    ts.copy(
                                        color = color.copy(alpha = 0.5f),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                } else ts
                            )
                        }
                    }
                }
            }
        }
        "note" -> {
            val note = data.notes.firstOrNull { it.id == el.source }
            Text(
                note?.body ?: "Pick a note",
                modifier = m,
                style = ts,
                maxLines = el.lines.coerceAtLeast(1)
            )
        }
        "tracker" -> {
            val t = data.trackers.firstOrNull { it.id == el.source }
            if (t == null) {
                Text("Pick a tracker", modifier = m, style = ts)
            } else {
                val unit = if (t.unit.isBlank()) "" else " " + t.unit
                Row(modifier = m, verticalAlignment = Alignment.CenterVertically) {
                    Text("-", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = ts)
                    Text("${t.current()}/${t.goal}$unit", style = ts)
                    Text("+", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = ts)
                }
            }
        }
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
            Text(label, modifier = m, style = ts)
        }
        else -> Text(el.text.orEmpty(), modifier = m, style = ts)
    }
}

// ---------------------------------------------------------------------------------------------
// Editor
// ---------------------------------------------------------------------------------------------

@Composable
fun DesignEditor(
    initial: WidgetDesign,
    onSave: (WidgetDesign) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val data = remember { AppDataStore.load(context) }
    var design by remember { mutableStateOf(initial) }
    var selected by remember { mutableIntStateOf(if (initial.elements.isEmpty()) -1 else 0) }
    var message by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current

    BackHandler { onCancel() }

    fun updateElement(index: Int, change: (Element) -> Element) {
        if (index !in design.elements.indices) return
        design = design.copy(
            elements = design.elements.mapIndexed { i, e -> if (i == index) change(e) else e }
        )
    }

    fun defaultSource(type: String): String? = when (type) {
        "todo" -> data.todoLists.firstOrNull()?.id
        "note" -> data.notes.firstOrNull()?.id
        "tracker" -> data.trackers.firstOrNull()?.id
        "countdown" -> data.countdowns.firstOrNull()?.id
        else -> null
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Text("Edit design", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { onSave(design) }) { Text("Save") }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DesignPreview(
                design = design,
                selected = selected,
                interactive = true,
                onSelect = { selected = it },
                onMove = { i, dx, dy ->
                    updateElement(i) {
                        it.copy(
                            xDp = (it.xDp + dx).coerceAtLeast(0),
                            yDp = (it.yDp + dy).coerceAtLeast(0)
                        )
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Tap an element in the preview to select it, drag to move it.",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                elementTypes.forEach { type ->
                    FilledTonalButton(onClick = {
                        val e = Element(
                            type = type,
                            text = if (type == "text") "Hello" else null,
                            source = defaultSource(type),
                            xDp = 16,
                            yDp = 16,
                            widthDp = if (type == "progress") 150 else null,
                            style = Style(sizeSp = 20)
                        )
                        design = design.copy(elements = design.elements + e)
                        selected = design.elements.size - 1
                    }) { Text("+ $type") }
                }
            }

            val el = design.elements.getOrNull(selected)
            if (el != null) {
                ElementPanel(
                    el = el,
                    data = data,
                    designWidth = design.widthDp,
                    designHeight = design.heightDp,
                    update = { change -> updateElement(selected, change) },
                    onDelete = {
                        val removeAt = selected
                        design = design.copy(
                            elements = design.elements.filterIndexed { i, _ -> i != removeAt }
                        )
                        selected = -1
                    }
                )
            }

            DesignPanel(design = design, change = { design = it })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    clipboard.setText(AnnotatedString(DesignStore.toJson(design)))
                    message = "Design JSON copied."
                }) { Text("Copy JSON") }
                OutlinedButton(onClick = {
                    val raw = clipboard.getText()?.text.orEmpty()
                    val result = DesignStore.parse(raw)
                    if (result.isSuccess) {
                        design = result.getOrThrow().copy(id = design.id)
                        selected = -1
                        message = "Design pasted."
                    } else {
                        message = "Clipboard doesn't hold a valid design."
                    }
                }) { Text("Paste JSON") }
            }
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ElementPanel(
    el: Element,
    data: AppData,
    designWidth: Int,
    designHeight: Int,
    update: ((Element) -> Element) -> Unit,
    onDelete: () -> Unit
) {
    val textual = el.type != "progress" && el.type != "clock" && el.type != "date"
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Selected element", style = MaterialTheme.typography.titleSmall)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            elementTypes.forEach { t ->
                FilterChip(
                    selected = el.type == t,
                    onClick = {
                        update {
                            it.copy(
                                type = t,
                                text = if (t == "text" && it.text == null) "Text" else it.text,
                                source = null
                            )
                        }
                    },
                    label = { Text(t) }
                )
            }
        }

        if (el.type == "text") {
            OutlinedTextField(
                value = el.text.orEmpty(),
                onValueChange = { v -> update { it.copy(text = v) } },
                label = { Text("Text") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SourceChips(el, data) { id -> update { it.copy(source = id) } }

        if (el.type == "todo" || el.type == "note") {
            LabeledSlider("Lines ${el.lines}", el.lines.toFloat(), 1f..12f) { v ->
                update { it.copy(lines = v.toInt()) }
            }
        }

        if (el.type == "clock" || el.type == "date") {
            Text("Format", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (if (el.type == "clock") clockFormats else dateFormats).forEach { f ->
                    FilterChip(
                        selected = el.format == f,
                        onClick = { update { it.copy(format = f) } },
                        label = { Text(f) }
                    )
                }
            }
            OutlinedTextField(
                value = el.format.orEmpty(),
                onValueChange = { v -> update { it.copy(format = v.ifBlank { null }) } },
                label = { Text("Custom format (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (el.type != "progress") {
            LabeledSlider("Size ${el.style.sizeSp} sp", el.style.sizeSp.toFloat(), 8f..72f) { v ->
                update { it.copy(style = it.style.copy(sizeSp = v.toInt())) }
            }
            SwitchRow("Bold", el.style.bold) { b -> update { it.copy(style = it.style.copy(bold = b)) } }
        }

        if (textual) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("start" to "Left", "center" to "Center", "end" to "Right").forEach { (key, label) ->
                    FilterChip(
                        selected = el.align == key,
                        onClick = { update { it.copy(align = key) } },
                        label = { Text(label) }
                    )
                }
            }
            SwitchRow("Italic", el.italic) { b -> update { it.copy(italic = b) } }
            SwitchRow("Underline", el.underline) { b -> update { it.copy(underline = b) } }
        }

        if (el.type != "clock" && el.type != "date" && el.type != "battery") {
            LabeledSlider(
                "Width ${el.widthDp ?: 0} dp (0 = automatic)",
                (el.widthDp ?: 0).toFloat(),
                0f..300f
            ) { v -> update { it.copy(widthDp = if (v.toInt() == 0) null else v.toInt()) } }
        }

        LabeledSlider("X position ${el.xDp} dp", el.xDp.toFloat(), 0f..designWidth.toFloat()) { v ->
            update { it.copy(xDp = v.toInt()) }
        }
        LabeledSlider("Y position ${el.yDp} dp", el.yDp.toFloat(), 0f..designHeight.toFloat()) { v ->
            update { it.copy(yDp = v.toInt()) }
        }

        ColorField("Color", el.style.color) { hex -> update { it.copy(style = it.style.copy(color = hex)) } }

        SwitchRow("Background chip", el.bgColor != null) { on ->
            update {
                it.copy(
                    bgColor = if (on) "#33FFFFFF" else null,
                    padDp = if (on && it.padDp == 0) 6 else it.padDp,
                    radiusDp = if (on && it.radiusDp == 0) 8 else it.radiusDp
                )
            }
        }
        val chip = el.bgColor
        if (chip != null) {
            ColorField("Chip color (8-digit hex adds transparency)", chip) { hex -> update { it.copy(bgColor = hex) } }
            LabeledSlider("Chip padding ${el.padDp} dp", el.padDp.toFloat(), 0f..24f) { v ->
                update { it.copy(padDp = v.toInt()) }
            }
            LabeledSlider("Chip corner ${el.radiusDp} dp", el.radiusDp.toFloat(), 0f..24f) { v ->
                update { it.copy(radiusDp = v.toInt()) }
            }
        }

        OutlinedTextField(
            value = el.action?.value.orEmpty(),
            onValueChange = { v ->
                update { it.copy(action = if (v.isBlank()) null else Action("openUrl", v)) }
            },
            label = { Text("Tap opens link (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
            Text("Delete this element")
        }
    }
}

@Composable
private fun SourceChips(el: Element, data: AppData, onPick: (String?) -> Unit) {
    val options: List<Pair<String, String>> = when (el.type) {
        "todo" -> data.todoLists.map { it.id to it.name }
        "note" -> data.notes.map { it.id to it.title }
        "tracker", "progress" -> data.trackers.map { it.id to it.name }
        "countdown" -> data.countdowns.map { it.id to it.name }
        else -> return
    }
    Text(
        if (options.isEmpty()) "Nothing to show yet: create one in the Data tab." else "Show which one?",
        style = MaterialTheme.typography.bodySmall
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (el.type == "progress") {
            FilterChip(
                selected = el.source == null,
                onClick = { onPick(null) },
                label = { Text("Battery") }
            )
        }
        options.forEach { (id, name) ->
            FilterChip(
                selected = el.source == id,
                onClick = { onPick(id) },
                label = { Text(name) }
            )
        }
    }
}

@Composable
private fun DesignPanel(design: WidgetDesign, change: (WidgetDesign) -> Unit) {
    val bg = design.background
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Widget settings", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = design.name,
            onValueChange = { change(design.copy(name = it)) },
            label = { Text("Design name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Widget size (grid cells)", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WidgetSize.entries.forEach { s ->
                FilterChip(
                    selected = design.widthDp == s.widthDp && design.heightDp == s.heightDp,
                    onClick = { change(design.copy(widthDp = s.widthDp, heightDp = s.heightDp)) },
                    label = { Text(s.label) }
                )
            }
        }

        LabeledSlider("Corner radius ${design.cornerRadiusDp} dp", design.cornerRadiusDp.toFloat(), 0f..48f) { v ->
            change(design.copy(cornerRadiusDp = v.toInt()))
        }

        ColorField("Background color", bg.color) { hex -> change(design.copy(background = bg.copy(color = hex))) }

        SwitchRow("Gradient", bg.color2 != null) { on ->
            change(design.copy(background = bg.copy(color2 = if (on) "#45475A" else null)))
        }
        val end = bg.color2
        if (end != null) {
            ColorField("Gradient end color", end) { hex -> change(design.copy(background = bg.copy(color2 = hex))) }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("vertical", "horizontal", "diagonal").forEach { d ->
                    FilterChip(
                        selected = bg.direction == d,
                        onClick = { change(design.copy(background = bg.copy(direction = d))) },
                        label = { Text(d) }
                    )
                }
            }
        }

        LabeledSlider("Background opacity ${bg.opacity}%", bg.opacity.toFloat(), 0f..100f) { v ->
            change(design.copy(background = bg.copy(opacity = v.toInt())))
        }

        LabeledSlider("Border ${bg.borderDp} dp", bg.borderDp.toFloat(), 0f..8f) { v ->
            change(design.copy(background = bg.copy(borderDp = v.toInt())))
        }
        if (bg.borderDp > 0) {
            ColorField("Border color", bg.borderColor) { hex ->
                change(design.copy(background = bg.copy(borderColor = hex)))
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range
        )
    }
}

@Composable
private fun ColorField(label: String, hex: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            swatches.forEach { c ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorOf(c))
                        .border(1.dp, Color.Gray, CircleShape)
                        .clickable { onChange(c) }
                )
            }
        }
        OutlinedTextField(
            value = hex,
            onValueChange = onChange,
            label = { Text("Hex color, e.g. #FF8800") },
            singleLine = true,
            isError = !validColor(hex),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
