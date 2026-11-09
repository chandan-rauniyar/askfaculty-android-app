# AskFaculty Android Client

Native Android application for the **AskFaculty** platform, built with Kotlin, Jetpack Compose, and Material 3.

---

## Prerequisites

- **Android Studio** (Koala, Jellyfish, or newer)
- **JDK 11** or higher
- **Android SDK** (Compile SDK 37, Min SDK 26)
- **Android Emulator** or physical Android device (API level 26+)

---

## Backend Setup & API Base URL

The app communicates with the AskFaculty backend API.
- **Backend Base URL (Debug):** `http://10.0.2.2:5000/api/v1` (maps to `localhost:5000` on the Android emulator).
- **Physical Device Setup:** If running on a physical device connected to the same Wi-Fi network as your development PC, override the debug base URL in `gradle.properties` (or `local.properties`) with your PC's LAN IP:
  ```properties
  askfaculty.debug.baseUrl=http://<YOUR_PC_IP>:5000/api/v1
  ```

---

## Configuration (`gradle.properties`)

Ensure your `gradle.properties` contains the required base URL properties:
```properties
# Debug API base URL (defaults to emulator host)
askfaculty.debug.baseUrl=http://10.0.2.2:5000/api/v1

# Release API base URL (required for release builds; build will fail if missing)
askfaculty.release.baseUrl=https://api.college.test/api/v1
```

---

## How to Run in Android Studio

1. **Clone & Open:**
   - Clone the repository and open the root folder in **Android Studio**.
2. **Sync Project:**
   - Allow Android Studio to import the project and sync Gradle (`libs.versions.toml`).
3. **Run Backend:**
   - Ensure the AskFaculty backend server is running locally (refer to the backend repository instructions: `docker compose up -d`, `npm run dev`).
4. **Run App:**
   - Select an emulator (API 26+).
   - Select the `debug` build variant.
   - Click **Run** (Green play button) or run via terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## Running Tests

To run local unit tests:
```bash
./gradlew testDebugUnitTest
```

---

## Documentation

For detailed architecture, step-by-step implementation plans, and API contracts, consult the documentation inside the `docs/` directory:
- [AGENTS.md](AGENTS.md) — AI tools & developer instructions
- [ANDROID_PLAN.md](docs/ANDROID_PLAN.md) — Step-by-step build plan
- [ANDROID_API_CONTRACT.md](docs/backend/ANDROID_API_CONTRACT.md) — Backend REST and Socket.IO contract
- [PROGRESS.md](docs/PROGRESS.md) — Current development progress and tech setup
