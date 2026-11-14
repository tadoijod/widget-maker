# Widget Maker

Android app (Kotlin, Jetpack Compose, Jetpack Glance) for designing home-screen widgets.

- Widget sizes: 2x1, 4x1, 2x2, 4x2, 3x3, 4x3, 4x4, 2x4 (one launcher widget entry per size)
- Element types: text, live clock, live date, battery, progress, to-do list, note, tracker, countdown
- `Models.kt`: design model + library, per-widget assignment, widget sizes
- `Data.kt`: to-do lists, notes, trackers, countdowns (shown inside widgets)
- `Actions.kt`: taps on widgets (toggle to-do, +/- trackers) update everything instantly
- `DesignWidget.kt`: Glance renderer; `Background.kt`: gradient/opacity/border bitmap + native TextClock
- `Editor.kt`: visual editor; `Screens.kt`, `DataScreen.kt`, `MainActivity.kt`: app UI
- `WidgetConfigActivity.kt`: design picker when a widget is added from the launcher
- `Templates.kt`: ready-made templates

Every push to `main` builds a debug APK and publishes it under Releases > "Latest build".
To update from chat: upload a new `project.zip` to the repo root; the workflow unpacks it.
