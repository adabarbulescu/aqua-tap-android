# AquaTap

AquaTap is a native Android prototype that turns an NFC-tagged water bottle into a lightweight hydration tracker. Record water intake through physical NFC scans, monitor your progress with an animated dashboard, and get immediate tactile feedback.

The project has been manually verified with physical NFC tags on an NFC-capable Android device.

<p align="center">
  <img
    src="https://github.com/user-attachments/assets/95030114-5c10-447b-85d9-bdc1df678623"
    alt="AquaTap Android app screenshot"
    width="280"
  />
</p>

---


## Features

- **Bottle Pairing**: Semantic NFC interaction. Pair your app with a specific bottle tag; other tags are rejected.
- **NFC-based Intake Logging**: Uses Android Reader Mode for immediate tag detection.
- **Physical NFC Verification**: Tested and verified with real NFC hardware.
- **Simulated Scan Fallback**: A manual button for testing on emulators or demoing without tags.
- **Daily Progress Dashboard**: Real-time tracking of current intake vs. daily goal.
- **Animated Water Bottle**: A custom Compose Canvas visualization that fills dynamically as you drink.
- **Recent Intake History**: A scrollable list of recent scans with timestamps.
- **Instant Feedback**: Material 3 Snackbar and haptic (vibration) confirmation upon successful scan.
- **Scan Cooldown Guard**: Prevents accidental duplicate entries from rapid repeated detections (1500ms window).
- **Persistent Preferences**: Paired bottle ID is stored locally using DataStore.
- **Unit Tested**: Core domain logic, tag matching, and ViewModel states are covered with JUnit tests.

---

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **NFC**: Android NFC Reader Mode
- **Persistence**: Preferences DataStore
- **Architecture**: MVVM (ViewModel, StateFlow)
- **Graphics**: Compose Canvas for custom animations
- **Testing**: JUnit 4, Kotlin Coroutines Test
- **Build System**: Gradle (Kotlin DSL)

---

## Architecture

The project follows a clean, modular structure:

- **`domain/`**: Core business logic and models.
  - `HydrationState`: Data class for current progress, history, and pairing state.
  - `HydrationCalculator`: Pure logic for progress and remaining-intake calculations.
  - `NfcTagMatcher`: Logic for validating scanned tags against the paired bottle.
- **`data/`**: Data access and persistence.
  - `BottleTagRepository`: Interface and DataStore implementation for persisting the paired tag ID.
- **`state/`**: Manages application state.
  - `HydrationViewModel`: Orchestrates pairing flow, intake recording, and event emission.
- **`ui/`**: Compose-based UI components.
  - `AquaTapScreen`: Main dashboard with pairing UI and scrollable history.
  - `WaterBottleView`: Custom animated canvas drawing.
- **`MainActivity`**:
  - NFC Reader Mode lifecycle management.
  - Event collection for haptics and Snackbar feedback.

---

## Demo Flow

1. **Pair Your Bottle**:
   - On first launch, tap **"Pair your bottle"**.
   - Scan any NFC tag. The app stores this ID as your "Bottle".
2. **Standard Scan**:
   - Bring your paired tag near the phone.
   - The app records 250 ml, vibrates, and shows a "250 ml added" snackbar.
3. **Rejected Scan**:
   - Scan a *different* NFC tag.
   - The app rejects the scan with a "This is not your paired bottle" message.
4. **Unpair**:
   - Tap **"Unpair"** in the header to forget the current bottle and return to pairing mode.
5. **Simulation**: Use the **"Simulate NFC scan"** button to demo the flow on an emulator.

---

## Local Setup

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 37 (API 37)

### Build and Run
Clone the repository and run:

**Windows:**
```powershell
.\gradlew.bat assembleDebug
```

**Unix/macOS:**
```bash
./gradlew assembleDebug
```

### Running Tests
```powershell
.\gradlew.bat testDebugUnitTest
```

---

## Current Limitations

- **Volatile History**: Intake history is not yet persisted; it resets when the app is closed (only the paired bottle ID persists).
- **Fixed Goal**: Daily goal is hardcoded to 2000ml.
- **Single Bottle**: Supports pairing with only one bottle at a time.
- **Background Scanning**: The app must be open to record a scan.

---

## Roadmap

- [ ] **History Persistence**: Add Room database for long-term intake storage.
- [ ] **Custom Goals**: Allow users to edit their daily hydration goal.
- [ ] **Insights**: Add Gemini-powered hydration tips.
- [ ] **CI**: Add GitHub Actions for automated verification.
