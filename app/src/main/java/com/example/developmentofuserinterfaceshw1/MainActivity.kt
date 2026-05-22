package com.example.developmentofuserinterfaceshw1

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val rows = 8
    private val lanes = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val gameMatrix = findViewById<LinearLayout>(R.id.gameMatrix)
        val leftButton = findViewById<ImageButton>(R.id.leftButton)
        val rightButton = findViewById<ImageButton>(R.id.rightButton)
        val life1 = findViewById<ImageView>(R.id.life1)
        val life2 = findViewById<ImageView>(R.id.life2)
        val life3 = findViewById<ImageView>(R.id.life3)
        val gameOverText = findViewById<TextView>(R.id.gameOverText)
        val countdownText = findViewById<TextView>(R.id.countdownText)
        val startButton = findViewById<Button>(R.id.startButton)
        val restartButton = findViewById<Button>(R.id.restartButton)
        val lifeImages = arrayOf(life1, life2, life3)
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        val bugCells = Array(rows) { Array<ImageView?>(lanes) { null } }
        val robotCells = Array<ImageView?>(lanes) { null }

        for (row in 0 until rows) {
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

            for (lane in 0 until lanes) {
                val cell = FrameLayout(this)
                cell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )

                val bugImage = ImageView(this)
                bugImage.setImageResource(R.drawable.red_bug_icon)
                bugImage.visibility = View.INVISIBLE
                bugImage.layoutParams = FrameLayout.LayoutParams(140, 140).apply {
                    gravity = android.view.Gravity.CENTER
                }

                cell.addView(bugImage)
                bugCells[row][lane] = bugImage

                if (row == rows - 2) {
                    val robotImage = ImageView(this)
                    robotImage.setImageResource(R.drawable.android_robot_icon)
                    robotImage.visibility = View.INVISIBLE
                    robotImage.layoutParams = FrameLayout.LayoutParams(140, 140).apply {
                        gravity = android.view.Gravity.CENTER
                    }

                    cell.addView(robotImage)
                    robotCells[lane] = robotImage
                }

                rowLayout.addView(cell)
            }

            gameMatrix.addView(rowLayout)
        }

        data class Bug(var row: Int, var lane: Int)

        val bugs = arrayOf(
            Bug(0, Random.nextInt(lanes)),
            Bug(-3, Random.nextInt(lanes)),
            Bug(-6, Random.nextInt(lanes))
        )

        var currentLane = 1
        var lives = 3
        var gameRunning = false
        val robotRow = rows - 2
        val handler = Handler(Looper.getMainLooper())
        val gameDelay = 350L

        fun showRobot() {
            for (lane in 0 until lanes) {
                robotCells[lane]?.visibility =
                    if (lane == currentLane) View.VISIBLE else View.INVISIBLE
            }
        }

        fun hideBug(bug: Bug) {
            if (bug.row in 0 until rows) {
                bugCells[bug.row][bug.lane]?.visibility = View.INVISIBLE
            }
        }

        fun showBug(bug: Bug) {
            if (bug.row in 0 until rows) {
                bugCells[bug.row][bug.lane]?.visibility = View.VISIBLE
            }
        }

        fun resetBug(bug: Bug) {
            bug.row = 0
            bug.lane = Random.nextInt(lanes)
        }

        fun resetBugsForNewGame() {
            bugs.forEach { hideBug(it) }

            bugs[0].row = 0
            bugs[0].lane = Random.nextInt(lanes)
            bugs[1].row = -3
            bugs[1].lane = Random.nextInt(lanes)
            bugs[2].row = -6
            bugs[2].lane = Random.nextInt(lanes)
        }

        fun startCountdown(onFinished: () -> Unit) {
            val countdownValues = arrayOf("3", "2", "1", "GO")

            countdownText.visibility = View.VISIBLE

            for (index in countdownValues.indices) {
                handler.postDelayed({
                    countdownText.text = countdownValues[index]

                    if (index == countdownValues.lastIndex) {
                        handler.postDelayed({
                            countdownText.visibility = View.INVISIBLE
                            onFinished()
                        }, 700)
                    }
                }, index * 700L)
            }
        }

        fun vibrateCrash() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(200)
            }
        }

        fun loseLife() {
            if (!gameRunning) {
                return
            }

            vibrateCrash()
            Toast.makeText(this, "Crash!", Toast.LENGTH_SHORT).show()

            lives--
            lifeImages[lives].visibility = View.INVISIBLE

            if (lives == 0) {
                gameRunning = false
                leftButton.isEnabled = false
                rightButton.isEnabled = false
                gameOverText.visibility = View.VISIBLE
                restartButton.visibility = View.VISIBLE
            }
        }

        fun moveBugDown(bug: Bug) {
            hideBug(bug)
            bug.row++

            if (bug.row >= rows) {
                resetBug(bug)
            }

            if (bug.row == robotRow && bug.lane == currentLane) {
                loseLife()
                resetBug(bug)
            }

            showBug(bug)
        }

        showRobot()
        leftButton.isEnabled = false
        rightButton.isEnabled = false

        val gameRunnable = object : Runnable {
            override fun run() {
                if (!gameRunning) {
                    return
                }

                bugs.forEach { moveBugDown(it) }

                if (gameRunning) {
                    handler.postDelayed(this, gameDelay)
                }
            }
        }

        leftButton.setOnClickListener {
            if (currentLane > 0) {
                currentLane--
                showRobot()
            }
        }

        rightButton.setOnClickListener {
            if (currentLane < lanes - 1) {
                currentLane++
                showRobot()
            }
        }

        startButton.setOnClickListener {
            startButton.visibility = View.INVISIBLE
            leftButton.isEnabled = false
            rightButton.isEnabled = false
            resetBugsForNewGame()

            startCountdown {
                gameRunning = true
                leftButton.isEnabled = true
                rightButton.isEnabled = true
                bugs.forEach { showBug(it) }
                handler.postDelayed(gameRunnable, gameDelay)
            }
        }

        restartButton.setOnClickListener {
            lives = 3
            gameRunning = false
            currentLane = 1

            lifeImages.forEach { it.visibility = View.VISIBLE }
            gameOverText.visibility = View.INVISIBLE
            restartButton.visibility = View.INVISIBLE
            leftButton.isEnabled = false
            rightButton.isEnabled = false

            showRobot()
            resetBugsForNewGame()

            startCountdown {
                gameRunning = true
                leftButton.isEnabled = true
                rightButton.isEnabled = true
                bugs.forEach { showBug(it) }
                handler.postDelayed(gameRunnable, gameDelay)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
