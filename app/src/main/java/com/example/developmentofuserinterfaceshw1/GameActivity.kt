package com.example.developmentofuserinterfaceshw1

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
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

class GameActivity : AppCompatActivity() {

    private lateinit var mode: GameMode
    private lateinit var logic: GameLogic
    private lateinit var locationProvider: LocationProvider
    private lateinit var highScoreStore: HighScoreStore
    private lateinit var gameMatrix: LinearLayout
    private lateinit var leftButton: ImageButton
    private lateinit var rightButton: ImageButton
    private lateinit var lifeImages: Array<ImageView>
    private lateinit var scoreText: TextView
    private lateinit var distanceText: TextView
    private lateinit var modeText: TextView
    private lateinit var gameOverText: TextView
    private lateinit var countdownText: TextView
    private lateinit var gameOverActions: LinearLayout
    private lateinit var crashOverlay: View
    private lateinit var obstacleCells: Array<Array<ImageView?>>
    private lateinit var coinCells: Array<Array<ImageView?>>
    private lateinit var robotCells: Array<ImageView?>

    private val handler = Handler(Looper.getMainLooper())
    private val vibrator: Vibrator by lazy {
        (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    }
    private val crashTone: ToneGenerator by lazy {
        ToneGenerator(AudioManager.STREAM_MUSIC, 90)
    }

    private var tiltController: TiltController? = null
    private var gameRunning = false
    private var scoreSaved = false
    private var currentTickDelayMillis = 0L

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (!gameRunning) {
                return
            }

            handleGameResult(logic.step())

            if (gameRunning) {
                handler.postDelayed(this, currentTickDelayMillis)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        mode = GameMode.fromName(intent.getStringExtra(EXTRA_GAME_MODE))
        logic = GameLogic(rows = ROAD_ROWS, lanes = ROAD_LANES)
        locationProvider = LocationProvider(this)
        highScoreStore = HighScoreStore(this)
        currentTickDelayMillis = mode.delayMillis
        locationProvider.onPermissionGranted = {
            if (gameRunning) {
                locationProvider.startTracking()
            }
        }

        bindViews()
        buildRoad()
        setupControls()
        locationProvider.requestPermissionIfNeeded()
        resetAndStartGame()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (gameRunning) {
            locationProvider.startTracking()
            startSensorIfNeeded()
            handler.removeCallbacks(gameRunnable)
            handler.postDelayed(gameRunnable, currentTickDelayMillis)
        }
    }

    override fun onPause() {
        super.onPause()
        tiltController?.stop()
        locationProvider.stopTracking()
        handler.removeCallbacks(gameRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        tiltController?.stop()
        locationProvider.stopTracking()
        crashTone.release()
    }

    private fun bindViews() {
        gameMatrix = findViewById(R.id.gameMatrix)
        leftButton = findViewById(R.id.leftButton)
        rightButton = findViewById(R.id.rightButton)
        scoreText = findViewById(R.id.scoreText)
        distanceText = findViewById(R.id.distanceText)
        modeText = findViewById(R.id.modeText)
        gameOverText = findViewById(R.id.gameOverText)
        countdownText = findViewById(R.id.countdownText)
        gameOverActions = findViewById(R.id.gameOverActions)
        crashOverlay = findViewById(R.id.crashOverlay)
        lifeImages = arrayOf(
            findViewById(R.id.life1),
            findViewById(R.id.life2),
            findViewById(R.id.life3)
        )
        modeText.text = mode.label
    }

    private fun buildRoad() {
        obstacleCells = Array(logic.rows) { arrayOfNulls(logic.lanes) }
        coinCells = Array(logic.rows) { arrayOfNulls(logic.lanes) }
        robotCells = arrayOfNulls(logic.lanes)
        gameMatrix.removeAllViews()

        for (row in 0 until logic.rows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }

            for (lane in 0 until logic.lanes) {
                val cell = FrameLayout(this).apply {
                    background = roadCellBackground(lane)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                }

                val obstacleImage = ImageView(this).apply {
                    setImageResource(R.drawable.red_bug_icon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    visibility = View.INVISIBLE
                    layoutParams = centeredImageLayout(42, 0.72f)
                }
                val coinImage = ImageView(this).apply {
                    setImageResource(R.drawable.coin_icon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    visibility = View.INVISIBLE
                    layoutParams = centeredImageLayout(34, 0.58f)
                }

                cell.addView(coinImage)
                cell.addView(obstacleImage)
                obstacleCells[row][lane] = obstacleImage
                coinCells[row][lane] = coinImage

                if (row == logic.robotRow) {
                    val robotImage = ImageView(this).apply {
                        setImageResource(R.drawable.android_robot_icon)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        visibility = View.INVISIBLE
                        layoutParams = centeredImageLayout(46, 0.74f)
                    }
                    cell.addView(robotImage)
                    robotCells[lane] = robotImage
                }

                rowLayout.addView(cell)
            }

            gameMatrix.addView(rowLayout)
        }
    }

    private fun setupControls() {
        leftButton.setOnClickListener {
            if (gameRunning) {
                handleGameResult(logic.moveLeft())
            }
        }
        rightButton.setOnClickListener {
            if (gameRunning) {
                handleGameResult(logic.moveRight())
            }
        }

        if (mode.usesSensors) {
            leftButton.visibility = View.INVISIBLE
            rightButton.visibility = View.INVISIBLE
            tiltController = TiltController(
                context = this,
                onTiltLeft = {
                    if (gameRunning) {
                        runOnUiThread { handleGameResult(logic.moveLeft()) }
                    }
                },
                onTiltRight = {
                    if (gameRunning) {
                        runOnUiThread { handleGameResult(logic.moveRight()) }
                    }
                },
                onSpeedChanged = { speed ->
                    runOnUiThread {
                        updateSensorSpeed(speed)
                    }
                }
            )

            if (tiltController?.isAvailable != true) {
                Toast.makeText(this, "Sensor is not available on this device", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            resetAndStartGame()
        }
        findViewById<Button>(R.id.menuButton).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.highScoresButton).setOnClickListener {
            startActivity(Intent(this, HighScoresActivity::class.java))
        }
    }

    private fun resetAndStartGame() {
        handler.removeCallbacksAndMessages(null)
        logic.reset()
        scoreSaved = false
        gameRunning = false
        currentTickDelayMillis = mode.delayMillis
        if (mode.usesSensors) {
            updateSensorSpeed(SensorSpeed.NORMAL)
        } else {
            modeText.text = mode.label
        }
        gameOverText.visibility = View.INVISIBLE
        gameOverActions.visibility = View.INVISIBLE
        setButtonControlsEnabled(false)
        renderGame(animate = false)
        startCountdown {
            gameRunning = true
            setButtonControlsEnabled(!mode.usesSensors)
            locationProvider.startTracking()
            startSensorIfNeeded()
            handler.postDelayed(gameRunnable, currentTickDelayMillis)
        }
    }

    private fun startCountdown(onFinished: () -> Unit) {
        val countdownValues = arrayOf("3", "2", "1", "GO")
        countdownText.visibility = View.VISIBLE

        countdownValues.forEachIndexed { index, value ->
            handler.postDelayed({
                countdownText.text = value

                if (index == countdownValues.lastIndex) {
                    handler.postDelayed({
                        countdownText.visibility = View.INVISIBLE
                        onFinished()
                    }, COUNTDOWN_INTERVAL_MS)
                }
            }, index * COUNTDOWN_INTERVAL_MS)
        }
    }

    private fun handleGameResult(result: GameLogic.StepResult) {
        renderGame(animate = true)

        if (result.collectedCoin) {
            pulseView(scoreText)
            Toast.makeText(this, "+10", Toast.LENGTH_SHORT).show()
        }
        if (result.crashed) {
            playCrashFeedback()
        }
        if (result.gameOver) {
            finishGame()
        }
    }

    private fun finishGame() {
        gameRunning = false
        handler.removeCallbacks(gameRunnable)
        tiltController?.stop()
        locationProvider.stopTracking()
        setButtonControlsEnabled(false)
        gameOverText.visibility = View.VISIBLE
        gameOverActions.visibility = View.VISIBLE

        if (!scoreSaved) {
            scoreSaved = true
            highScoreStore.saveScore(
                score = logic.score,
                distance = logic.distance,
                location = locationProvider.getFreshLocationForScore()
            )
        }
    }

    private fun renderGame(animate: Boolean = true) {
        val visibleObstacles = Array(logic.rows) { BooleanArray(logic.lanes) }
        val visibleCoins = Array(logic.rows) { BooleanArray(logic.lanes) }

        logic.obstacles.forEach { obstacle ->
            if (obstacle.row in 0 until logic.rows) {
                visibleObstacles[obstacle.row][obstacle.lane] = true
            }
        }
        logic.coins.forEach { coin ->
            if (coin.row in 0 until logic.rows) {
                visibleCoins[coin.row][coin.lane] = true
            }
        }

        for (row in 0 until logic.rows) {
            for (lane in 0 until logic.lanes) {
                setSpriteVisible(obstacleCells[row][lane], visibleObstacles[row][lane], animate)
                setSpriteVisible(coinCells[row][lane], visibleCoins[row][lane], animate)
            }
        }

        robotCells.forEachIndexed { lane, robotImage ->
            setSpriteVisible(robotImage, lane == logic.currentLane, animate)
        }

        lifeImages.forEachIndexed { index, image ->
            image.visibility = if (index < logic.lives) View.VISIBLE else View.INVISIBLE
        }
        scoreText.text = getString(R.string.score_format, logic.score)
        distanceText.text = getString(R.string.distance_format, logic.distance)
    }

    private fun playCrashFeedback() {
        crashTone.startTone(ToneGenerator.TONE_SUP_ERROR, 220)
        vibrator.vibrate(
            VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        )
        flashCrashOverlay()
        Toast.makeText(this, "Crash!", Toast.LENGTH_SHORT).show()
    }

    private fun setButtonControlsEnabled(enabled: Boolean) {
        leftButton.isEnabled = enabled
        rightButton.isEnabled = enabled
    }

    private fun startSensorIfNeeded() {
        if (mode.usesSensors) {
            tiltController?.start()
        }
    }

    private fun updateSensorSpeed(speed: SensorSpeed) {
        if (!mode.usesSensors) {
            return
        }

        currentTickDelayMillis = speed.delayMillis.coerceAtLeast(MIN_TICK_DELAY_MS)
        modeText.text = getString(R.string.sensor_mode_speed_format, speed.label)
    }

    private fun centeredImageLayout(maxSizeDp: Int, laneFill: Float): FrameLayout.LayoutParams {
        val size = dp(scaledSpriteSizeDp(maxSizeDp, laneFill))
        return FrameLayout.LayoutParams(size, size).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun scaledSpriteSizeDp(maxSizeDp: Int, laneFill: Float): Int {
        val horizontalInsetsDp = (resources.getDimension(R.dimen.road_side_inset) * 2f) /
            resources.displayMetrics.density
        val laneWidthDp = (resources.configuration.screenWidthDp - horizontalInsetsDp) / logic.lanes
        return minOf(maxSizeDp, (laneWidthDp * laneFill).toInt()).coerceAtLeast(24)
    }

    private fun roadCellBackground(lane: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun setSpriteVisible(sprite: ImageView?, visible: Boolean, animate: Boolean) {
        sprite ?: return

        if (visible) {
            if (sprite.visibility != View.VISIBLE) {
                sprite.animate().cancel()
                sprite.visibility = View.VISIBLE

                if (animate) {
                    sprite.alpha = 0f
                    sprite.scaleX = 0.78f
                    sprite.scaleY = 0.78f
                    sprite.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(SPRITE_ANIMATION_MS)
                        .start()
                } else {
                    resetSprite(sprite)
                }
            }
        } else if (sprite.visibility != View.INVISIBLE) {
            sprite.animate().cancel()
            sprite.visibility = View.INVISIBLE
            resetSprite(sprite)
        }
    }

    private fun resetSprite(sprite: ImageView) {
        sprite.alpha = 1f
        sprite.scaleX = 1f
        sprite.scaleY = 1f
    }

    private fun pulseView(view: View) {
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        view.animate()
            .scaleX(1.12f)
            .scaleY(1.12f)
            .setDuration(PULSE_ANIMATION_MS)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(PULSE_ANIMATION_MS)
                    .start()
            }
            .start()
    }

    private fun flashCrashOverlay() {
        crashOverlay.animate().cancel()
        crashOverlay.alpha = 0f
        crashOverlay.visibility = View.VISIBLE
        crashOverlay.animate()
            .alpha(0.34f)
            .setDuration(CRASH_FLASH_IN_MS)
            .withEndAction {
                crashOverlay.animate()
                    .alpha(0f)
                    .setDuration(CRASH_FLASH_OUT_MS)
                    .withEndAction { crashOverlay.visibility = View.INVISIBLE }
                    .start()
            }
            .start()
    }

    companion object {
        const val EXTRA_GAME_MODE = "extra_game_mode"
        private const val ROAD_ROWS = 12
        private const val ROAD_LANES = 5
        private const val COUNTDOWN_INTERVAL_MS = 650L
        private const val MIN_TICK_DELAY_MS = 180L
        private const val SPRITE_ANIMATION_MS = 140L
        private const val PULSE_ANIMATION_MS = 90L
        private const val CRASH_FLASH_IN_MS = 50L
        private const val CRASH_FLASH_OUT_MS = 220L
    }
}
