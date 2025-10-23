#!/bin/bash

# Dough Vision Detector Build Script

set -e  # Exit on error

echo "================================"
echo "Dough Vision Detector - Build"
echo "================================"
echo ""

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running from project root
if [ ! -d "backend" ] || [ ! -d "frontend" ]; then
    echo -e "${RED}Error: Must run from project root directory${NC}"
    exit 1
fi

# Build C++ backend
echo -e "${YELLOW}Building C++ backend...${NC}"
cd backend

if [ ! -d "build" ]; then
    mkdir build
fi

cd build
cmake ..
make -j$(nproc)

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ C++ backend built successfully${NC}"
else
    echo -e "${RED}✗ C++ backend build failed${NC}"
    exit 1
fi

cd ../..

# Build Java frontend
echo ""
echo -e "${YELLOW}Building Java frontend...${NC}"
cd frontend

mvn clean package -q

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Java frontend built successfully${NC}"
else
    echo -e "${RED}✗ Java frontend build failed${NC}"
    exit 1
fi

cd ..

# Summary
echo ""
echo "================================"
echo -e "${GREEN}Build Complete!${NC}"
echo "================================"
echo ""
echo "Executables created:"
echo "  • C++ Backend: build/bin/dough_vision_detector"
echo "  • Java Frontend: frontend/target/dough-vision-frontend-1.0-SNAPSHOT.jar"
echo ""
echo "To run:"
echo "  • Backend:  cd build/bin && ./dough_vision_detector"
echo "  • Frontend: cd frontend && java -jar target/dough-vision-frontend-1.0-SNAPSHOT.jar"
echo ""
