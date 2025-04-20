#!/bin/bash
# Usuń poprzednie pliki

# publish.sh

# Zainstaluj w trybie edytowalnym
python changelog.py
bash git.sh
bash publish.sh
