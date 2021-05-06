#!/usr/bin/env bash

set -o nounset # don't allow use of undefined variables
set -o pipefail # if any single command in a pipe chain exits non-zero, set the exit status to that
set -o errexit # if any single command exits non-zero, exit the entire script with that status

# Set environment variable for GitHub action to recognise colours
export TERM=xterm

# Colours for stdout
RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
NC=$(tput sgr0)

# Path to test resources
TEST_RESOURCES_PATH="$(git rev-parse --show-toplevel)/src/test/resources"

# Temporary directory for saving compiled classes
TEMP_DIR_COMPILED_RESOURCES=$(mktemp -d)
was_temp_created=$?
if [[ $was_temp_created -ne 0 ]]
  then
    printf "The directory could not be created. Exiting program with 1.\n"
    exit 1
fi

# Make sure the temporary directory is deleted even if the script aborts
trap 'rm -r $TEMP_DIR_COMPILED_RESOURCES' EXIT

# Global variables which will be modified as the script runs
status=0
file_count=0
compilation_errors=()

# Verify if the resources compiles
compile() {
  # actual path of the test resource
  local actual_path=$1
  # Name of the class after PREV or NEW prefix
  local syntactical_name
  syntactical_name="$(basename "$actual_path" | cut -d '_' -f 2)"
  local copied_path="$TEMP_DIR_COMPILED_RESOURCES/$syntactical_name"
  cp "$actual_path" "$copied_path"
  if ! javac "$copied_path" -d "$TEMP_DIR_COMPILED_RESOURCES"; then
    compilation_errors+=("$actual_path")
    status=1
  fi
}

# Record changes to global variables
shopt -s lastpipe

# Find all Java files inside the test resources directory
find "$TEST_RESOURCES_PATH" -type f -name "*.java" | while read -r file; do
  compile "$file"
  file_count=$((file_count+1))
done

if [[ $status -eq 0 ]]
  then
    printf "\n%bAll [%d/%d] files were compiled successfully!%b\n" "$GREEN" $file_count $file_count "$NC"
  else
    errors=${#compilation_errors[@]}
    printf "\n%bErrors in %d/%d files.%b\n" "$RED" "$errors" $file_count "$NC"
    for file in "${compilation_errors[@]}"; do
      printf "%b%s%b\n" "$RED" "$file" "$NC"
    done
fi

printf "Script exiting with code %d.\n" $status
exit $status
