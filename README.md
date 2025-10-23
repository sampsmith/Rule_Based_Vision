# Dough Vision Detector

A rule-based machine vision application for detecting dough on production lines using OpenCV. The system uses color segmentation, contour detection, and configurable rule-based logic to identify and track dough pieces in real-time.

## Features

- **C++ Backend**: High-performance vision processing using OpenCV
  - Color segmentation (HSV-based)
  - Contour detection and feature extraction
  - Rule-based validation engine
  - Real-time camera interface
  
- **Java Desktop Frontend**: User-friendly Swing-based GUI
  - Live detection visualization
  - Interactive teach mode with ROI drawing
  - Real-time parameter adjustment
  - Configuration management
  
- **Optimized for Constrained Hardware**: Designed to run efficiently on embedded systems and industrial PCs

## System Architecture

```
┌─────────────────────────────────────────┐
│         Java Frontend (Swing)           │
│  - Live View                            │
│  - Teach Mode                           │
│  - Configuration UI                     │
└─────────────┬───────────────────────────┘
              │ JNI / Socket
┌─────────────▼───────────────────────────┐
│         C++ Backend (OpenCV)            │
│  - Camera Interface                     │
│  - Color Segmentation                   │
│  - Contour Detection                    │
│  - Rule Engine                          │
└─────────────────────────────────────────┘
```

## Prerequisites

### C++ Backend Dependencies
- **CMake**: Version 3.10 or higher
- **C++ Compiler**: GCC 7+ or Clang 6+ with C++17 support
- **OpenCV**: Version 4.0 or higher
- **nlohmann/json**: JSON library for C++ (header-only)

### Java Frontend Dependencies
- **Java JDK**: Version 11 or higher
- **Maven**: Version 3.6 or higher
- **Gson**: JSON library for Java (managed by Maven)

### Ubuntu/Debian Installation
```bash
# Install C++ dependencies
sudo apt-get update
sudo apt-get install -y build-essential cmake
sudo apt-get install -y libopencv-dev
sudo apt-get install -y nlohmann-json3-dev

# Install Java and Maven
sudo apt-get install -y openjdk-11-jdk maven
```

## Building the Project

### Building C++ Backend

```bash
cd backend
mkdir build && cd build
cmake ..
make -j$(nproc)
```

This will create:
- `../build/bin/dough_vision_detector` - Standalone executable
- `../build/lib/libdough_vision_jni.so` - JNI library for Java frontend

### Building Java Frontend

```bash
cd frontend
mvn clean package
```

This will create:
- `target/dough-vision-frontend-1.0-SNAPSHOT.jar` - Runnable JAR with dependencies

## Running the Application

### Option 1: C++ Standalone (Headless)

```bash
cd build/bin
./dough_vision_detector [config_file]

# Example with default config
./dough_vision_detector ../../config/default_config.json
```

**Controls:**
- `q` or `ESC`: Quit application
- `s`: Save current configuration

### Option 2: Java GUI Application

```bash
cd frontend
java -jar target/dough-vision-frontend-1.0-SNAPSHOT.jar
```

Or double-click the JAR file in a file manager.

## Configuration

Configuration files are stored in JSON format in the `config/` directory.

### Configuration Parameters

```json
{
    "color_segmentation": {
        "lower": [H_min, S_min, V_min],  // HSV lower bound
        "upper": [H_max, S_max, V_max]   // HSV upper bound
    },
    "roi": {
        "x": 0, "y": 0,
        "width": 640, "height": 480
    },
    "detection": {
        "min_area": 500,              // Minimum contour area in pixels
        "max_area": 50000,            // Maximum contour area in pixels
        "min_circularity": 0.3,       // 0.0 to 1.0 (1.0 = perfect circle)
        "max_circularity": 1.0
    },
    "camera": {
        "index": 0,                   // Camera device index
        "width": 640,
        "height": 480,
        "fps": 30
    }
}
```

### HSV Color Tuning

For dough detection, typical HSV ranges:
- **White/Beige Dough**: H: 20-40, S: 50-255, V: 50-255
- **Whole Wheat Dough**: H: 10-30, S: 20-150, V: 80-200

Use the **Teach Mode** in the GUI to visually tune these values.

## Using Teach Mode

1. Launch the Java frontend application
2. Navigate to the **Teach Mode** tab
3. Load a sample image from your production line
4. Use the **Rectangle Tool** to draw regions of interest (ROIs)
5. Click **Save Regions** to apply the ROI to the configuration
6. Adjust color ranges in the **Configuration** tab
7. Save the configuration using **File → Save Configuration**

## Project Structure

```
DoughVisionDetector/
├── backend/                 # C++ backend
│   ├── CMakeLists.txt      # CMake build configuration
│   ├── include/            # Header files
│   │   ├── vision_processor.h
│   │   ├── color_segmentation.h
│   │   ├── contour_detector.h
│   │   ├── rule_engine.h
│   │   ├── config_manager.h
│   │   └── camera_interface.h
│   ├── src/                # Implementation files
│   │   ├── main.cpp
│   │   ├── vision_processor.cpp
│   │   ├── color_segmentation.cpp
│   │   ├── contour_detector.cpp
│   │   ├── rule_engine.cpp
│   │   ├── config_manager.cpp
│   │   ├── camera_interface.cpp
│   │   └── jni_interface.cpp
│   └── lib/                # External libraries
│
├── frontend/               # Java frontend
│   ├── pom.xml            # Maven configuration
│   └── src/main/java/com/doughvision/
│       ├── DoughVisionApp.java
│       ├── MainFrame.java
│       ├── VisionPanel.java
│       ├── ControlPanel.java
│       ├── TeachModePanel.java
│       ├── ConfigurationPanel.java
│       └── ConfigurationManager.java
│
├── config/                 # Configuration files
│   └── default_config.json
│
├── build/                  # Build outputs (generated)
│   ├── bin/               # Executables
│   └── lib/               # Libraries
│
└── README.md
```

## Performance Optimization

For constrained hardware:

1. **Reduce Resolution**: Lower camera resolution (e.g., 640x480 or 320x240)
2. **Adjust FPS**: Target 15-20 FPS for production lines with moderate speed
3. **Limit ROI**: Use teach mode to define smaller regions of interest
4. **Morphology**: Adjust `morph_kernel_size` (smaller = faster, less noise filtering)

## Troubleshooting

### Camera Not Detected
```bash
# List available cameras
ls /dev/video*

# Test camera with OpenCV
v4l2-ctl --list-devices
```

### OpenCV Not Found
```bash
# Verify OpenCV installation
pkg-config --modversion opencv4

# If not found, install from source or check package name
```

### JNI Library Not Loading
Ensure `libdough_vision_jni.so` is in the Java library path:
```bash
java -Djava.library.path=../build/lib -jar frontend.jar
```

## Development

### Adding New Detection Rules

1. Modify `backend/include/rule_engine.h` to add new rule parameters
2. Update `DetectionRules` struct
3. Implement validation logic in `rule_engine.cpp`
4. Update configuration JSON schema
5. Add UI controls in Java `ConfigurationPanel.java`

### Extending the Frontend

The Java frontend uses Swing with a modular panel-based design:
- `VisionPanel`: Display component
- `ControlPanel`: Real-time controls
- `TeachModePanel`: ROI definition
- `ConfigurationPanel`: Parameter editing

## License

Copyright © 2024. All rights reserved.

## Support

For production deployment support and customization inquiries, please contact your system integrator.

## Roadmap

- [ ] Multi-camera support
- [ ] Advanced shape recognition
- [ ] Production line statistics
- [ ] Network-based configuration
- [ ] GPU acceleration with CUDA
- [ ] Web-based monitoring interface
