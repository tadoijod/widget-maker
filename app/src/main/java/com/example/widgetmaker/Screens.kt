package com.example.widgetmaker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DesignsTab(
    designs: List<WidgetDesign>,
    status: String?,
    onNew: () -> Unit,
    onEdit: (WidgetDesign) -> Unit,
    onDuplicate: (WidgetDesign) -> Unit,
    onDelete: (WidgetDesign) -> Unit,
    onPin: (WidgetDesign) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Each design can be added to your home screen as its own widget.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onNew) { Text("New design") }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        designs.forEach { d ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val size = WidgetSize.match(d.widthDp, d.heightDp)
                    Text("${d.name}  (${size.label})", style = MaterialTheme.typography.titleMedium)
                    DesignPreview(design = d)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onPin(d) }) { Text("Add to home screen") }
                        OutlinedButton(onClick = { onEdit(d) }) { Text("Edit") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onDuplicate(d) }) { Text("Duplicate") }
                        if (designs.size > 1) {
                            OutlinedButton(onClick = { onDelete(d) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesTab(
    onUse: (WidgetDesign) -> Unit,
    onPin: (WidgetDesign) -> Unit
) {
    val templates = remember { Templates.all.mapNotNull { DesignStore.parse(it.second).getOrNull() } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Ready-made templates in every size. Copy one to My designs, or add it straight to your home screen.",
            style = MaterialTheme.typography.bodyMedium
        )
        templates.forEach { t ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.name, style = MaterialTheme.typography.titleMedium)
                    DesignPreview(design = t)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onPin(t) }) { Text("Add to home screen") }
                        OutlinedButton(onClick = { onUse(t) }) { Text("Customize") }
                    }
                }
            }
        }
    }
}
