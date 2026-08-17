# CaviTrack Android App

CaviTrack is a robust, offline-first native Android inventory management app built with Kotlin, Jetpack Compose, and Material 3. 

## Architecture
The app strictly follows **Clean Architecture** combined with **MVVM/MVI** principles:
- **Presentation Layer**: Built completely in Jetpack Compose using unidirectional data flow via `UiState`.
- **Domain Layer**: Houses pure Kotlin models (`Component`, `Mold`, etc.) and abstract repository interfaces.
- **Data Layer**: Implements the repositories.
  - **Local Cache**: Handled via `Room` Database.
  - **Network**: Handled via `Retrofit`.
  - **Offline-First Sync**: Edits made while offline are saved to a `PendingActions` queue in Room, and seamlessly synchronized back to the server using `WorkManager` when connectivity is restored.

## Setup & Requirements

### Firebase Cloud Messaging (FCM)
For Push Notifications (e.g., low stock alerts) to function:
1. Register this app (`com.company.cavitrack`) on the [Firebase Console](https://console.firebase.google.com/).
2. Download the `google-services.json` file.
3. Place `google-services.json` in the root of the `app/` directory.

### Build Instructions
1. Open the project in Android Studio (Iguana or later recommended).
2. Sync Gradle files.
3. If running the emulator, the Retrofit Base URL is hardcoded to `http://10.0.2.2:3000/` to correctly route to your host machine's Node.js backend. Ensure the backend is running locally.
4. Click **Run**.
