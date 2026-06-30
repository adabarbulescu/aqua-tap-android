# AquaTap Demo Guide

This guide describes how to demonstrate the core features of the AquaTap MVP.

## Scenario 1: The First Sip (NFC Scan)

1. **Setup**: Ensure the app is open on an NFC-enabled Android device.
2. **Action**: Bring any standard NFC tag (like a travel card, office badge, or dedicated NTAG215) to the back of the phone.
3. **What to Observe**:
    - **Haptic**: The phone gives a short, sharp buzz.
    - **Visual**: A Snackbar at the bottom says "250 ml added".
    - **Animation**: The water level in the bottle visualization rises smoothly.
    - **History**: A new entry "+250 ml" appears at the bottom of the screen with the current time.

## Scenario 2: Rapid Scanning (Duplicate Guard)

1. **Action**: Quickly tap the NFC tag against the phone multiple times in succession.
2. **What to Observe**:
    - Only the first scan is recorded.
    - The app ignores subsequent scans for 1.5 seconds.
    - This demonstrates the **Duplicate-Scan Guard**, which prevents accidental double-logging.

## Scenario 3: Emulator/No-Tag Demo

1. **Action**: Tap the **"Simulate NFC scan"** button.
2. **What to Observe**:
    - The experience is identical to a real NFC scan (Vibration, Snackbar, and Animation).
    - This ensures the app can be demoed even when physical tags aren't available.

## Scenario 4: Reaching the Goal

1. **Action**: Continue scanning until the total intake reaches 2000 ml.
2. **What to Observe**:
    - The **"Remaining"** stat card drops to 0 ml.
    - The **"Progress"** card reaches 100%.
    - The animated bottle is now full up to the shoulder.
    - The **LinearProgressIndicator** in the daily card is completely filled.

## Scenario 5: Starting Fresh

1. **Action**: Tap the **"Reset"** button.
2. **What to Observe**:
    - All stats return to 0.
    - The water bottle empties with an animation.
    - The recent history is cleared.
