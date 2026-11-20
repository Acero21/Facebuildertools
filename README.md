# Facebuilder Tools

Offline Android face-modeling studio for creating an editable face, tracking landmarks, fitting face geometry, and preparing diffuse/albedo textures.

## Face tracking setup

The tracker runs MediaPipe Face Landmarker locally through CameraX. The model file is intentionally not committed to this repository. Download it once before building:

```bash
./scripts/download_face_landmarker.sh
```

This installs `face_landmarker.task` into `app/src/main/assets/`. The one-time download needs internet access; after the model is installed, face inference works offline. On Android, grant camera permission when prompted. The live preview displays facial landmarks count and does not save or upload camera frames.

## Current MVP
- Kotlin + Jetpack Compose Android project
- Offline-first studio navigation
- Live front-camera face landmark tracking using MediaPipe Tasks Vision and CameraX
- Face Builder, Face Geo Tracker, and Diffuse/Albedo workspaces
- Export workflow placeholders for GLB, OBJ, and PNG

## Run
Open the repository in Android Studio, sync Gradle, run the model download script once, and launch the `app` configuration on an Android 8.0+ device. Camera access is required for live tracking.

## Roadmap
1. Persist tracking sessions and support recorded-video input.
2. Add landmark-guided head mesh fitting.
3. Generate an unlit diffuse/albedo texture and real GLB/OBJ export.

The builder preview and mesh fitting/export workflows remain UI prototypes; face landmark tracking is implemented separately in the Face Tracker tab.
