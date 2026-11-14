package com.example.widgetmaker

object Templates {
    /** Indexes (into [all]) of the designs every new install starts with. */
    val starterIndexes = listOf(0, 5, 9)

    val all: List<Pair<String, String>> = listOf(
        // 0
        "Clock (4x2)" to """
{
  "id": "t-clock-4x2", "name": "Clock (4x2)", "widthDp": 250, "heightDp": 110, "cornerRadiusDp": 24,
  "background": { "color": "#1E1E2E", "color2": "#313244", "direction": "diagonal" },
  "elements": [
    { "type": "clock", "xDp": 16, "yDp": 6, "style": { "color": "#FFFFFF", "sizeSp": 44, "bold": true } },
    { "type": "date", "xDp": 18, "yDp": 62, "style": { "color": "#A6ADC8", "sizeSp": 14 } },
    { "type": "battery", "xDp": 190, "yDp": 14, "style": { "color": "#A6E3A1", "sizeSp": 14, "bold": true } }
  ]
}
""".trimIndent(),
        // 1
        "Clock strip (4x1)" to """
{
  "id": "t-strip-4x1", "name": "Clock strip (4x1)", "widthDp": 250, "heightDp": 40, "cornerRadiusDp": 20,
  "background": { "color": "#1E1E2E" },
  "elements": [
    { "type": "clock", "xDp": 12, "yDp": 5, "style": { "color": "#FFFFFF", "sizeSp": 22, "bold": true } },
    { "type": "date", "xDp": 80, "yDp": 12, "style": { "color": "#A6ADC8", "sizeSp": 13 } },
    { "type": "battery", "xDp": 206, "yDp": 12, "style": { "color": "#A6E3A1", "sizeSp": 13, "bold": true } }
  ]
}
""".trimIndent(),
        // 2
        "Mini clock (2x2)" to """
{
  "id": "t-mini-2x2", "name": "Mini clock (2x2)", "widthDp": 110, "heightDp": 110, "cornerRadiusDp": 22,
  "background": { "color": "#11111B", "color2": "#1E1E2E", "direction": "vertical" },
  "elements": [
    { "type": "clock", "xDp": 12, "yDp": 10, "style": { "color": "#FFFFFF", "sizeSp": 32, "bold": true } },
    { "type": "date", "xDp": 14, "yDp": 56, "style": { "color": "#A6ADC8", "sizeSp": 13 } },
    { "type": "battery", "xDp": 14, "yDp": 80, "style": { "color": "#A6E3A1", "sizeSp": 13, "bold": true } }
  ]
}
""".trimIndent(),
        // 3
        "Battery card (4x2)" to """
{
  "id": "t-battery-4x2", "name": "Battery card (4x2)", "widthDp": 250, "heightDp": 110, "cornerRadiusDp": 28,
  "background": { "color": "#11111B" },
  "elements": [
    { "type": "text", "text": "BATTERY", "xDp": 18, "yDp": 14, "style": { "color": "#7F849C", "sizeSp": 12, "bold": true } },
    { "type": "battery", "xDp": 18, "yDp": 30, "style": { "color": "#A6E3A1", "sizeSp": 40, "bold": true } },
    { "type": "progress", "xDp": 18, "yDp": 88, "widthDp": 214, "style": { "color": "#A6E3A1" } }
  ]
}
""".trimIndent(),
        // 4
        "Quote (4x2)" to """
{
  "id": "t-quote-4x2", "name": "Quote (4x2)", "widthDp": 250, "heightDp": 110, "cornerRadiusDp": 20,
  "background": { "color": "#F5E0DC" },
  "elements": [
    { "type": "text", "text": "Small steps every day.", "xDp": 18, "yDp": 16, "widthDp": 210,
      "style": { "color": "#1E1E2E", "sizeSp": 22, "bold": true } },
    { "type": "date", "xDp": 18, "yDp": 80, "style": { "color": "#6C6F85", "sizeSp": 13 } }
  ]
}
""".trimIndent(),
        // 5
        "To-do list (4x4)" to """
{
  "id": "t-todo-4x4", "name": "To-do list (4x4)", "widthDp": 250, "heightDp": 250, "cornerRadiusDp": 26,
  "background": { "color": "#1E1E2E", "color2": "#313244", "direction": "vertical" },
  "elements": [
    { "type": "text", "text": "To-do", "xDp": 16, "yDp": 12, "style": { "color": "#CDD6F4", "sizeSp": 20, "bold": true } },
    { "type": "todo", "source": "todo-main", "lines": 8, "xDp": 16, "yDp": 50, "widthDp": 218,
      "style": { "color": "#CDD6F4", "sizeSp": 15 } }
  ]
}
""".trimIndent(),
        // 6
        "To-do compact (2x2)" to """
{
  "id": "t-todo-2x2", "name": "To-do compact (2x2)", "widthDp": 110, "heightDp": 110, "cornerRadiusDp": 22,
  "background": { "color": "#1E1E2E" },
  "elements": [
    { "type": "text", "text": "Today", "xDp": 12, "yDp": 8, "style": { "color": "#89B4FA", "sizeSp": 14, "bold": true } },
    { "type": "todo", "source": "todo-main", "lines": 4, "xDp": 12, "yDp": 30, "widthDp": 92,
      "style": { "color": "#CDD6F4", "sizeSp": 12 } }
  ]
}
""".trimIndent(),
        // 7
        "Sticky note (2x2)" to """
{
  "id": "t-note-2x2", "name": "Sticky note (2x2)", "widthDp": 110, "heightDp": 110, "cornerRadiusDp": 14,
  "background": { "color": "#FFF3B0" },
  "elements": [
    { "type": "note", "source": "note-main", "lines": 6, "xDp": 12, "yDp": 12, "widthDp": 88,
      "style": { "color": "#1E1E2E", "sizeSp": 13 } }
  ]
}
""".trimIndent(),
        // 8
        "Note card (4x2)" to """
{
  "id": "t-note-4x2", "name": "Note card (4x2)", "widthDp": 250, "heightDp": 110, "cornerRadiusDp": 18,
  "background": { "color": "#FFF3B0" },
  "elements": [
    { "type": "note", "source": "note-main", "lines": 4, "xDp": 16, "yDp": 14, "widthDp": 218,
      "style": { "color": "#1E1E2E", "sizeSp": 15 } }
  ]
}
""".trimIndent(),
        // 9
        "Water tracker (2x2)" to """
{
  "id": "t-water-2x2", "name": "Water tracker (2x2)", "widthDp": 110, "heightDp": 110, "cornerRadiusDp": 22,
  "background": { "color": "#0B3D5B", "color2": "#145374", "direction": "vertical" },
  "elements": [
    { "type": "text", "text": "WATER", "xDp": 12, "yDp": 10, "style": { "color": "#9AD1F5", "sizeSp": 11, "bold": true } },
    { "type": "tracker", "source": "tracker-water", "xDp": 4, "yDp": 32, "style": { "color": "#FFFFFF", "sizeSp": 13, "bold": true } },
    { "type": "progress", "source": "tracker-water", "xDp": 14, "yDp": 80, "widthDp": 82, "style": { "color": "#5BC0F8" } }
  ]
}
""".trimIndent(),
        // 10
        "Study tracker (4x2)" to """
{
  "id": "t-study-4x2", "name": "Study tracker (4x2)", "widthDp": 250, "heightDp": 110, "cornerRadiusDp": 24,
  "background": { "color": "#1E1E2E" },
  "elements": [
    { "type": "text", "text": "STUDY TODAY", "xDp": 16, "yDp": 10, "style": { "color": "#A6ADC8", "sizeSp": 12, "bold": true } },
    { "type": "tracker", "source": "tracker-study", "xDp": 8, "yDp": 28, "style": { "color": "#F9E2AF", "sizeSp": 26, "bold": true } },
    { "type": "progress", "source": "tracker-study", "xDp": 16, "yDp": 84, "widthDp": 218, "style": { "color": "#F9E2AF" } }
  ]
}
""".trimIndent(),
        // 11
        "Countdown (2x2)" to """
{
  "id": "t-countdown-2x2", "name": "Countdown (2x2)", "widthDp": 110, "heightDp": 110, "cornerRadiusDp": 22,
  "background": { "color": "#3B1D4A", "color2": "#6A2C70", "direction": "diagonal" },
  "elements": [
    { "type": "text", "text": "EXAM IN", "xDp": 12, "yDp": 12, "style": { "color": "#F4B6FF", "sizeSp": 11, "bold": true } },
    { "type": "countdown", "source": "countdown-main", "xDp": 12, "yDp": 34, "style": { "color": "#FFFFFF", "sizeSp": 26, "bold": true } }
  ]
}
""".trimIndent(),
        // 12
        "Day planner (4x4)" to """
{
  "id": "t-planner-4x4", "name": "Day planner (4x4)", "widthDp": 250, "heightDp": 250, "cornerRadiusDp": 26,
  "background": { "color": "#181825", "color2": "#1E1E2E", "direction": "vertical" },
  "elements": [
    { "type": "clock", "xDp": 16, "yDp": 8, "style": { "color": "#FFFFFF", "sizeSp": 38, "bold": true } },
    { "type": "date", "xDp": 18, "yDp": 54, "style": { "color": "#A6ADC8", "sizeSp": 14 } },
    { "type": "todo", "source": "todo-main", "lines": 5, "xDp": 16, "yDp": 86, "widthDp": 218,
      "style": { "color": "#CDD6F4", "sizeSp": 14 } },
    { "type": "tracker", "source": "tracker-water", "xDp": 8, "yDp": 200, "style": { "color": "#9AD1F5", "sizeSp": 14, "bold": true } },
    { "type": "progress", "source": "tracker-water", "xDp": 16, "yDp": 230, "widthDp": 218, "style": { "color": "#5BC0F8" } }
  ]
}
""".trimIndent(),
        // 13
        "Tasks tall (2x4)" to """
{
  "id": "t-tasks-2x4", "name": "Tasks tall (2x4)", "widthDp": 110, "heightDp": 250, "cornerRadiusDp": 22,
  "background": { "color": "#1E1E2E", "color2": "#11111B", "direction": "vertical" },
  "elements": [
    { "type": "text", "text": "Tasks", "xDp": 12, "yDp": 10, "style": { "color": "#F38BA8", "sizeSp": 16, "bold": true } },
    { "type": "todo", "source": "todo-main", "lines": 9, "xDp": 12, "yDp": 40, "widthDp": 90,
      "style": { "color": "#CDD6F4", "sizeSp": 12 } }
  ]
}
""".trimIndent()
    )
}
