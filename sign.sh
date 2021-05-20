#!/usr/bin/env bash
#
# (C) Copyright IBM Deutschland GmbH 2021
# (C) Copyright IBM Corp. 2021
#

set -eu -o pipefail

# Signs a bundle with the release key

APP="$1"
TYPE="${2:-release}"

if [[ -f "$APP" ]]; then
  BUNDLE="$APP"
else
  BUNDLE="$(ls "$APP/build/outputs/apk/${TYPE}"/*.apk | tail -1)"
fi

if [ -z "${RELEASE_KEYSTORE:-}" ]; then
  1>&2 echo "RELEASE_KEYSTORE environment variable must be set"
  exit 1
fi

if [ -z "${RELEASE_KEYSTORE_PASSWORD:-}" ]; then
  1>&2 echo "RELEASE_KEYSTORE_PASSWORD environment variable must be set"
  exit 1
fi

if [ -z "${RELEASE_KEYSTORE_KEY:-}" ]; then
  1>&2 echo "RELEASE_KEYSTORE_KEY environment variable must be set"
  exit 1
fi

if [ -z "${RELEASE_KEYSTORE_KEY_PASSWORD:-}" ]; then
  1>&2 echo "RELEASE_KEYSTORE_KEY_PASSWORD environment variable must be set"
  exit 1
fi

if [[ "$BUNDLE" == *.aab ]]; then
  jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -storetype pkcs12 -keystore "$RELEASE_KEYSTORE" -storepass "$RELEASE_KEYSTORE_PASSWORD" -keypass "$RELEASE_KEYSTORE_KEY_PASSWORD" "$BUNDLE" "$RELEASE_KEYSTORE_KEY"
else
  java -jar /usr/lib/android-sdk/build-tools/debian/apksigner.jar sign --verbose --ks-type pkcs12 --ks "$RELEASE_KEYSTORE" --ks-pass "env:RELEASE_KEYSTORE_PASSWORD" --ks-key-alias "$RELEASE_KEYSTORE_KEY" --key-pass "env:RELEASE_KEYSTORE_KEY_PASSWORD" "$BUNDLE"
fi
