# Widget Maker

Android app (Kotlin, Jetpack Compose, Jetpack Glance) that lets you edit a widget
design as JSON and show it as a home-screen widget.

- `app/src/main/java/.../Models.kt`: design data model + storage
- `Templates.kt`: built-in templates
- `DesignWidget.kt`: the Glance renderer (JSON -> widget)
- `MainActivity.kt`: editor screen
- `WidgetUpdateWorker.kt`: 15-minute refresh

Every push to `main` builds a debug APK and publishes it under Releases > "Latest build".
