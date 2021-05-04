#!/usr/bin/env bash

# Set environment variable for GitHub action to recognise colours
export TERM=xterm

# Colours for stdout
RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
WHITE=$(tput setaf 7)
NC=$(tput sgr0)
RED_BG=$(tput setab 1)

# Path to test resources
TEST_RESOURCES_PATH=src/test/resources

# Temporary directory for saving compiled classes
TEMP_DIR_COMPILED_RESOURCES=$(mktemp -d)
echo "Created $TEMP_DIR_COMPILED_RESOURCES for saving compiled files."
echo -e "It will be removed after the script has executed with any exit code.\n"

# Delete the temporary directory as it served its purpose
cleanup() {
  echo -e "\nRemoving $TEMP_DIR_COMPILED_RESOURCES directory ..."
  rm -rf "$TEMP_DIR_COMPILED_RESOURCES"
  echo "Done, script execution complete."
}

# Make sure the temporary directory is deleted even if the script aborts
trap cleanup EXIT

# Verify if the resources compiles
compile() {
  local file=$1
  if javac "$file" -d "$TEMP_DIR_COMPILED_RESOURCES"
    then
      echo -e "${file}:${GREEN} Compiled successfully!${NC}"
    else
      echo -e "${RED}There were compilation errors in ${WHITE}${RED_BG}${file}${NC}"
  fi
}

# Find all Java files inside the test resources directory
find $TEST_RESOURCES_PATH -type f -name "*.java" | while read -r file
  do
    compile "$file"
  done
