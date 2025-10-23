#include "color_segmentation.h"

namespace dough_vision {

ColorSegmentation::ColorSegmentation() 
    : morph_kernel_size_(5) {
    // Default HSV range for dough (yellowish/beige color)
    lower_bound_ = cv::Scalar(20, 50, 50);
    upper_bound_ = cv::Scalar(40, 255, 255);
}

ColorSegmentation::~ColorSegmentation() {}

void ColorSegmentation::setColorRange(const cv::Scalar& lower, const cv::Scalar& upper) {
    lower_bound_ = lower;
    upper_bound_ = upper;
}

cv::Mat ColorSegmentation::segment(const cv::Mat& frame) {
    if (frame.empty()) {
        return cv::Mat();
    }
    
    // Convert to HSV color space
    cv::Mat hsv;
    cv::cvtColor(frame, hsv, cv::COLOR_BGR2HSV);
    
    // Apply color thresholding
    cv::Mat mask;
    cv::inRange(hsv, lower_bound_, upper_bound_, mask);
    
    // Clean up the mask
    mask = cleanMask(mask);
    
    return mask;
}

cv::Mat ColorSegmentation::cleanMask(const cv::Mat& mask) {
    if (mask.empty()) {
        return cv::Mat();
    }
    
    cv::Mat cleaned = mask.clone();
    
    // Create morphological kernel
    cv::Mat kernel = cv::getStructuringElement(
        cv::MORPH_ELLIPSE,
        cv::Size(morph_kernel_size_, morph_kernel_size_)
    );
    
    // Remove noise with opening (erosion followed by dilation)
    cv::morphologyEx(cleaned, cleaned, cv::MORPH_OPEN, kernel, cv::Point(-1, -1), 2);
    
    // Fill gaps with closing (dilation followed by erosion)
    cv::morphologyEx(cleaned, cleaned, cv::MORPH_CLOSE, kernel, cv::Point(-1, -1), 2);
    
    return cleaned;
}

void ColorSegmentation::getColorRange(cv::Scalar& lower, cv::Scalar& upper) const {
    lower = lower_bound_;
    upper = upper_bound_;
}

} // namespace dough_vision
