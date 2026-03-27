#!/bin/sh
set -eu

# Wait for database to be ready
# Using a simple /dev/tcp check with bash if available, otherwise fallback to a small sleep
# Since we are on eclipse-temurin (Ubuntu/Debian), we can try to use a simple timeout with /dev/tcp in bash
if [ -f /bin/bash ]; then
  until /bin/bash -c "echo > /dev/tcp/database/${DATABASE_CONTAINER_PORT:-5432}" 2>/dev/null; do
    echo "Waiting for database (via bash /dev/tcp)..."
    sleep 2
  done
else
  # Fallback to a simple delay if bash is not available, though it's usually there in these images
  # Or just let the app fail and restart if we can't check
  echo "Bash not found, skipping wait and hoping for the best..."
fi

exec "$@"