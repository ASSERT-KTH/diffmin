#!/usr/bin/env bash

set -o nounset # don't allow use of undefined variables
set -o pipefail # if any single command in a pipe chain exits non-zero, set the exit status to that

# Set environment variable for GitHub action to recognise colours
export TERM=xterm

# Colours for stdout
RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
WHITE=$(tput setaf 7)
NC=$(tput sgr0)
RED_BG=$(tput setab 1)

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
printf "Created %s for saving compiled files temporarily.\n" "$TEMP_DIR_COMPILED_RESOURCES"
printf "It will be removed after the script has executed with any exit code.\n\n"

# Delete the temporary directory as it served its purpose
cleanup() {
  printf "\nRemoving %s directory ...\n" "$TEMP_DIR_COMPILED_RESOURCES"
  rm -r "$TEMP_DIR_COMPILED_RESOURCES"
  printf "Done.\n"
}

# Make sure the temporary directory is deleted even if the script aborts
trap cleanup EXIT

# Global variables which will be modified as the script runs
status=0
file_count=0
compilation_errors=0

# Verify if the resources compiles
compile() {
  local file=$1 # path to program in /tmp or equal to actual path
  local actual_path=$2 # actual path of the test resource
  if ! javac "$file" -d "$TEMP_DIR_COMPILED_RESOURCES"; then
    printf "%bThere were compilation errors in %b%b%b%b\n" "$RED" "$WHITE" "$RED_BG" "$actual_path" "$NC"
    status=1
    compilation_errors=$((compilation_errors+1))
  fi
}

# Rename file according to the first public class inside
get_and_rename_file() {
  local file=$1
  REGEX_PATTERN="(?<=public\sclass\s)[A-Z$_][\w$]*"

  # -m 1: returns the first public class which is matched
  # -P: use PCRE to interpret lookbehind
  # -o: print only the matched part
  classname=$(grep -m 1 -P -o "$REGEX_PATTERN" "$file")

  if [ -z "$classname" ]
    then
      compile "$file" "$file"
    else
      local new_file_name="${classname}.java"
      cp "$file" "$TEMP_DIR_COMPILED_RESOURCES/${new_file_name}"
      local new_file_path="${TEMP_DIR_COMPILED_RESOURCES}/${new_file_name}"
      compile "$new_file_path" "$file"
  fi
}

# Record changes to global variables
shopt -s lastpipe

# Find all Java files inside the test resources directory
for file in $(find "$TEST_RESOURCES_PATH" -type f -name "*.java"); do
  get_and_rename_file "$file"
  file_count=$((file_count+1))
done

if [[ $status -eq 0 ]]
  then
    printf "\n%bAll [%d/%d] files were compiled successfully!%b\n" "$GREEN" $file_count $file_count "$NC"
  else
    printf "\n%bErrors in %d/%d files.%b\n" "$RED" $compilation_errors $file_count "$NC"
fi

printf "Script exiting with code %d.\n" $status
exit $status
