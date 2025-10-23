cmake_minimum_required(VERSION 3.10)
project(DoughVisionBackend VERSION 1.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find OpenCV
find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

# Find JNI for Java interface
find_package(JNI)
if(JNI_FOUND)
    include_directories(${JNI_INCLUDE_DIRS})
endif()

# Include directories
include_directories(${PROJECT_SOURCE_DIR}/include)

# Source files
set(SOURCES
    src/main.cpp
    src/vision_processor.cpp
    src/color_segmentation.cpp
    src/contour_detector.cpp
    src/rule_engine.cpp
    src/config_manager.cpp
    src/camera_interface.cpp
)

# Create executable
add_executable(dough_vision_detector ${SOURCES})

# Link OpenCV libraries
target_link_libraries(dough_vision_detector ${OpenCV_LIBS})

# Optional: Build shared library for JNI interface
add_library(dough_vision_jni SHARED
    src/jni_interface.cpp
    src/vision_processor.cpp
    src/color_segmentation.cpp
    src/contour_detector.cpp
    src/rule_engine.cpp
    src/config_manager.cpp
    src/camera_interface.cpp
)

target_link_libraries(dough_vision_jni ${OpenCV_LIBS})

# Set output directory
set_target_properties(dough_vision_detector dough_vision_jni
    PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../build/bin
    LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../build/lib
)
