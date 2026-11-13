package com.example.widgetmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WidgetUpdateWorker.schedule(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { App() }
            }
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var designs by remember { mutableStateOf(DesignStore.all(context)) }
    var editing by remember { mutableStateOf<WidgetDesign?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    val current = editing
    if (current != null) {
        DesignEditor(
            initial = current,
            onSave = { d ->
                DesignStore.save(context, d)
                designs = DesignStore.all(context)
                editing = null
                scope.launch { DesignWidget().updateAll(context) }
            },
            onCancel = { editing = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Widget Maker", style = MaterialTheme.typography.headlineMedium)
        Text("Create designs here, then add a widget to your home screen and pick one.")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { editing = DesignStore.newDesign() }) { Text("New design") }
            OutlinedButton(onClick = {
                scope.launch {
                    val ok = GlanceAppWidgetManager(context)
                        .requestPinGlanceAppWidget(DesignWidgetReceiver::class.java)
                    status = if (ok) {
                        "Confirm the prompt, then choose a design."
                    } else {
                        "Your launcher can't pin widgets. Long-press the home screen > Widgets > Widget Maker."
                    }
                }
            }) { Text("Add widget") }
        }

        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        designs.forEach { d ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(d.name, style = MaterialTheme.typography.titleMedium)
                    DesignPreview(design = d)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { editing = d }) { Text("Edit") }
                        OutlinedButton(onClick = {
                            val copy = d.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                name = d.name + " copy"
                            )
                            DesignStore.save(context, copy)
                            designs = DesignStore.all(context)
                        }) { Text("Duplicate") }
                        if (designs.size > 1) {
                            OutlinedButton(onClick = {
                                DesignStore.delete(context, d.id)
                                designs = DesignStore.all(context)
                                scope.launch { DesignWidget().updateAll(context) }
                            }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
