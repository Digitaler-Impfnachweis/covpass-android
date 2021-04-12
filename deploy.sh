#!/usr/bin/env bash
set -eu -o pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

APPID="$1"
BUNDLE="$2"
STATUS="${3:-completed}"

"$ROOT/sign.sh" "$BUNDLE"

fastlane deploy "appid:${APPID}" "skip_metadata:true" "path:$BUNDLE" "release_status:$STATUS"
