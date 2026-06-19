# Characterization - Road Runner HW2

## Game Idea

Road Runner HW2 is a five-lane survival runner. Obstacles fall from the top of the road while the player controls an Android runner near the bottom. The player survives by moving between lanes, collects coins for score, and tries to travel the greatest distance before losing three lives.

## Actors / User

The main user is the player. The player chooses a control mode, moves the runner left or right, avoids obstacles, collects coins, and reviews saved high scores after a game ends.

## Screens

- Main menu: contains exactly three required game mode options: Button mode - slow, Button mode - fast, and Sensor mode.
- Game screen: contains the five-lane road, player, obstacles, coins, lives, score, distance counter, crash feedback, and game-over actions.
- High-score screen: displays the saved top 10 results and includes a map section that updates when a score with a saved location is selected.

## Main Features

- Five lanes replace HW1's three lanes.
- The road is longer with 12 rows.
- Slow and fast button modes keep the HW1 left/right button movement with different game speeds.
- Sensor mode uses accelerometer tilt. Tilting left moves left; tilting right moves right.
- Coins spawn randomly and increase score when collected.
- The odometer increases while the game is running.
- Crashes play a sound, vibrate, remove one life, and eventually end the game.
- High scores are saved with score, distance, date/time, latitude, and longitude.
- Location permission is requested safely. Denied permission saves scores without coordinates instead of crashing.
- Tapping a score updates the map to that score's saved coordinates.

## Implementation Notes

- `MainActivity` is the menu and starts the selected game mode.
- `GameActivity` owns the game UI, countdown, rendering, crash feedback, and game-over flow.
- `GameLogic` contains lane count, row count, player position, obstacles, coins, score, lives, distance, and collision rules.
- `TiltController` wraps Android accelerometer input.
- `LocationProvider` handles runtime location permission and safe last-known-location lookup.
- `HighScoreStore` saves and loads the top 10 scores with `SharedPreferences`.
- `HighScoresActivity` displays the score table and updates an OpenStreetMap `WebView`.

## HW2 Requirement Coverage

- Custom icon: implemented with a road runner adaptive launcher foreground/background.
- Five-lane road: implemented in `GameLogic` and rendered dynamically in `GameActivity`.
- Longer gameplay area: implemented with 12 road rows.
- Tilt controls: implemented with `TiltController`.
- Button controls: preserved in slow and fast modes.
- Three-option menu: implemented in `activity_main.xml`.
- Coins and score: implemented in `GameLogic` and rendered by `GameActivity`.
- Odometer: implemented as `distance` in `GameLogic` and displayed in `GameActivity`.
- Crash sound: implemented with `ToneGenerator`.
- High scores: implemented with `HighScoreStore`.
- Location and map: implemented with `LocationProvider` and `HighScoresActivity`.
