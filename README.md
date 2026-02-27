# PakkiePakkie

Kotlin Multiplatform app targeting Android and iOS with Compose Multiplatform UI.

## Prerequisites
- JDK 17 or higher
- Android SDK (set path in `local.properties`)
- Xcode (for iOS builds)

## Android
Open the project in Android Studio and run the imported android run configuration.

Build: `./gradlew :androidApp:assembleDevDebug`

## iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run the desired scheme (dev/stag/prod).

## Signing

Keystore files and credentials

## Git merge strategy

This repository is configured with **fast-forward merge** and **squash always**:

- No merge commits are created. Fast-forward merges only.
- When there is a merge conflict, the user is given the option to rebase.
- If merge trains are enabled, merging is only possible if the branch can be rebased without conflicts.
- Squashing is always performed -- each merge request results in a single commit on the target branch.
