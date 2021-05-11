#!/usr/bin/env bash
set -eu -o pipefail

IMAGE="de.icr.io/ega_tools/ega-android-fastlane:2"

ARGS=()
if [[ ! -z "${SUPPLY_JSON_KEY:-}" ]]; then
  ARGS+=(-v "$SUPPLY_JSON_KEY:/tmp/api.json:ro" -e SUPPLY_JSON_KEY=/tmp/api.json)
fi
if [[ ! -z "${RELEASE_KEYSTORE:-}" ]]; then
  ARGS+=(-v "$RELEASE_KEYSTORE:/tmp/release.keystore:ro" -e RELEASE_KEYSTORE=/tmp/release.keystore -e RELEASE_KEYSTORE_PASSWORD=$RELEASE_KEYSTORE_PASSWORD -e RELEASE_KEYSTORE_KEY=key -e "RELEASE_KEYSTORE_KEY_PASSWORD=$RELEASE_KEYSTORE_PASSWORD")
fi

docker pull "$IMAGE"
docker run -t \
    -w /app \
    "${ARGS[@]}" \
    -v "$(pwd):/app" \
    "$IMAGE" \
    "$@"
