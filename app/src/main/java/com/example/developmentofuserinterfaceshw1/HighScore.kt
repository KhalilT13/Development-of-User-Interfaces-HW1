package com.example.developmentofuserinterfaceshw1

data class HighScore(
    val score: Int,
    val distance: Int,
    val dateTime: String,
    val latitude: Double?,
    val longitude: Double?
) {
    val hasLocation: Boolean
        get() = latitude != null && longitude != null
}
