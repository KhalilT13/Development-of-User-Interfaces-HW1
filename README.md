# HW2 – Android Road Game

An Android road game developed in Kotlin for the second homework assignment in the **Development of User Interfaces** course.

## Student

**Khalil Talhami**

## Project Description

The player controls an Android robot travelling on a five-lane road.

The goal is to avoid obstacles, collect coins, increase the score, and travel the greatest possible distance before losing all three lives.

The game supports button controls and accelerometer-based tilt controls.

## Main Features

* Custom application icon
* Five-lane road
* Extended road matrix
* Android robot player
* Moving obstacles
* Collectable coins
* Score counter
* Distance counter
* Three lives
* Collision detection
* Crash sound
* Game Over screen
* Three gameplay modes
* Accelerometer controls
* High-score table
* Score-location map
* Location saving
* Ten highest scores stored locally

## Game Modes

The main menu contains three gameplay modes.

### Button Mode – Slow

The player moves the robot left and right using on-screen buttons.

The game runs at a slower speed.

### Button Mode – Fast

The player moves the robot left and right using on-screen buttons.

The game runs at a faster speed.

### Sensor Mode

The player controls the robot by tilting the phone.

* Tilt left to move left
* Tilt right to move right
* Tilt forward to increase the game speed
* Hold the phone in the neutral position for normal speed
* Tilt backward to decrease the game speed

When Sensor Mode starts, the phone should be held still briefly so the accelerometer can calibrate its neutral position.

## Bonus Feature

Forward and backward phone tilt dynamically changes the game speed in Sensor Mode.

The three available sensor speeds are:

* Fast
* Normal
* Slow

## Gameplay

Obstacles and coins move down the road through a logical row-and-lane matrix.

* Colliding with an obstacle removes one life
* A crash sound is played after a collision
* Collecting a coin increases the score
* The distance counter increases while the game is running
* The game ends when all three lives are lost
* The final result can be saved in the high-score table

## Movement Implementation

The robot, obstacles, and coins are positioned using a logical matrix containing rows and lanes.

Game objects are not moved using absolute screen coordinates such as:

* `setX()`
* `setY()`
* `translationX`
* `translationY`

The game updates the logical row or lane and displays the object inside the correct matrix cell.

`scaleX` and `scaleY` are used only for visual size animations and do not change an object’s position.

## High Scores

The application stores the ten highest results using `SharedPreferences`.

Each saved result contains:

* Score
* Distance
* Date and time
* Latitude and longitude when location is available

Results are ordered by score, with distance used as a secondary comparison.

The High Scores screen can be opened directly from the main menu.

## High-Score Screen

The High Scores screen contains two separate fragments.

### ScoresTableFragment

Displays the ten highest results in a table with the following columns:

* Rank
* Score
* Distance
* Date and time

Each table row is clickable.

### ScoreMapFragment

Displays the saved location of the selected result using OpenStreetMap.

Clicking a row in the score table updates the map and displays the corresponding location marker.

If the selected result has no saved location, the application displays:

> No saved location for this score.

## Location

The application requests location permission while the game is active.

When location is available, the newest location is saved together with the final score.

If permission is denied or no location is available, the score is still saved without coordinates.

## Technologies

* Kotlin
* Android Studio
* Android XML layouts
* Android accelerometer sensor
* Android location services
* SharedPreferences
* Fragments
* Fragment Result API
* OpenStreetMap
* WebView
* Gradle

## Project Structure

Important source files include:

* `MainActivity.kt` – main menu
* `GameActivity.kt` – gameplay screen and game loop
* `GameLogic.kt` – logical game state
* `GameMode.kt` – gameplay-mode definitions
* `TiltController.kt` – accelerometer controls
* `LocationProvider.kt` – location tracking
* `HighScore.kt` – saved-score model
* `HighScoreStore.kt` – score storage and sorting
* `HighScoresActivity.kt` – high-score screen container
* `ScoresTableFragment.kt` – high-score table
* `ScoreMapFragment.kt` – score-location map
* `RoadView.kt` – road drawing

## How to Run

1. Clone or download the repository.
2. Open the project in Android Studio.
3. Wait for Gradle synchronization to finish.
4. Connect an Android phone or start an Android emulator.
5. Select the device from the device selector.
6. Select the `app` run configuration.
7. Press the Run button.

The project can also be built from the Android Studio terminal using:

```powershell
.\gradlew.bat clean test assembleDebug
```

## Testing Sensor Mode on a Physical Phone

1. Enable Developer Options on the phone.
2. Enable USB debugging.
3. Connect the phone using a USB cable that supports data transfer.
4. Select the phone in Android Studio.
5. Run the application.
6. Enter Sensor Mode.
7. Hold the phone still briefly for calibration.
8. Test left, right, forward, and backward tilt.

## Build Status

The project was verified using:

```powershell
.\gradlew.bat clean test assembleDebug
```

Final result:

```text
BUILD SUCCESSFUL
```
