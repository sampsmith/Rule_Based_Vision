#!/bin/bash
# Launcher script for Dough Vision Detector Frontend

cd "$(dirname "$0")/frontend"

echo "========================================="
echo "  Dough Vision Detector - Teach Mode"
echo "========================================="
echo ""
echo "Starting Java GUI application..."
echo ""

# Run the application
java -jar target/dough-vision-frontend-1.0-SNAPSHOT.jar

echo ""
echo "Application closed."
