#include <iostream>
#include <opencv2/opencv.hpp>
#include "vision_processor.h"
#include "camera_interface.h"
#include "config_manager.h"

using namespace dough_vision;

int main(int argc, char** argv) {
    std::cout << "Dough Vision Detector - Starting..." << std::endl;
    
    // Load configuration
    std::string config_path = "../config/default_config.json";
    if (argc > 1) {
        config_path = argv[1];
    }
    
    // Initialize camera
    CameraInterface camera;
    if (!camera.initialize(0)) {
        std::cerr << "Error: Could not initialize camera" << std::endl;
        return -1;
    }
    
    // Initialize vision processor
    VisionProcessor processor;
    if (!processor.initialize(config_path)) {
        std::cerr << "Error: Could not initialize vision processor" << std::endl;
        return -1;
    }
    
    std::cout << "System initialized. Press 'q' to quit, 's' to save config" << std::endl;
    
    cv::Mat frame;
    while (true) {
        // Capture frame
        if (!camera.captureFrame(frame)) {
            std::cerr << "Error: Could not capture frame" << std::endl;
            break;
        }
        
        // Process frame
        DetectionResult result = processor.processFrame(frame);
        
        // Display results
        cv::Mat display = processor.getProcessedFrame();
        
        // Draw detection info
        std::string info = "Dough Count: " + std::to_string(result.dough_count);
        cv::putText(display, info, cv::Point(10, 30), 
                    cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(0, 255, 0), 2);
        
        if (!result.is_valid) {
            cv::putText(display, "ALERT: " + result.message, cv::Point(10, 70),
                        cv::FONT_HERSHEY_SIMPLEX, 0.8, cv::Scalar(0, 0, 255), 2);
        }
        
        cv::imshow("Dough Detection", display);
        cv::imshow("Segmentation", processor.getSegmentedFrame());
        
        // Handle keyboard input
        char key = cv::waitKey(30);
        if (key == 'q' || key == 27) { // 'q' or ESC
            break;
        } else if (key == 's') {
            std::cout << "Saving configuration..." << std::endl;
            // TODO: Save current configuration
        }
    }
    
    camera.release();
    cv::destroyAllWindows();
    
    std::cout << "Dough Vision Detector - Stopped" << std::endl;
    return 0;
}
