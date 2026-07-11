# AquaTap Architecture

AquaTap is built using modern Android development practices, focusing on a clean separation of concerns and reactive state management.

## Architectural Layers

### 1. Domain Layer (`com.adabarbulescu.aquatap.domain`)
This layer is the core of the application, containing pure Kotlin logic and data models with no dependencies on Android frameworks.

- **`IntakeEvent`**: A simple data class representing a single hydration event (amount and timestamp).
- **`HydrationState`**: Encapsulates the entire state of the hydration tracking, including progress, history, and bottle pairing status.
- **`HydrationCalculator`**: A singleton object providing utility functions for UI calculations, such as progress percentage and remaining intake.
- **`NfcTagMatcher`**: Logic for validating scanned tags against the paired bottle ID.

### 2. Data Layer (`com.adabarbulescu.aquatap.data`)
Handles data persistence and external communication.

- **`SettingsRepository`**: Interface and implementation (`DataStoreSettingsRepository`) that uses **Jetpack Preferences DataStore** to persist the paired bottle's NFC ID and the user's daily hydration goal.
- **`IntakeRepository`**: Interface and implementation (`RoomIntakeRepository`) that uses **Room Database** to store the history of intake events.

### 3. State/ViewModel Layer (`com.adabarbulescu.aquatap.state`)
Acts as the bridge between the UI and the domain logic.

- **`HydrationViewModel`**: 
    - Exposes a `StateFlow<HydrationState>` for UI state.
    - Exposes a `SharedFlow<UiEvent>` for one-time events like haptic feedback and Snackbars.
    - Orchestrates the pairing flow: enables pairing mode, stores new tag IDs, and validates future scans.
    - Manages intake recording and history persistence via `IntakeRepository`.
    - Manages user settings like the daily goal via `SettingsRepository`.

### 4. UI Layer (`com.adabarbulescu.aquatap.ui`)
Built entirely with Jetpack Compose, the UI is a function of the state provided by the ViewModel.

- **`AquaTapScreen`**: The top-level Composable that organizes the dashboard, pairing UI, and history.
- **`WaterBottleView`**: A custom-drawn component using Compose `Canvas` with animated filling and wave effects.

### 5. Hardware Integration (`MainActivity`)
The `MainActivity` handles the integration with Android system services.

- **NFC Reader Mode**: Uses `enableReaderMode` to detect tag IDs and pass them to the ViewModel for validation.
- **Haptic Feedback**: Integrates with the `Vibrator` service to provide physical confirmation.
- **Event Collection**: Observes `viewModel.events` to trigger Snackbars and vibrations.

## Data Flow
1. **Trigger**: NFC Tag detection in `MainActivity`.
2. **Action**: `MainActivity` calls `viewModel.handleNfcScan(tagId)`.
3. **Validation**: ViewModel uses `NfcTagMatcher` to check the tag against the repository's `pairedTagId`.
4. **Persistence**: If valid, ViewModel records the intake via `IntakeRepository`, which saves it to the **Room Database**.
5. **State Update**: ViewModel observes the repository's Flow, updates `HydrationState`, and emits a `UiEvent`.
6. **Reaction**:
    - Compose UI re-composes based on the new state (dashboard, progress bar, animated bottle, and history list).
    - `MainActivity` reacts to the `UiEvent` by triggering specific haptic feedback and showing a Snackbar.
