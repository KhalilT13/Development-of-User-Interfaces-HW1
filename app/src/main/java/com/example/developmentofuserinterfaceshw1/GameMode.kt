package com.example.developmentofuserinterfaceshw1

enum class GameMode(
    val label: String,
    val delayMillis: Long,
    val usesSensors: Boolean
) {
    BUTTON_SLOW("Button mode - slow", 520L, false),
    BUTTON_FAST("Button mode - fast", 260L, false),
    SENSOR("Sensor mode", 360L, true);

    companion object {
        fun fromName(name: String?): GameMode {
            return values().firstOrNull { it.name == name } ?: BUTTON_SLOW
        }
    }
}
