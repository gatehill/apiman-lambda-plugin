#!/usr/bin/env bash

set -e

CODE_FILES=(
    "policyBackend"
    "policyRequestMutator"
    "policyResponseMutator"
)

for CODE_FILE in ${CODE_FILES[@]}; do
    echo "Creating function archive: ${CODE_FILE}"
    CODE_SOURCE="$( cd ../examples && pwd )/${CODE_FILE}.js"
    CODE_ARCHIVE="/$( cd ../src/test/resources && pwd )/${CODE_FILE}.zip"

    zip -j ${CODE_ARCHIVE} ${CODE_SOURCE}
done
