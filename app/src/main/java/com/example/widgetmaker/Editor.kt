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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val swatches = listOf(
    "#FFFFFF", "#000000", "#1E1E2E", "#A6E3A1", "#89B4FA",
    "#F9E2AF", "#F38BA8", "#FAB387", "#CBA6F7", "#F5E0DC"
)
private val elementTypes = listOf("text", "clock", "date", "battery", "progress")

internal fun colorOf(hex: String): Color =
    runCatching { Color(AColor.parseColor(hex)) }.getOrDefault(Color.White)

private fun validColor(hex: String): Boolean =
    runCatching { AColor.parseColor(hex) }.isSuccess

/** Approximate on-screen rendering of a design (the real widget is drawn by Glance). */
@Composable
fun DesignPreview(
    design: WidgetDesign,
    modifier: Modifier = Modifier,
    selected: Int = -1,
    interactive: Boolean = false,
    onSelect: (Int) -> Unit = {},
    onMove: (Int, Int, Int) -> Unit = { _, _, _ -> }
) {
    val latestSelect by rememberUpdatedState(onSelect)
    val latestMove by rememberUpdatedState(onMove)
    val clock = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val date = remember { SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date()) }

    Box(
        modifier = modifier
            .size(design.widthDp.dp, design.heightDp.dp)
            .clip(RoundedCornerShape(design.cornerRadiusDp.dp))
            .background(colorOf(design.background.color))
    ) {
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
            Box(modifier = m) { ElementContent(el, clock, date) }
        }
    }
}

@Composable
private fun ElementContent(el: Element, clock: String, date: String) {
    val color = colorOf(el.style.color)
    val widthMod: Modifier = el.widthDp?.let { Modifier.width(it.dp) } ?: Modifier
    if (el.type == "progress") {
        LinearProgressIndicator(
            progress = { 0.85f },
            modifier = widthMod,
            color = color,
            trackColor = Color(0x33FFFFFF)
        )
    } else {
        val value = when (el.type) {
            "clock" -> clock
            "date" -> date
            "battery" -> "85%"
            else -> el.text.orEmpty()
        }
        Text(
            text = value,
            modifier = widthMod,
            color = color,
            fontSize = el.style.sizeSp.sp,
            fontWeight = if (el.style.bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DesignEditor(
    initial: WidgetDesign,
    onSave: (WidgetDesign) -> Unit,
    onCancel: () -> Unit
) {
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
            Text("Tap an element in the preview to select it, drag to move it.",
                style = MaterialTheme.typography.bodySmall)

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                elementTypes.forEach { type ->
                    FilledTonalButton(onClick = {
                        val e = Element(
                            type = type,
                            text = if (type == "text") "Hello" else null,
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
                Text("Selected element", style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    elementTypes.forEach { t ->
                        FilterChip(
                            selected = el.type == t,
                            onClick = {
                                updateElement(selected) {
                                    it.copy(
                                        type = t,
                                        text = if (t == "text" && it.text == null) "Text" else it.text
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
                        onValueChange = { v -> updateElement(selected) { it.copy(text = v) } },
                        label = { Text("Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (el.type != "progress") {
                    LabeledSlider("Size ${el.style.sizeSp} sp", el.style.sizeSp.toFloat(), 8f..72f) { v ->
                        updateElement(selected) { it.copy(style = it.style.copy(sizeSp = v.toInt())) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bold")
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = el.style.bold,
                            onCheckedChange = { b ->
                                updateElement(selected) { it.copy(style = it.style.copy(bold = b)) }
                            }
                        )
                    }
                }

                if (el.type == "text" || el.type == "progress") {
                    LabeledSlider(
                        "Width ${el.widthDp ?: 0} dp (0 = automatic)",
                        (el.widthDp ?: 0).toFloat(),
                        0f..300f
                    ) { v ->
                        updateElement(selected) {
                            it.copy(widthDp = if (v.toInt() == 0) null else v.toInt())
                        }
                    }
                }

                LabeledSlider("X position ${el.xDp} dp", el.xDp.toFloat(), 0f..design.widthDp.toFloat()) { v ->
                    updateElement(selected) { it.copy(xDp = v.toInt()) }
                }
                LabeledSlider("Y position ${el.yDp} dp", el.yDp.toFloat(), 0f..design.heightDp.toFloat()) { v ->
                    updateElement(selected) { it.copy(yDp = v.toInt()) }
                }

                ColorField("Color", el.style.color) { hex ->
                    updateElement(selected) { it.copy(style = it.style.copy(color = hex)) }
                }

                OutlinedTextField(
                    value = el.action?.value.orEmpty(),
                    onValueChange = { v ->
                        updateElement(selected) {
                            it.copy(action = if (v.isBlank()) null else Action("openUrl", v))
                        }
                    },
                    label = { Text("Tap opens link (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        val removeAt = selected
                        design = design.copy(
                            elements = design.elements.filterIndexed { i, _ -> i != removeAt }
                        )
                        selected = -1
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete this element") }
            }

            Text("Widget settings", style = MaterialTheme.typography.titleSmall)

            OutlinedTextField(
                value = design.name,
                onValueChange = { design = design.copy(name = it) },
                label = { Text("Design name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("2x2" to (110 to 110), "4x2" to (250 to 110), "4x4" to (250 to 250))
                    .forEach { (label, size) ->
                        FilterChip(
                            selected = design.widthDp == size.first && design.heightDp == size.second,
                            onClick = {
                                design = design.copy(widthDp = size.first, heightDp = size.second)
                            },
                            label = { Text(label) }
                        )
                    }
            }

            LabeledSlider("Corner radius ${design.cornerRadiusDp} dp", design.cornerRadiusDp.toFloat(), 0f..48f) { v ->
                design = design.copy(cornerRadiusDp = v.toInt())
            }

            ColorField("Background", design.background.color) { hex ->
                design = design.copy(background = Background(hex))
            }

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
