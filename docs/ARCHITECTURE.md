# AquaTap Architecture

AquaTap is built using modern Android development practices, focusing on a clean separation of concerns and reactive state management.

## Architectural Layers

### 1. Domain Layer (`com.adabarbulescu.aquatap.domain`)
This layer is the core of the application, containing pure Kotlin logic and data models with no dependencies on Android frameworks.

- **`IntakeEvent`**: A simple data class representing a single hydration event (amount and timestamp).
- **`HydrationState`**: Encapsulates the entire state of the hydration tracking for the day.
- **`HydrationCalculator`**: A singleton object providing utility functions for UI calculations, such as progress percentage and remaining intake. This is fully unit-tested.

### 2. State/ViewModel Layer (`com.adabarbulescu.aquatap.state`)
Acts as the bridge between the UI and the domain logic.

- **`HydrationViewModel`**: 
    - Exposes a `StateFlow<HydrationState>` to the UI.
    - Manages the logic for recording new intake and resetting progress.
    - Ensures that history is capped to a manageable size for the MVP.

### 3. UI Layer (`com.adabarbulescu.aquatap.ui`)
Built entirely with Jetpack Compose, the UI is a function of the state provided by the ViewModel.

- **`AquaTapScreen`**: The top-level Composable that organizes the dashboard. It uses a `Scaffold` to manage the `SnackbarHost` for user feedback.
- **`WaterBottleView`**: A custom-drawn component using Compose `Canvas`. It uses `animateFloatAsState` for smooth filling and `rememberInfiniteTransition` for the wave effect.
- **`IntakeStats`**: Displays granular data cards (Total, Remaining, Percentage).

### 4. Hardware Integration (`MainActivity`)
The `MainActivity` handles the integration with Android system services.

- **NFC Reader Mode**: Implemented using `enableReaderMode` to provide a low-latency, "foreground-only" scanning experience.
- **Haptic Feedback**: Integrates with the `Vibrator` service to provide physical confirmation of a scan.
- **Lifecycle Management**: Carefully manages NFC scanning and cooldown timers to prevent duplicate records and ensure battery efficiency.

## Data Flow
1. **Trigger**: NFC Tag detection or Button Click.
2. **Action**: `MainActivity` calls `viewModel.recordIntake()`.
3. **Logic**: ViewModel updates the `_state` MutableStateFlow.
4. **Reaction**: Compose UI observes the `StateFlow` and re-composes automatically.
5. **Feedback**: `MainActivity` triggers vibration and shows a Snackbar.
