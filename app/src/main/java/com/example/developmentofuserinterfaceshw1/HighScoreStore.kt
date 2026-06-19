package com.example.developmentofuserinterfaceshw1

import android.content.Context
import android.location.Location
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HighScoreStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveScore(score: Int, distance: Int, location: Location?) {
        val newScore = HighScore(
            score = score,
            distance = distance,
            dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            latitude = location?.latitude,
            longitude = location?.longitude
        )

        val topScores = (getScores() + newScore)
            .sortedWith(
                compareByDescending<HighScore> { it.score }
                    .thenByDescending { it.distance }
                    .thenByDescending { it.dateTime }
            )
            .take(MAX_SCORES)

        prefs.edit().putString(KEY_SCORES, toJson(topScores).toString()).apply()
    }

    fun getScores(): List<HighScore> {
        val rawScores = prefs.getString(KEY_SCORES, "[]") ?: "[]"
        return try {
            val array = JSONArray(rawScores)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        HighScore(
                            score = item.optInt("score"),
                            distance = item.optInt("distance"),
                            dateTime = item.optString("dateTime"),
                            latitude = if (item.isNull("latitude")) null else item.optDouble("latitude"),
                            longitude = if (item.isNull("longitude")) null else item.optDouble("longitude")
                        )
                    )
                }
            }
        } catch (exception: Exception) {
            emptyList()
        }
    }

    private fun toJson(scores: List<HighScore>): JSONArray {
        val array = JSONArray()
        scores.forEach { score ->
            array.put(
                JSONObject().apply {
                    put("score", score.score)
                    put("distance", score.distance)
                    put("dateTime", score.dateTime)
                    put("latitude", score.latitude ?: JSONObject.NULL)
                    put("longitude", score.longitude ?: JSONObject.NULL)
                }
            )
        }
        return array
    }

    companion object {
        private const val PREFS_NAME = "road_runner_high_scores"
        private const val KEY_SCORES = "scores"
        private const val MAX_SCORES = 10
    }
}
