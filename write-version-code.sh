#!/usr/bin/env bash
#
# (C) Copyright IBM Deutschland GmbH 2021
# (C) Copyright IBM Corp. 2021
#

set -eu -o pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

VERSION=$("$ROOT/get-version-code.sh")
echo "versionCode=$VERSION" > "$ROOT/generated.properties"
