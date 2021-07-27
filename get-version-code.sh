#!/usr/bin/env bash
#
# (C) Copyright IBM Deutschland GmbH 2021
# (C) Copyright IBM Corp. 2021
#

set -eu -o pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo -n $(($(git rev-list --count HEAD) + 80))
