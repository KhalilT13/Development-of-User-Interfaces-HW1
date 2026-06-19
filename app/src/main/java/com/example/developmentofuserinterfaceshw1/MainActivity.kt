package com.example.developmentofuserinterfaceshw1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonModeSlowButton).setOnClickListener {
            startGame(GameMode.BUTTON_SLOW)
        }
        findViewById<Button>(R.id.buttonModeFastButton).setOnClickListener {
            startGame(GameMode.BUTTON_FAST)
        }
        findViewById<Button>(R.id.sensorModeButton).setOnClickListener {
            startGame(GameMode.SENSOR)
        }
        findViewById<Button>(R.id.menuHighScoresButton).setOnClickListener {
            startActivity(Intent(this, HighScoresActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startGame(mode: GameMode) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(GameActivity.EXTRA_GAME_MODE, mode.name)
        startActivity(intent)
    }
}
