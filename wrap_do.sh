#!/usr/bin/env bash
set -euo pipefail

# Read input from stdin
input_text=$(cat)

# Prepend "do" and append "end"
output_text="do\n$input_text\nend"

# Print the modified text to stdout
echo -e "$output_text"
