#!/usr/bin/env bash

fennel --compile "$1" | ./wrap_do.sh | tee /dev/tty | xclip -sel clipboard


