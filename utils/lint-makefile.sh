#!/usr/bin/env bash

# This script attempts to find targets in a Makefile that are NOT file paths and are NOT in .PHONY.

MAKEFILE=${1:-Makefile}

if [ ! -f "$MAKEFILE" ]; then
    echo "Error: $MAKEFILE not found."
    exit 1
fi

# 1. Extract targets (simple ones, ignoring dynamic/pattern ones for now)
# Targets are lines starting with a word followed by a colon.
# We exclude .PHONY itself and targets that look like file paths (containing / or .)
targets=$(grep -E '^[a-zA-Z0-9_%-]+:' "$MAKEFILE" | cut -d: -f1 | grep -v '^\.' | grep -v '/' | sort | uniq)

# 2. Extract .PHONY targets
# This is tricky because .PHONY can span multiple lines.
phony_targets=$(make -p -f "$MAKEFILE" .PHONY 2>/dev/null | grep '^\.PHONY:' | cut -d: -f2- | tr ' ' '\n' | sed 's/\\//g' | grep -v '^$' | sort | uniq)

# Also check for .PHONY: $(addprefix ...) or similar, which make -p might not expand perfectly in all versions
# but make -p .PHONY usually works well to show the final set.

missing_phony=""
for target in $targets; do
    # Ignore pattern rules (containing %)
    if [[ "$target" == *"%"* ]]; then
        continue
    fi
    
    # Check if target is in phony_targets
    if ! echo "$phony_targets" | grep -qx "$target"; then
        # Check if it's a file that exists (not a perfect check for "is it a file target")
        if [ ! -f "$target" ]; then
            missing_phony="$missing_phony $target"
        fi
    fi
done

if [ -n "$missing_phony" ]; then
    echo "Potential non-file targets missing from .PHONY:"
    for m in $missing_phony; do
        echo "  - $m"
    done
    exit 1
else
    echo "All identified non-file targets are in .PHONY."
    exit 0
fi
