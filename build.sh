#!/usr/bin/env bash
#
# Dependency-free build + test script for Movie Manager.
# Uses only the JDK (javac / java / jar) — no Maven or internet access required.
#
# Usage:
#   ./build.sh          # compile, run tests, and build the runnable JAR
#   ./build.sh test     # compile and run tests only
#   ./build.sh run      # build then launch the application
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/out"
DIST="$ROOT/dist"
MAIN="com.example.movie.Main"
JAR="$DIST/movie-manager.jar"

compile() {
    echo ">> Compiling..."
    rm -rf "$OUT"
    mkdir -p "$OUT"
    # Collect all sources recursively (main + test, including sub-packages).
    find "$ROOT/src" -name '*.java' > "$OUT/sources.txt"
    javac -d "$OUT" @"$OUT/sources.txt"
    echo ">> Compiled to $OUT"
}

test() {
    echo ">> Running tests..."
    java -cp "$OUT" com.example.movie.RepositoryTests
}

package() {
    echo ">> Building JAR..."
    mkdir -p "$DIST"
    jar --create --file "$JAR" --main-class "$MAIN" -C "$OUT" com/example/movie
    echo ">> Built $JAR"
}

case "${1:-all}" in
    test)
        compile
        test
        ;;
    run)
        compile
        package
        echo ">> Launching..."
        java -jar "$JAR"
        ;;
    all)
        compile
        test
        package
        echo ">> Done. Run with: java -jar $JAR"
        ;;
    *)
        echo "Unknown command: $1 (use: test | run | all)" >&2
        exit 1
        ;;
esac
