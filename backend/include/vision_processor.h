#ifndef VISION_PROCESSOR_H
#define VISION_PROCESSOR_H

#include <opencv2/opencv.hpp>
#include <memory>
#include <vector>
#include "color_segmentation.h"
#include "contour_detector.h"
#include "rule_engine.h"

namespace dough_vision {

struct DetectionResult {
    std::vector<std::vector<cv::Point>> contours;
    std::vector<cv::Rect> bounding_boxes;
    int dough_count;
    bool is_valid;
    double confidence;
    std::string message;
};

class VisionProcessor {
public:
    VisionProcessor();
    ~VisionProcessor();

    // Initialize with configuration
    bool initialize(const std::string& config_path);
    
    // Process a single frame
    DetectionResult processFrame(const cv::Mat& frame);
    
    // Update configuration parameters
    void updateColorRange(const cv::Scalar& lower, const cv::Scalar& upper);
    void updateROI(const cv::Rect& roi);
    void setMinDoughArea(double area);
    void setMaxDoughArea(double area);
    
    // Getters
    cv::Mat getProcessedFrame() const;
    cv::Mat getSegmentedFrame() const;

private:
    std::unique_ptr<ColorSegmentation> color_segmenter_;
    std::unique_ptr<ContourDetector> contour_detector_;
    std::unique_ptr<RuleEngine> rule_engine_;
    
    cv::Mat processed_frame_;
    cv::Mat segmented_frame_;
    cv::Rect roi_;
    
    bool is_initialized_;
};

} // namespace dough_vision

#endif // VISION_PROCESSOR_H
