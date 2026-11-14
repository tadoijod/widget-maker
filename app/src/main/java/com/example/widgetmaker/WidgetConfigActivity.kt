package com.example.widgetmaker

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private suspend fun applyDesign(context: Context, appWidgetId: Int, designId: String) {
    DesignStore.assign(context, appWidgetId, designId)
    runCatching {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        DesignWidget().update(context, glanceId)
    }
}

/**
 * Shown when a widget is added from the launcher's widget list (or reconfigured).
 * When the widget was pinned from inside the app, the design is already chosen and this closes at once.
 */
class WidgetConfigActivity : ComponentActivity() {
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val finishOk: () -> Unit = {
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }

        val pending = DesignStore.takePending(this)?.let { DesignStore.get(this, it) }
        if (pending != null) {
            val context = this
            scope.launch {
                applyDesign(context, appWidgetId, pending.id)
                finishOk()
            }
            return
        }

        val size = WidgetSize.fromProvider(this, appWidgetId)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfigScreen(appWidgetId = appWidgetId, size = size, onDone = finishOk)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

@Composable
private fun ConfigScreen(appWidgetId: Int, size: WidgetSize?, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val all = remember { DesignStore.all(context) }
    val matching = remember {
        if (size == null) all else all.filter { WidgetSize.match(it.widthDp, it.heightDp) == size }
    }
    val designs = if (matching.isEmpty()) all else matching

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Choose a design", style = MaterialTheme.typography.headlineSmall)
        if (matching.isEmpty() && size != null) {
            Text(
                "You have no ${size.label} designs yet, so every design is shown. " +
                    "Create a ${size.label} design in the app for a perfect fit.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        designs.forEach { d ->
            Card(
                onClick = {
                    scope.launch {
                        applyDesign(context, appWidgetId, d.id)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(d.name, style = MaterialTheme.typography.titleMedium)
                    DesignPreview(design = d)
                }
            }
        }
    }
}
