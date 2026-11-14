package com.example.widgetmaker

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll

val ListKey = ActionParameters.Key<String>("list")
val ItemKey = ActionParameters.Key<String>("item")
val TrackerKey = ActionParameters.Key<String>("tracker")
val DeltaKey = ActionParameters.Key<Int>("delta")

/** Tapping a to-do row on the widget toggles it and refreshes every widget at once. */
class ToggleTodoAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val list = parameters[ListKey] ?: return
        val item = parameters[ItemKey] ?: return
        AppDataStore.toggleTodo(context, list, item)
        DesignWidget().updateAll(context)
    }
}

/** Tapping + / - on a tracker changes its value and refreshes every widget at once. */
class StepTrackerAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[TrackerKey] ?: return
        val delta = parameters[DeltaKey] ?: return
        AppDataStore.stepTracker(context, id, delta)
        DesignWidget().updateAll(context)
    }
}
