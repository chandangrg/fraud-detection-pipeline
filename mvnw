#!/usr/bin/env sh
set -eu

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
if [ ! -f "$PROPS" ]; then
  echo "Missing $PROPS" >&2
  exit 1
fi

DIST_URL=$(sed -n 's/^distributionUrl=//p' "$PROPS" | head -n 1)
if [ -z "$DIST_URL" ]; then
  echo "distributionUrl is not configured in $PROPS" >&2
  exit 1
fi

ARCHIVE_NAME=${DIST_URL##*/}
MVN_DIR_NAME=${ARCHIVE_NAME%-bin.zip}
CACHE_ROOT="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/$MVN_DIR_NAME"
MVN_BIN="$CACHE_ROOT/$MVN_DIR_NAME/bin/mvn"
ARCHIVE="$CACHE_ROOT/$ARCHIVE_NAME"

if [ ! -x "$MVN_BIN" ]; then
  mkdir -p "$CACHE_ROOT"
  if [ ! -f "$ARCHIVE" ]; then
    echo "Downloading Maven from $DIST_URL" >&2
    if command -v curl >/dev/null 2>&1; then
      curl --fail --location --silent --show-error "$DIST_URL" --output "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
      wget --quiet "$DIST_URL" --output-document "$ARCHIVE"
    else
      echo "Install Maven 3.9+, curl, or wget." >&2
      exit 1
    fi
  fi
  if ! command -v unzip >/dev/null 2>&1; then
    echo "The unzip command is required to bootstrap Maven." >&2
    exit 1
  fi
  rm -rf "$CACHE_ROOT/$MVN_DIR_NAME"
  unzip -q "$ARCHIVE" -d "$CACHE_ROOT"
fi

exec "$MVN_BIN" "$@"
