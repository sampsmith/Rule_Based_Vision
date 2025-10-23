#include "vision_processor.h"
#include "config_manager.h"
#include <iostream>

namespace dough_vision {

VisionProcessor::VisionProcessor() 
    : is_initialized_(false) {
    color_segmenter_ = std::make_unique<ColorSegmentation>();
    contour_detector_ = std::make_unique<ContourDetector>();
    rule_engine_ = std::make_unique<RuleEngine>();
}

VisionProcessor::~VisionProcessor() {}

bool VisionProcessor::initialize(const std::string& config_path) {
    ConfigManager config_mgr;
    if (!config_mgr.loadConfig(config_path)) {
        std::cerr << "Warning: Could not load config, using defaults" << std::endl;
        // Set default values
        color_segmenter_->setColorRange(
            cv::Scalar(20, 50, 50),   // Lower HSV for dough (yellowish)
            cv::Scalar(40, 255, 255)  // Upper HSV
        );
        roi_ = cv::Rect(0, 0, 640, 480);
    } else {
        VisionConfig cfg = config_mgr.getConfig();
        color_segmenter_->setColorRange(cfg.color_lower, cfg.color_upper);
        roi_ = cfg.roi;
        
        DetectionRules rules;
        rules.min_area = cfg.min_area;
        rules.max_area = cfg.max_area;
        rules.min_circularity = cfg.min_circularity;
        rules.max_circularity = cfg.max_circularity;
        rules.min_aspect_ratio = 0.5;
        rules.max_aspect_ratio = 2.0;
        rules.expected_count = 0;
        rules.enforce_count = false;
        
        rule_engine_->setRules(rules);
    }
    
    is_initialized_ = true;
    return true;
}

DetectionResult VisionProcessor::processFrame(const cv::Mat& frame) {
    DetectionResult result;
    result.dough_count = 0;
    result.is_valid = false;
    result.confidence = 0.0;
    
    if (frame.empty() || !is_initialized_) {
        result.message = "Invalid frame or not initialized";
        return result;
    }
    
    // Apply ROI if set
    cv::Mat roi_frame = frame;
    if (roi_.width > 0 && roi_.height > 0) {
        roi_frame = frame(roi_);
    }
    
    // Color segmentation
    cv::Mat segmented = color_segmenter_->segment(roi_frame);
    segmented_frame_ = segmented.clone();
    
    // Find contours
    std::vector<std::vector<cv::Point>> contours = contour_detector_->findContours(segmented);
    
    // Extract features
    std::vector<ContourFeatures> features = contour_detector_->extractFeatures(contours);
    
    // Apply rules to filter valid dough pieces
    std::vector<std::vector<cv::Point>> valid_contours;
    std::vector<cv::Rect> bounding_boxes;
    
    for (size_t i = 0; i < features.size(); i++) {
        if (rule_engine_->validateContour(features[i])) {
            valid_contours.push_back(contours[i]);
            bounding_boxes.push_back(features[i].bounding_box);
        }
    }
    
    result.contours = valid_contours;
    result.bounding_boxes = bounding_boxes;
    result.dough_count = static_cast<int>(valid_contours.size());
    result.is_valid = rule_engine_->applyRules(features);
    result.message = rule_engine_->getValidationMessage();
    result.confidence = result.dough_count > 0 ? 0.85 : 0.0;
    
    // Draw results on frame
    processed_frame_ = roi_frame.clone();
    for (size_t i = 0; i < valid_contours.size(); i++) {
        cv::drawContours(processed_frame_, valid_contours, i, cv::Scalar(0, 255, 0), 2);
        cv::rectangle(processed_frame_, bounding_boxes[i], cv::Scalar(255, 0, 0), 2);
        
        // Draw center point
        cv::Moments m = cv::moments(valid_contours[i]);
        if (m.m00 != 0) {
            cv::Point2f center(m.m10 / m.m00, m.m01 / m.m00);
            cv::circle(processed_frame_, center, 5, cv::Scalar(0, 0, 255), -1);
        }
    }
    
    return result;
}

void VisionProcessor::updateColorRange(const cv::Scalar& lower, const cv::Scalar& upper) {
    color_segmenter_->setColorRange(lower, upper);
}

void VisionProcessor::updateROI(const cv::Rect& roi) {
    roi_ = roi;
}

void VisionProcessor::setMinDoughArea(double area) {
    DetectionRules rules = rule_engine_->getRules();
    rules.min_area = area;
    rule_engine_->setRules(rules);
}

void VisionProcessor::setMaxDoughArea(double area) {
    DetectionRules rules = rule_engine_->getRules();
    rules.max_area = area;
    rule_engine_->setRules(rules);
}

cv::Mat VisionProcessor::getProcessedFrame() const {
    return processed_frame_;
}

cv::Mat VisionProcessor::getSegmentedFrame() const {
    return segmented_frame_;
}

} // namespace dough_vision
