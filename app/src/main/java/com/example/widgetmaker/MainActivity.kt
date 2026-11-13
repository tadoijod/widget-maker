package com.example.widgetmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Surface(modifier = Modifier.fillMaxSize()) { EditorScreen() }
            }
        }
    }
}

@Composable
fun EditorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jsonText by remember { mutableStateOf(DesignStore.loadRaw(context)) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Widget Maker", style = MaterialTheme.typography.headlineMedium)
        Text("1. Pick a template   2. Edit the JSON   3. Apply   4. Add to home screen")

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Templates.all.forEach { (name, template) ->
                FilledTonalButton(onClick = { jsonText = template; status = null }) {
                    Text(name)
                }
            }
        }

        OutlinedTextField(
            value = jsonText,
            onValueChange = { jsonText = it; status = null },
            modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            label = { Text("Design JSON") }
        )

        Button(
            onClick = {
                val result = DesignStore.parse(jsonText)
                if (result.isSuccess) {
                    DesignStore.saveRaw(context, jsonText)
                    scope.launch {
                        DesignWidget().updateAll(context)
                        status = "Applied to all widgets."
                    }
                } else {
                    status = "Invalid design: ${result.exceptionOrNull()?.message}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Apply to widgets") }

        OutlinedButton(
            onClick = {
                scope.launch {
                    val ok = GlanceAppWidgetManager(context)
                        .requestPinGlanceAppWidget(DesignWidgetReceiver::class.java)
                    status = if (ok) {
                        "Confirm in the prompt to add the widget."
                    } else {
                        "Your launcher can't pin widgets. Long-press the home screen > Widgets > Widget Maker."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add widget to home screen") }

        status?.let { Text(it) }

        Text(
            "Element types: text, clock, date, battery, progress. " +
                "Positions (xDp, yDp) are measured from the top-left corner. " +
                "Colors are hex like #FF8800.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
