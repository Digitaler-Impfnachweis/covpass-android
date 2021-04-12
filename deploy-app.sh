#!/usr/bin/env bash
set -eu -o pipefail

APP="$1"
TYPE="release"
STATUS="${3:-completed}"

BUILDCONFIG="$(find $APP/build/generated/source/buildConfig/$TYPE/ -name BuildConfig.java)"
APPID="$(grep -E 'APPLICATION_ID' $BUILDCONFIG | sed 's/^.*APPLICATION_ID\s*=\s*"\(.\+\)".*$/\1/')"
APPPATH="$(ls "$APP/build/outputs/apk/${TYPE}"/*.apk | tail -1)"

IMAGE="de.icr.io/ega_tools/ega-android-fastlane:2"

docker pull "$IMAGE"
docker run -t \
    -w /app \
    -v "$(pwd):/app" \
    -v "$SUPPLY_JSON_KEY:/tmp/api.json:ro" \
    -v "$RELEASE_KEYSTORE:/tmp/release.keystore:ro" \
    -e "SUPPLY_JSON_KEY=/tmp/api.json" \
    -e "RELEASE_KEYSTORE=/tmp/release.keystore" \
    -e "RELEASE_KEYSTORE_PASSWORD=$RELEASE_KEYSTORE_PASSWORD" \
    -e "RELEASE_KEYSTORE_KEY=key" \
    -e "RELEASE_KEYSTORE_KEY_PASSWORD=$RELEASE_KEYSTORE_PASSWORD" \
    "$IMAGE" \
    ./deploy.sh "$APPID" "$APPPATH"
