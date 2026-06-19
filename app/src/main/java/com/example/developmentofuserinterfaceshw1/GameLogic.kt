package com.example.developmentofuserinterfaceshw1

import kotlin.random.Random

data class FallingItem(
    var row: Int,
    var lane: Int
)

class GameLogic(
    val rows: Int = 12,
    val lanes: Int = 5,
    private val random: Random = Random.Default
) {
    data class StepResult(
        val crashed: Boolean = false,
        val collectedCoin: Boolean = false,
        val gameOver: Boolean = false
    )

    val robotRow: Int = rows - 2
    val obstacles = mutableListOf<FallingItem>()
    val coins = mutableListOf<FallingItem>()

    var currentLane: Int = lanes / 2
        private set
    var lives: Int = 3
        private set
    var score: Int = 0
        private set
    var distance: Int = 0
        private set

    init {
        reset()
    }

    fun reset() {
        currentLane = lanes / 2
        lives = 3
        score = 0
        distance = 0

        obstacles.clear()
        obstacles += FallingItem(0, random.nextInt(lanes))
        obstacles += FallingItem(-3, random.nextInt(lanes))
        obstacles += FallingItem(-6, random.nextInt(lanes))
        obstacles += FallingItem(-9, random.nextInt(lanes))

        coins.clear()
        coins += FallingItem(-2, random.nextInt(lanes))
        coins += FallingItem(-5, random.nextInt(lanes))
        coins += FallingItem(-8, random.nextInt(lanes))
    }

    fun moveLeft(): StepResult {
        if (currentLane > 0) {
            currentLane--
        }
        return resolveRobotCell()
    }

    fun moveRight(): StepResult {
        if (currentLane < lanes - 1) {
            currentLane++
        }
        return resolveRobotCell()
    }

    fun step(): StepResult {
        if (lives <= 0) {
            return StepResult(gameOver = true)
        }

        distance++

        obstacles.forEach { obstacle ->
            obstacle.row++
            if (obstacle.row >= rows) {
                resetObstacle(obstacle)
            }
        }

        coins.forEach { coin ->
            coin.row++
            if (coin.row >= rows) {
                resetCoin(coin)
            }
        }

        return resolveRobotCell()
    }

    private fun resolveRobotCell(): StepResult {
        var crashed = false
        var collectedCoin = false

        coins
            .filter { it.row == robotRow && it.lane == currentLane }
            .forEach { coin ->
                score += 10
                collectedCoin = true
                resetCoin(coin)
            }

        val hitObstacles = obstacles.filter { it.row == robotRow && it.lane == currentLane }
        if (hitObstacles.isNotEmpty() && lives > 0) {
            lives--
            crashed = true
            hitObstacles.forEach { resetObstacle(it) }
        }

        return StepResult(
            crashed = crashed,
            collectedCoin = collectedCoin,
            gameOver = lives <= 0
        )
    }

    private fun resetObstacle(obstacle: FallingItem) {
        obstacle.row = -random.nextInt(1, 5)
        obstacle.lane = random.nextInt(lanes)
    }

    private fun resetCoin(coin: FallingItem) {
        coin.row = -random.nextInt(2, 7)
        coin.lane = random.nextInt(lanes)
    }
}
