#!/usr/bin/env bash
set -euo pipefail

ASSET_DIR="app/src/main/assets"
MODEL_PATH="$ASSET_DIR/face_landmarker.task"
MODEL_URL="https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task"

mkdir -p "$ASSET_DIR"
curl --fail --location --output "$MODEL_PATH" "$MODEL_URL"
echo "Installed MediaPipe Face Landmarker model at $MODEL_PATH"
