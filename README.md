# AquaTap

AquaTap is a native Android prototype that turns an NFC-tagged water bottle into a lightweight hydration tracker. Each accepted NFC scan records a 250 ml intake event, updates the daily hydration dashboard, animates a custom water bottle visualization, and provides visual and haptic feedback.

The project has been manually verified with physical NFC tags on an NFC-capable Android device.

---

## Features

- **NFC-based Intake Logging**: Uses Android Reader Mode for immediate tag detection.
- **Physical NFC Verification**: Tested and verified with real NFC hardware.
- **Simulated Scan Fallback**: A manual button for testing on emulators or demoing without tags.
- **Daily Progress Dashboard**: Real-time tracking of current intake vs. daily goal.
- **Animated Water Bottle**: A custom Compose Canvas visualization that fills dynamically as you drink.
- **Recent Intake History**: A scrollable list of recent scans with timestamps.
- **Instant Feedback**: Material 3 Snackbar and haptic (vibration) confirmation upon successful scan.
- **Scan Cooldown Guard**: Prevents accidental duplicate entries from rapid repeated detections (1500ms window).
- **Unit-Tested Domain Logic**: Hydration progress and remaining-intake calculations are covered with JUnit tests.
---

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **NFC**: Android NFC Reader Mode
- **Architecture**: MVVM (ViewModel, StateFlow)
- **Graphics**: Compose Canvas for custom animations
- **Testing**: JUnit 4
- **Build System**: Gradle (Kotlin DSL)

---

## Architecture

The project follows a clean, modular structure:

- **`domain/`**: Contains core business logic and models.
  - `HydrationState`: Data class for current progress and history.
  - `IntakeEvent`: Model for individual scan events.
  - `HydrationCalculator`: Pure logic for progress percentages and remaining amounts.
- **`state/`**: Manages application state.
  - `HydrationViewModel`: Handles intake recording, history management, and state updates.
- **`ui/`**: Compose-based UI components.
  - `AquaTapScreen`: Main dashboard layout with Scaffold and Snackbar support.
  - `WaterBottleView`: Custom animated canvas drawing of the water bottle.
  - `IntakeStats`: Statistics summary cards.
- **`MainActivity`**:
  - App entry point and ViewModel binding.
  - NFC Reader Mode lifecycle management (`onResume`/`onPause`).
  - Scan cooldown and haptic feedback orchestration.

For more details, see [ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Demo Flow

1. **Open AquaTap**: Launch the app on an NFC-capable device.
2. **Scan/Simulate**: Tap the **"Simulate NFC scan"** button or bring a **physical NFC tag** near the device's sensor.
3. **Record Intake**: The app records a 250 ml intake event.
4. **Visual & Haptic Update**:
   - The **animated bottle** fills upward with a smooth wave animation.
   - A **Snackbar** appears saying "250 ml added".
   - The device triggers a short **vibration**.
   - A new item appears in the **Recent intake** list.
5. **Cooldown**: Hold the tag near the sensor; notice that duplicate entries are ignored for 1.5 seconds.
6. **Reset**: Tap **"Reset"** to clear daily progress and history.

---

## Local Setup

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 37 (API 37)
- (Optional) NFC-capable Android device for physical testing

### Build and Run
Clone the repository and run the following commands in the project root:

**Windows:**
```powershell
.\gradlew.bat assembleDebug
```

**Unix/macOS:**
```bash
./gradlew assembleDebug
```

### Running Tests
**Windows:**
```powershell
.\gradlew.bat testDebugUnitTest
```

**Unix/macOS:**
```bash
./gradlew testDebugUnitTest
```

---

## NFC Testing Notes

- **Hardware Required**: Physical NFC scanning requires a real Android phone with NFC support enabled.
- **Emulator Use**: Standard emulators do not support physical NFC. Use the **"Simulate NFC scan"** button for all non-hardware testing.
- **Tag Compatibility**: This MVP supports standard NFC tag technologies (NFC-A, B, F, V).
- **Validation**: Currently, the app treats *any* detected NFC tag as a valid scan. Paired-tag validation is planned for the roadmap.
- **Lifecycle**: NFC Reader Mode is only active while the app is in the foreground.

---

## Current Limitations

- **Volatile State**: No persistence yet; progress resets when the app is closed.
- **Fixed Goal**: Daily goal is currently hardcoded to 2000ml.
- **No Tag Pairing**: Any NFC tag triggers a scan.
- **No Background Scanning**: The app must be open to record a scan.
- **Local Only**: No cloud sync or Health Connect integration.

---

## Roadmap

- [ ] **Persistence**: Add Local storage using DataStore or Room.
- [ ] **Custom Goals**: Allow users to edit their daily hydration goal.
- [ ] **Tag Pairing**: Allow the app to respond only to a selected bottle tag.
- [ ] **History View**: Expand the recent history into a full daily/weekly view.
- [ ] **Insights**: Add optional Gemini-powered hydration tips and insights.
- [ ] **CI**: Add GitHub Actions for build and unit-test verification.
---


