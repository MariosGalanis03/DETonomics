#!/bin/bash

# Budget Tuner Run Script for macOS/Linux

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Budget Tuner Launcher ===${NC}"

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven (mvn) is not installed or not in your PATH."
    echo "Please install Maven to run this application."
    exit 1
fi

# Build if target directory doesn't exist or if forced with --build flag
if [ ! -d "target" ] || [ "$1" == "--build" ]; then
    echo -e "${GREEN}Building project...${NC}"
    mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check the errors above."
        exit 1
    fi
else
    echo -e "${GREEN}Project appears built. Skipping compilation (use --build to force rebuild).${NC}"
fi

# Run the application
echo -e "${GREEN}Starting application...${NC}"
mvn javafx:run
