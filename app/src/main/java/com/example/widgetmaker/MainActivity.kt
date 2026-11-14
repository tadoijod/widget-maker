package com.example.widgetmaker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

/** Asks the launcher to pin a widget of the right size, pre-assigned to this design. */
suspend fun pinDesign(context: Context, design: WidgetDesign): Boolean {
    DesignStore.setPending(context, design.id)
    val m = GlanceAppWidgetManager(context)
    return when (WidgetSize.match(design.widthDp, design.heightDp)) {
        WidgetSize.S2x1 -> m.requestPinGlanceAppWidget(Widget2x1Receiver::class.java)
        WidgetSize.S4x1 -> m.requestPinGlanceAppWidget(Widget4x1Receiver::class.java)
        WidgetSize.S2x2 -> m.requestPinGlanceAppWidget(Widget2x2Receiver::class.java)
        WidgetSize.S4x2 -> m.requestPinGlanceAppWidget(DesignWidgetReceiver::class.java)
        WidgetSize.S3x3 -> m.requestPinGlanceAppWidget(Widget3x3Receiver::class.java)
        WidgetSize.S4x3 -> m.requestPinGlanceAppWidget(Widget4x3Receiver::class.java)
        WidgetSize.S4x4 -> m.requestPinGlanceAppWidget(Widget4x4Receiver::class.java)
        WidgetSize.S2x4 -> m.requestPinGlanceAppWidget(Widget2x4Receiver::class.java)
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var designs by remember { mutableStateOf(DesignStore.all(context)) }
    var editing by remember { mutableStateOf<WidgetDesign?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }

    fun pin(design: WidgetDesign) {
        scope.launch {
            val ok = pinDesign(context, design)
            status = if (ok) {
                "Confirm the prompt to place the widget."
            } else {
                "Your launcher can't pin widgets. Long-press the home screen > Widgets > Widget Maker, then pick this design."
            }
        }
    }

    val current = editing
    if (current != null) {
        DesignEditor(
            initial = current,
            onSave = { d ->
                DesignStore.save(context, d)
                designs = DesignStore.all(context)
                editing = null
                tab = 0
                scope.launch { DesignWidget().updateAll(context) }
            },
            onCancel = { editing = null }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Text(
            "Widget Maker",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
        )
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("My designs") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Templates") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Data") })
        }
        when (tab) {
            0 -> DesignsTab(
                designs = designs,
                status = status,
                onNew = { editing = DesignStore.newDesign() },
                onEdit = { editing = it },
                onDuplicate = { d ->
                    DesignStore.addCopy(context, d, d.name + " copy")
                    designs = DesignStore.all(context)
                },
                onDelete = { d ->
                    DesignStore.delete(context, d.id)
                    designs = DesignStore.all(context)
                    scope.launch { DesignWidget().updateAll(context) }
                },
                onPin = { pin(it) }
            )
            1 -> TemplatesTab(
                onUse = { t ->
                    editing = DesignStore.addCopy(context, t)
                    designs = DesignStore.all(context)
                },
                onPin = { t ->
                    val added = DesignStore.addCopy(context, t)
                    designs = DesignStore.all(context)
                    pin(added)
                }
            )
            else -> DataTab()
        }
    }
}

