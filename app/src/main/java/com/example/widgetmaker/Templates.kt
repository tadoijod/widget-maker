package com.example.widgetmaker

object Templates {
    val all: List<Pair<String, String>> = listOf(
        "Minimal clock" to """
{
  "id": "minimal-clock",
  "name": "Minimal Clock",
  "cornerRadiusDp": 24,
  "background": { "color": "#1E1E2E" },
  "elements": [
    { "type": "clock", "xDp": 16, "yDp": 8,
      "style": { "color": "#FFFFFF", "sizeSp": 44, "bold": true } },
    { "type": "date", "xDp": 18, "yDp": 62,
      "style": { "color": "#A6ADC8", "sizeSp": 14 } },
    { "type": "battery", "xDp": 190, "yDp": 14,
      "style": { "color": "#A6E3A1", "sizeSp": 14, "bold": true } },
    { "type": "text", "text": "Open Maps", "xDp": 170, "yDp": 70,
      "style": { "color": "#89B4FA", "sizeSp": 13 },
      "action": { "type": "openUrl", "value": "https://maps.google.com" } }
  ]
}
""".trimIndent(),

        "Battery card" to """
{
  "id": "battery-card",
  "name": "Battery Card",
  "cornerRadiusDp": 28,
  "background": { "color": "#11111B" },
  "elements": [
    { "type": "text", "text": "BATTERY", "xDp": 18, "yDp": 14,
      "style": { "color": "#7F849C", "sizeSp": 12, "bold": true } },
    { "type": "battery", "xDp": 18, "yDp": 30,
      "style": { "color": "#A6E3A1", "sizeSp": 40, "bold": true } },
    { "type": "progress", "xDp": 18, "yDp": 88, "widthDp": 214,
      "style": { "color": "#A6E3A1" } }
  ]
}
""".trimIndent(),

        "Quote" to """
{
  "id": "quote",
  "name": "Quote",
  "cornerRadiusDp": 20,
  "background": { "color": "#F5E0DC" },
  "elements": [
    { "type": "text", "text": "Small steps every day.", "xDp": 18, "yDp": 16, "widthDp": 210,
      "style": { "color": "#1E1E2E", "sizeSp": 22, "bold": true } },
    { "type": "date", "xDp": 18, "yDp": 80,
      "style": { "color": "#6C6F85", "sizeSp": 13 } }
  ]
}
""".trimIndent()
    )
}
