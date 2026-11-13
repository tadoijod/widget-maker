# Widget Maker

Android app (Kotlin, Jetpack Compose, Jetpack Glance) for designing home-screen widgets.

- `Models.kt`: design model + design library storage (per-widget assignment)
- `Templates.kt`: starter designs
- `Editor.kt`: visual editor (live preview, drag to move, sliders, colors, copy/paste JSON)
- `WidgetConfigActivity.kt`: design picker shown when a widget is added
- `DesignWidget.kt`: Glance renderer (design -> widget)
- `MainActivity.kt`: design library
- `WidgetUpdateWorker.kt`: 15-minute refresh

Every push to `main` builds a debug APK and publishes it under Releases > "Latest build".
To update from chat: upload a new `project.zip` to the repo root; the workflow unpacks it.
