
https://user-images.githubusercontent.com/13612410/212567902-b967615b-8f14-4c4c-b128-8e1529eade2c.mp4

# LetSee!

LetSee provides an easy way to mock API responses at runtime for **Android** and **iOS** apps built with Kotlin Multiplatform. Save server responses as JSON files, pick the one you want on the fly, and test every edge case without restarting or changing code.

## Features

- **Runtime mock selection** — intercept any API call and choose which JSON response to return
- **Scenarios** — define ordered steps so LetSee auto-responds to a sequence of requests
- **On-the-fly editing** — paste or edit JSON directly at runtime
- **Live-to-server fallback** — let individual requests hit the real server while mocking others
- **Floating debug overlay** — draggable button with quick-access mock picker (Android Compose & iOS UIKit)

## Installation

### Android (Gradle — Kotlin DSL)

Add both dependencies to your app module's `build.gradle.kts`. **Both are required** — `LetSeeCore` provides the mock engine, and `LetSeeUI` provides the floating debug button and mock selection overlay. `LetSeeUI` is available from **version 0.0.9** onwards.

```kotlin
dependencies {
    implementation("io.github.rrevel.letsee-kmm:LetSeeCore:0.0.9")
    implementation("io.github.rrevel.letsee-kmm:LetSeeUI:0.0.9")
}
```

Make sure `mavenCentral()` is in your `settings.gradle.kts` repositories:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

### iOS (Swift Package Manager)

Add the KMM repository as a Swift Package dependency:

1. In Xcode, go to **File → Add Package Dependencies**
2. Enter the repository URL: `https://github.com/Let-See/kmm`
3. Select the version/branch you want

## Quick Start — Android

### 1. Add mock JSON files

LetSee reads mocks from Android's **`assets`** folder. Create a `Mocks` directory inside `src/main/assets/` with subfolders matching your API endpoint paths:

```
app/src/main/assets/
└── Mocks/
    ├── .ls.global.json          ← optional path mapping (see below)
    ├── products/
    │   ├── success_productList.json
    │   └── error_notFound.json
    └── categories/
        ├── success_categoryList.json
        ├── success_emptyList.json
        └── filters/
            └── success_filterList.json
```

When LetSee intercepts a request to `https://api.example.com/products`, it shows all JSON files in the `Mocks/products/` folder for you to choose from.

#### Path mapping with `.ls.global.json`

If your API paths are complex (e.g., `/v2/staging/api/products`), you don't need to create deep nested folders. Add a `.ls.global.json` in the `Mocks/` root:

```json
{
    "maps": [
        { "folder": "/products", "to": "/v2/staging/api" },
        { "folder": "/categories", "to": "/v1/api" }
    ]
}
```

This maps requests to `/v2/staging/api/products` → `Mocks/products/`.

> **Android gotcha — AAPT strips dot-files**
>
> Android's asset packaging tool (AAPT) **silently ignores files whose names start with a dot** by default. This means `.ls.global.json` is never included in the APK, so LetSee shows mock folders without path mappings — mock keys appear as raw folder names (e.g. `/arrangements/`) instead of the full API paths.
>
> **Symptom:** LetSee intercepts requests and shows the mock selection UI, but only the generic system mocks (Custom Failure, Custom Success, Live, Cancel) appear — no folder-specific mocks.
>
> **Fix:** Override `ignoreAssetsPattern` in your `build.gradle.kts` to stop AAPT from stripping dot-files:
>
> ```kotlin
> android {
>     androidResources {
>         ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc"
>     }
> }
> ```
>
> The default pattern includes `.*:` which strips all dot-files. The override above preserves `.ls.global.json` in the APK.

### 2. Initialize LetSee

Initialization is split across two entry points — core setup belongs in `Application`, and UI setup belongs in `Activity`.

**Application class** — register the Android context, load mocks, and set configuration. Register in `AndroidManifest.xml` via `android:name=".MyApp"`:

```kotlin
import android.app.Application
import io.github.letsee.Configuration
import io.github.letsee.DefaultLetSee
import io.github.letsee.implementations.setLetSeeAndroidContext

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setLetSeeAndroidContext(this)
        DefaultLetSee.letSee.setMocks(path = "Mocks")
        DefaultLetSee.letSee.setConfigurations(
            Configuration.default.copy(isMockEnabled = true)
        )
    }
}
```

**Activity** — call `initLetSee()` to initialize platform services (clipboard support), then add the overlay. `initLetSee()` comes from `LetSeeUI` and is an **Activity extension function** — it must be called from an `Activity`, not from `Application`:

```kotlin
import io.github.letsee.DefaultLetSee
import io.github.letsee.ui.initLetSee

class MainActivity : ComponentActivity() {
    private val letSee = DefaultLetSee.letSee

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLetSee()

        setContent {
            // ... your app content (see step 3)
        }
    }
}
```

### 3. Add the debug overlay

Wrap your app content with `LetSeeOverlay` to get a draggable floating button for picking mock responses at runtime.

**Compose-based app** — drop `LetSeeOverlay` directly into your composition tree:

```kotlin
import io.github.letsee.ui.LetSeeOverlay

setContent {
    LetSeeOverlay(letSee) {
        MyAppContent()
    }
}
```

**XML-based app** — `LetSeeOverlay` is a composable, so it needs a `ComposeView` host. Add one programmatically on top of your existing layout after `setContentView()`:

```kotlin
import androidx.compose.ui.platform.ComposeView
import android.widget.FrameLayout
import io.github.letsee.ui.LetSeeOverlay

// In Activity.onCreate(), after setContentView(...)
val rootView = findViewById<FrameLayout>(android.R.id.content)
val composeOverlay = ComposeView(this).apply {
    setContent {
        LetSeeOverlay(DefaultLetSee.letSee) {}
    }
}
rootView.addView(composeOverlay)
```

### 4. Intercept network requests

**OkHttp** — add the LetSee interceptor to your client:

```kotlin
import okhttp3.OkHttpClient

val client = OkHttpClient.Builder()
    .addLetSee(letSee)
    .build()
```

When mocking is enabled, LetSee intercepts matching requests and returns the mock you select. When disabled, requests pass through to the real server.

**Custom networking layer** — LetSee does not automatically intercept HTTP traffic. If your networking layer uses an abstraction that bypasses OkHttp, you need to create a thin bridge. Pass a `DefaultRequest` to `letSee.addRequest()` along with a `Result` callback; LetSee either returns a mock or invokes the callback with a "Live" signal so you can fall through to the real network:

```kotlin
letSee.addRequest(
    request = DefaultRequest(url = url, method = method),
    onResult = { result ->
        when (result) {
            is MockResult -> handleMockResponse(result.json)
            is LiveResult -> proceedWithRealRequest()
        }
    }
)
```

## Quick Start — iOS

### 1. Add mock JSON files

Add a `Mocks` folder to your app bundle with JSON files organized by endpoint path.

### 2. Setup LetSee

```swift
import LetSeeCore

// In your AppDelegate or app startup
let config = LetSeeConfiguration(
    baseURL: URL(string: "https://api.example.com/")!,
    isMockEnabled: true
)
LetSee.shared.config(config)
LetSee.shared.addMocks(from: Bundle.main.bundlePath + "/Mocks")
```

### 3. Add the floating overlay

```swift
// Create the LetSee debug window
let letSeeWindow = LetSeeWindow(frame: window.frame)
letSeeWindow.windowScene = window.windowScene
```

## Mock file conventions

- **`success_*.json`** — successful response mock (default if no prefix)
- **`error_*.json`** — error response mock

### Path mapping (`.ls.global.json`)

Place a `.ls.global.json` in your Mocks root to map folders to complex URL paths:

```json
{
    "maps": [
        { "folder": "/products", "to": "/v1/staging" },
        { "folder": "/categories", "to": "/v2/api" }
    ]
}
```

## Scenarios

Define automated response sequences as `.plist` (iOS) or JSON files. Each step specifies a folder and response file name. When a scenario is active, LetSee auto-responds in order without manual selection.

## Architecture

```
kmm/
├── LetSeeCore/     # Shared KMP logic (mock loading, interception, scenarios)
├── LetSeeUI/       # Compose Multiplatform debug overlay
├── androidApp/     # Android showcase app
└── iosApp/         # iOS showcase app
```

## Publishing

Artifacts are published to Maven Central under the group `io.github.rrevel.letsee-kmm`.

| Artifact | Description |
|---|---|
| `LetSeeCore` | Core library — mock loading, interception, scenarios |
| `LetSeeUI` | Debug overlay — floating button, mock picker (Compose) |

## License

MIT
