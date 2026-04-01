#!/bin/bash
set -e

GOINFRE_PATH="/goinfre/$USER"
DOCKER_ROOT="$GOINFRE_PATH/docker"
GRADLE_ROOT="$GOINFRE_PATH/gradle"
DOCKER_CONFIG_DIR="$HOME/.config/docker"
DOCKER_DAEMON_JSON="$DOCKER_CONFIG_DIR/daemon.json"

echo "Checking /goinfre partition..."
if [ ! -d "$GOINFRE_PATH" ]; then
    echo "ERROR: /goinfre/$USER does not exist. Please check your campus machine configuration."
    exit 1
fi

echo "Stopping Docker service..."
systemctl --user stop docker || true

echo "Preparing Docker directory in /goinfre..."
mkdir -p "$DOCKER_ROOT"

echo "Configuring Docker to use /goinfre as data-root..."
# Handle potentially broken symlinks in ~/.config/docker
if [ -L "$DOCKER_CONFIG_DIR" ] && [ ! -d "$DOCKER_CONFIG_DIR" ]; then
    echo "Fixing broken symlink for $DOCKER_CONFIG_DIR..."
    rm "$DOCKER_CONFIG_DIR"
fi
mkdir -p "$DOCKER_CONFIG_DIR"

if [ -f "$DOCKER_DAEMON_JSON" ]; then
    echo "Merging with existing daemon.json..."
    if command -v jq >/dev/null 2>&1; then
        jq --arg root "$DOCKER_ROOT" '. + {"data-root": $root}' "$DOCKER_DAEMON_JSON" > "${DOCKER_DAEMON_JSON}.tmp" && mv "${DOCKER_DAEMON_JSON}.tmp" "$DOCKER_DAEMON_JSON"
    else
        echo "{\"data-root\": \"$DOCKER_ROOT\"}" > "$DOCKER_DAEMON_JSON"
    fi
else
    echo "{\"data-root\": \"$DOCKER_ROOT\"}" > "$DOCKER_DAEMON_JSON"
fi

echo "Moving Gradle cache to /goinfre (much faster than NFS /sgoinfre)..."
mkdir -p "$GRADLE_ROOT"
if [ -L "$HOME/.gradle" ]; then
    rm "$HOME/.gradle"
elif [ -d "$HOME/.gradle" ]; then
    mv "$HOME/.gradle" "$HOME/.gradle_old"
fi
ln -sf "$GRADLE_ROOT" "$HOME/.gradle"

echo "Reloading systemd and starting Docker..."
systemctl --user daemon-reload
systemctl --user restart docker

echo "Verifying Docker root directory..."
NEW_ROOT=$(docker info --format '{{.DockerRootDir}}' 2>/dev/null || echo "FAILED")
echo "New Docker root: $NEW_ROOT"

if [[ "$NEW_ROOT" == *"$DOCKER_ROOT"* ]]; then
    echo "SUCCESS: Docker and Gradle are now using /goinfre!"
    echo "You might want to run 'docker system prune -a' to start fresh if you have corrupted data."
else
    echo "ERROR: Docker is still not using the new root. Check 'systemctl --user status docker' for errors."
    exit 1
fi
