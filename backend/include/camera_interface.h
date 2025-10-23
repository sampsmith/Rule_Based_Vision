#ifndef CAMERA_INTERFACE_H
#define CAMERA_INTERFACE_H

#include <opencv2/opencv.hpp>
#include <memory>

namespace dough_vision {

class CameraInterface {
public:
    CameraInterface();
    ~CameraInterface();

    // Initialize camera with index
    bool initialize(int camera_index = 0);
    
    // Initialize with video file
    bool initializeFromFile(const std::string& video_path);
    
    // Capture a single frame
    bool captureFrame(cv::Mat& frame);
    
    // Set camera properties
    void setResolution(int width, int height);
    void setFPS(int fps);
    void setBrightness(double brightness);
    void setContrast(double contrast);
    
    // Get camera info
    int getWidth() const;
    int getHeight() const;
    int getFPS() const;
    bool isOpened() const;
    
    // Release camera
    void release();

private:
    std::unique_ptr<cv::VideoCapture> capture_;
    bool is_initialized_;
    int width_;
    int height_;
    int fps_;
};

} // namespace dough_vision

#endif // CAMERA_INTERFACE_H
