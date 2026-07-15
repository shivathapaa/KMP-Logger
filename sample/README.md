# Sample Apps

Demo applications for the [`logger`](../logger) and [`logger-coroutines`](../logger-coroutines)
Kotlin Multiplatform libraries, running on **every supported target**: Android, iOS, Desktop (JVM),
Web (JS & Wasm), and native terminal (macOS, Linux, Windows).

## Modules

| Module | What it is | Platforms |
| --- | --- | --- |
| [`composeApp`](composeApp) | Compose Multiplatform UI demo | Android, Desktop (JVM), iOS, Web (JS), Web (Wasm) |
| [`androidApp`](androidApp) | Android application hosting the Compose UI | Android |
| [`iosApp`](iosApp) | iOS application (Xcode) hosting the `ComposeApp` framework | iOS |
| [`terminalApp`](terminalApp) | Kotlin/Native CLI demo (simple + structured + coroutine logging) | macOS, Linux, Windows |

## Prerequisites

- **JDK 17+** (project runs the Gradle/JVM toolchain).
- Run every command below **from the repository root**.
- On **Windows**, use `gradlew.bat` in place of `./gradlew`.
- **Android**: a connected device or running emulator (for `installDebug`) + Android SDK.
- **iOS**: macOS with **Xcode** and an iOS simulator/device.
- **Web**: a browser (Chrome recommended) - the dev-server tasks launch it automatically.
- **Terminal native**: `runDebugExecutable<Target>` only runs on a **matching host**
  (macOS runs `MacosArm64`, Linux runs `LinuxX64`, Windows runs `MingwX64`); native targets
  cannot be run cross-platform.

## Run

| Platform | Command |
| --- | --- |
| **Android** (device/emulator) | `./gradlew :sample:androidApp:installDebug` |
| **iOS** | Open `sample/iosApp/iosApp.xcodeproj` in Xcode → select the `iosApp` scheme + a simulator → **Run**. Gradle builds the framework automatically via the `embedAndSignAppleFrameworkForXcode` build phase. |
| **Desktop (JVM)** | `./gradlew :sample:composeApp:run` |
| **Web (JS)** | `./gradlew :sample:composeApp:jsBrowserDevelopmentRun` |
| **Web (Wasm)** | `./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun` |
| **Terminal - macOS** | `./gradlew :sample:terminalApp:runDebugExecutableMacosArm64` |
| **Terminal - Linux** | `./gradlew :sample:terminalApp:runDebugExecutableLinuxX64` |
| **Terminal - Windows** | `./gradlew :sample:terminalApp:runDebugExecutableMingwX64` |

> The web dev-server tasks serve at `http://localhost:8080` and keep running until stopped (Ctrl+C).
> The desktop `run` task opens a native window. Terminal apps print to stdout and exit.
> For an optimized native binary, swap `Debug` → `Release` (e.g. `runReleaseExecutableMacosArm64`).

## Build only (produce artifacts, no run)

| Target | Command | Output |
| --- | --- | --- |
| Android APK | `./gradlew :sample:androidApp:assembleDebug` | `sample/androidApp/build/outputs/apk/debug/androidApp-debug.apk` |
| iOS framework (simulator) | `./gradlew :sample:composeApp:linkDebugFrameworkIosSimulatorArm64` | `sample/composeApp/build/bin/iosSimulatorArm64/` |
| Desktop native package | `./gradlew :sample:composeApp:packageDistributionForCurrentOS` | `sample/composeApp/build/compose/binaries/` (`.dmg` / `.msi` / `.deb`) |
| Web (JS) bundle | `./gradlew :sample:composeApp:jsBrowserDistribution` | `sample/composeApp/build/dist/js/productionExecutable/` |
| Web (Wasm) bundle | `./gradlew :sample:composeApp:wasmJsBrowserDistribution` | `sample/composeApp/build/dist/wasmJs/productionExecutable/` |
| Terminal binary | `./gradlew :sample:terminalApp:linkDebugExecutableMacosArm64` | `sample/terminalApp/build/bin/macosArm64/debugExecutable/` |

## Build everything at once

```bash
./gradlew \
  :sample:androidApp:assembleDebug \
  :sample:composeApp:jvmJar \
  :sample:composeApp:jsBrowserDistribution \
  :sample:composeApp:wasmJsBrowserDistribution \
  :sample:composeApp:linkDebugFrameworkIosSimulatorArm64 \
  :sample:terminalApp:linkDebugExecutableMacosArm64
```
