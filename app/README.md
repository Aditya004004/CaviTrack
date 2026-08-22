# CaviTrack Android App

CaviTrack is a robust, offline-first native Android inventory management app built with Kotlin, Jetpack Compose, and Material 3. 

## Architecture
The app strictly follows **Clean Architecture** combined with **MVVM/MVI** principles:
- **Presentation Layer**: Built completely in Jetpack Compose using unidirectional data flow via `UiState`.
- **Domain Layer**: Houses pure Kotlin models (`Component`, `Mold`, etc.) and abstract repository interfaces.
- **Data Layer**: Implements the repositories.
  - **Local Cache**: Handled via `Room` Database.
  - **Network**: Handled via `Firebase Firestore` and `Firebase Auth`.
  - **Offline-First Sync**: Edits made while offline are saved to a `pending_actions` queue in Room, and seamlessly synchronized back to Firestore using `WorkManager` when connectivity is restored.

## Setup & Requirements

### Firebase Configuration
1. Register this app (`com.company.cavitrack`) on the [Firebase Console](https://console.firebase.google.com/).
2. Enable Firestore and Firebase Authentication (Email/Password).
3. Download the `google-services.json` file.
4. Place `google-services.json` in the root of the `app/` directory.

### Build Instructions
1. Open the project in Android Studio (Iguana or later recommended).
2. Sync Gradle files.
3. Click **Run**.
