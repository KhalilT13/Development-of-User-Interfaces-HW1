# Development of User Interfaces - HW1

## Project Description

This is a simple Android game developed in Kotlin as part of a course project.

The player controls an Android robot that moves between three lanes. Red bugs move downward through the lanes, and the goal is to avoid crashing into them.
The player starts with three coffee lives, and the game ends when all lives are lost.

## Game Features

- Three-lane game area
- Android robot player
- Left and right movement using arrow buttons
- Red bugs obstacles
- Random obstacle lanes
- Constant obstacle speed
- Collision detection
- Toast message on crash
- Vibration feedback on crash
- Three coffee lives system
- Countdown before the game starts: 3, 2, 1, GO
- Game over screen
- Restart option after game over

## Implementation Note

The game is implemented using a matrix of Android Views. Each game cell contains ImageViews, and movement is created by changing the visibility of the views.

For example:

- Bugs move by hiding the bug image in the current row and showing it in the next row.
- The robot moves by hiding it in the old lane and showing it in the new lane.

This approach follows the course requirement to avoid moving objects using x/y coordinates.

## Technologies Used

- Kotlin
- Android XML layouts
- ConstraintLayout
- LinearLayout
- FrameLayout
- ImageView
- ImageButton
- Handler and Runnable
- Toast
- Vibration

## How to Play

1. Press **Start Game**.
2. Wait for the countdown to finish.
3. Use the left and right arrow buttons to move the robot.
4. Avoid the red bugs.
5. Each crash removes one coffee life.
6. After losing all three lives, the game ends.
7. Press **Start Again** to restart the game.

## Main Files

### `MainActivity.kt`

Contains the main game logic, including:

- Countdown
- Player movement
- Bug movement
- Collision detection
- Lives system
- Vibration feedback
- Game over handling
- Restart logic

### `activity_main.xml`

Contains the screen layout, including:

- Game matrix container
- Player and bug ImageViews
- Coffee lives display
- Movement buttons
- Countdown text
- Game over UI
- Start and restart buttons

## Student

Khalil Talhami

## Course

Development of User Interfaces
