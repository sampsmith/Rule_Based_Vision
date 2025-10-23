#ifndef COLOR_SEGMENTATION_H
#define COLOR_SEGMENTATION_H

#include <opencv2/opencv.hpp>

namespace dough_vision {

class ColorSegmentation {
public:
    ColorSegmentation();
    ~ColorSegmentation();

    // Set HSV color range for dough detection
    void setColorRange(const cv::Scalar& lower, const cv::Scalar& upper);
    
    // Perform color-based segmentation
    cv::Mat segment(const cv::Mat& frame);
    
    // Apply morphological operations to clean up mask
    cv::Mat cleanMask(const cv::Mat& mask);
    
    // Get current color range
    void getColorRange(cv::Scalar& lower, cv::Scalar& upper) const;

private:
    cv::Scalar lower_bound_;
    cv::Scalar upper_bound_;
    
    // Morphological kernel sizes
    int morph_kernel_size_;
};

} // namespace dough_vision

#endif // COLOR_SEGMENTATION_H
