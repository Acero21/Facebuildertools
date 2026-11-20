# Facebuilder Tools

Offline Android face-modeling studio for creating an editable face, tracking landmarks, fitting face geometry, and preparing diffuse/albedo textures.

## Current MVP
- Kotlin + Jetpack Compose Android project
- Offline-first studio navigation
- Face Builder workspace with a 3D preview placeholder and head/jaw/eye controls
- Face Tracker, Face Geo Tracker, and Diffuse/Albedo workspaces
- Camera permission declared for the upcoming capture pipeline
- Export workflow placeholders for GLB, OBJ, and PNG

## Run
Open the repository in Android Studio, sync Gradle, and run the `app` configuration on an Android 8.0+ device or emulator.

## Roadmap
1. Add CameraX capture and local photo import.
2. Add on-device face landmark detection.
3. Add a low-poly head mesh and landmark fitting.
4. Generate an unlit diffuse/albedo texture and real GLB/OBJ export.

The current preview is intentionally a UI prototype; it does not yet claim to reconstruct or track a face.
