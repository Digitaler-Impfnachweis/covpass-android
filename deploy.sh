#!/usr/bin/env bash
set -eu -o pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

APP="$1"
TYPE="${2:-release}"
STATUS="${3:-completed}"

BUILDCONFIG="$(find $APP/build/generated/source/buildConfig/$TYPE/ -name BuildConfig.java)"
APPID="$(grep -E 'APPLICATION_ID' $BUILDCONFIG | sed 's/^.*APPLICATION_ID\s*=\s*"\(.\+\)".*$/\1/')"
BUNDLE="$(ls "$APP/build/outputs/apk/${TYPE}"/*.apk | tail -1)"

fastlane deploy "appid:${APPID}" "skip_metadata:true" "path:$BUNDLE" "release_status:$STATUS"
