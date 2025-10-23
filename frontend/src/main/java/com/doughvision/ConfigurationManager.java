package com.doughvision;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Manages application configuration and persists to JSON
 */
public class ConfigurationManager {
    
    private VisionConfiguration config;
    private Gson gson;
    private List<TeachModePanel.AnnotatedRegion> trainingData;
    private Map<String, LabelRule> learnedRules;
    private List<String> ignoreLabels;
    private boolean fastMode = false;  // Fast inference for constrained hardware
    
    // Calibration and measurement
    private double pixelsPerMm = 1.0;  // Default: 1px = 1mm (uncalibrated)
    private double targetWidth = 100.0;  // Target width in mm
    private double targetHeight = 100.0; // Target height in mm
    private double tolerance = 10.0;     // Tolerance percentage (e.g., 10%)
    
    public ConfigurationManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.config = new VisionConfiguration();
        this.trainingData = new ArrayList<>();
        this.learnedRules = new HashMap<>();
        this.ignoreLabels = new ArrayList<>();
        loadDefaultConfig();
        
        // Try to load saved rules on startup
        loadRulesFromFile();
    }
    
    public void loadDefaultConfig() {
        config = new VisionConfiguration();
        
        // Default color range for dough (HSV)
        config.colorLower = new int[]{20, 50, 50};
        config.colorUpper = new int[]{40, 255, 255};
        
        // Default ROI
        config.roiX = 0;
        config.roiY = 0;
        config.roiWidth = 640;
        config.roiHeight = 480;
        
        // Default detection rules
        config.minArea = 500;
        config.maxArea = 50000;
        config.minCircularity = 0.3;
        config.maxCircularity = 1.0;
        
        // Default camera settings
        config.cameraIndex = 0;
        config.frameWidth = 640;
        config.frameHeight = 480;
        config.fps = 30;
        
        // Processing settings
        config.morphKernelSize = 5;
        config.enablePreprocessing = true;
    }
    
    public boolean loadConfiguration(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            
            // Parse color segmentation
            if (json.has("color_segmentation")) {
                JsonObject colorSeg = json.getAsJsonObject("color_segmentation");
                config.colorLower = gson.fromJson(colorSeg.get("lower"), int[].class);
                config.colorUpper = gson.fromJson(colorSeg.get("upper"), int[].class);
            }
            
            // Parse ROI
            if (json.has("roi")) {
                JsonObject roi = json.getAsJsonObject("roi");
                config.roiX = roi.get("x").getAsInt();
                config.roiY = roi.get("y").getAsInt();
                config.roiWidth = roi.get("width").getAsInt();
                config.roiHeight = roi.get("height").getAsInt();
            }
            
            // Parse detection rules
            if (json.has("detection")) {
                JsonObject detection = json.getAsJsonObject("detection");
                config.minArea = detection.get("min_area").getAsInt();
                config.maxArea = detection.get("max_area").getAsInt();
                config.minCircularity = detection.get("min_circularity").getAsDouble();
                config.maxCircularity = detection.get("max_circularity").getAsDouble();
            }
            
            // Parse camera settings
            if (json.has("camera")) {
                JsonObject camera = json.getAsJsonObject("camera");
                config.cameraIndex = camera.get("index").getAsInt();
                config.frameWidth = camera.get("width").getAsInt();
                config.frameHeight = camera.get("height").getAsInt();
                config.fps = camera.get("fps").getAsInt();
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean saveConfiguration(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            JsonObject json = new JsonObject();
            
            // Color segmentation
            JsonObject colorSeg = new JsonObject();
            colorSeg.add("lower", gson.toJsonTree(config.colorLower));
            colorSeg.add("upper", gson.toJsonTree(config.colorUpper));
            json.add("color_segmentation", colorSeg);
            
            // ROI
            JsonObject roi = new JsonObject();
            roi.addProperty("x", config.roiX);
            roi.addProperty("y", config.roiY);
            roi.addProperty("width", config.roiWidth);
            roi.addProperty("height", config.roiHeight);
            json.add("roi", roi);
            
            // Detection rules
            JsonObject detection = new JsonObject();
            detection.addProperty("min_area", config.minArea);
            detection.addProperty("max_area", config.maxArea);
            detection.addProperty("min_circularity", config.minCircularity);
            detection.addProperty("max_circularity", config.maxCircularity);
            json.add("detection", detection);
            
            // Camera settings
            JsonObject camera = new JsonObject();
            camera.addProperty("index", config.cameraIndex);
            camera.addProperty("width", config.frameWidth);
            camera.addProperty("height", config.frameHeight);
            camera.addProperty("fps", config.fps);
            json.add("camera", camera);
            
            // Processing settings
            JsonObject processing = new JsonObject();
            processing.addProperty("morph_kernel_size", config.morphKernelSize);
            processing.addProperty("enable_preprocessing", config.enablePreprocessing);
            json.add("processing", processing);
            
            // Measurement settings
            JsonObject measurement = new JsonObject();
            measurement.addProperty("pixels_per_mm", pixelsPerMm);
            measurement.addProperty("target_width_mm", targetWidth);
            measurement.addProperty("target_height_mm", targetHeight);
            measurement.addProperty("tolerance_percent", tolerance);
            json.add("measurement", measurement);
            
            gson.toJson(json, writer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Getters and setters
    public void setColorRange(int h1, int s1, int v1, int h2, int s2, int v2) {
        config.colorLower = new int[]{h1, s1, v1};
        config.colorUpper = new int[]{h2, s2, v2};
    }
    
    public void setMinArea(int area) {
        config.minArea = area;
    }
    
    public void setMaxArea(int area) {
        config.maxArea = area;
    }
    
    public void setROIRegions(List<Rectangle> regions) {
        if (!regions.isEmpty()) {
            Rectangle r = regions.get(0); // Use first region as main ROI
            config.roiX = r.x;
            config.roiY = r.y;
            config.roiWidth = r.width;
            config.roiHeight = r.height;
        }
    }
    
    public void setAnnotatedRegions(List<TeachModePanel.AnnotatedRegion> regions) {
        this.trainingData = new ArrayList<>(regions);
    }
    
    /**
     * Teach the model based on annotated regions
     */
    public boolean teachModel(BufferedImage image, List<TeachModePanel.AnnotatedRegion> regions) {
        try {
            System.out.println("Teaching model with " + regions.size() + " annotated regions...");
            
            // Store training data
            this.trainingData = new ArrayList<>(regions);
            
            // Separate positive samples and ignore samples
            Map<String, List<ColorSample>> labelSamples = new HashMap<>();
            ignoreLabels.clear();
            
            // Collect samples from annotated regions
            for (TeachModePanel.AnnotatedRegion region : regions) {
                List<ColorSample> samples = extractColorSamples(image, region);
                
                // Check if this is an ignore label
                String lowerLabel = region.label.toLowerCase();
                if (lowerLabel.contains("ignore") || lowerLabel.contains("background") || 
                    lowerLabel.contains("reject") || lowerLabel.contains("exclude")) {
                    ignoreLabels.add(region.label);
                    System.out.println("Marking '" + region.label + "' as IGNORE label");
                }
                
                labelSamples.computeIfAbsent(region.label, k -> new ArrayList<>()).addAll(samples);
            }
            
            // Create rules for each label using robust clustering
            for (Map.Entry<String, List<ColorSample>> entry : labelSamples.entrySet()) {
                String label = entry.getKey();
                List<ColorSample> samples = entry.getValue();
                
                LabelRule rule = computeRobustRule(label, samples);
                learnedRules.put(label, rule);
                
                String type = ignoreLabels.contains(label) ? "IGNORE" : "DETECT";
                System.out.println("Learned " + type + " rule for '" + label + "': HSV range [" +
                    rule.hMin + "," + rule.sMin + "," + rule.vMin + "] to [" +
                    rule.hMax + "," + rule.sMax + "," + rule.vMax + "] (" + samples.size() + " samples)");
            }
            
            // Save rules to file
            saveRulesToFile();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract color samples from annotated region
     */
    private List<ColorSample> extractColorSamples(BufferedImage image, TeachModePanel.AnnotatedRegion region) {
        List<ColorSample> samples = new ArrayList<>();
        Rectangle bounds = region.boundingBox;
        
        System.out.println("  Extracting samples from region '" + region.label + "': bounds=" + bounds + ", isPolygon=" + region.isPolygon);
        
        int pixelsChecked = 0;
        int pixelsInside = 0;
        
        for (int y = bounds.y; y < bounds.y + bounds.height && y < image.getHeight(); y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width && x < image.getWidth(); x++) {
                pixelsChecked++;
                
                // Check if point is inside polygon (if applicable)
                if (region.isPolygon && !isPointInPolygon(new Point(x, y), region.polygonPoints)) {
                    continue;
                }
                
                pixelsInside++;
                
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int[] hsv = rgbToHsv(r, g, b);
                samples.add(new ColorSample(hsv[0], hsv[1], hsv[2]));
            }
        }
        
        System.out.println("  Checked " + pixelsChecked + " pixels, " + pixelsInside + " inside region, extracted " + samples.size() + " samples");
        
        return samples;
    }
    
    /**
     * Check if point is inside polygon
     */
    private boolean isPointInPolygon(Point p, List<Point> polygon) {
        int n = polygon.size();
        boolean inside = false;
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygon.get(i);
            Point pj = polygon.get(j);
            
            if ((pi.y > p.y) != (pj.y > p.y) &&
                p.x < (pj.x - pi.x) * (p.y - pi.y) / (pj.y - pi.y) + pi.x) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    /**
     * Convert RGB to HSV
     */
    private int[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        
        float h = 0;
        if (delta != 0) {
            if (max == rf) {
                h = 60 * (((gf - bf) / delta) % 6);
            } else if (max == gf) {
                h = 60 * (((bf - rf) / delta) + 2);
            } else {
                h = 60 * (((rf - gf) / delta) + 4);
            }
        }
        if (h < 0) h += 360;
        
        float s = (max == 0) ? 0 : (delta / max);
        float v = max;
        
        // OpenCV HSV ranges: H: 0-179, S: 0-255, V: 0-255
        return new int[]{(int)(h / 2), (int)(s * 255), (int)(v * 255)};
    }
    
    /**
     * Compute robust rule using percentile-based outlier removal
     */
    private LabelRule computeRobustRule(String label, List<ColorSample> samples) {
        if (samples.isEmpty()) {
            return new LabelRule(label, 0, 0, 0, 179, 255, 255);
        }
        
        System.out.println("Computing rule for '" + label + "' with " + samples.size() + " samples");
        
        // Sort samples by each channel
        List<Integer> hValues = new ArrayList<>();
        List<Integer> sValues = new ArrayList<>();
        List<Integer> vValues = new ArrayList<>();
        
        for (ColorSample sample : samples) {
            hValues.add(sample.h);
            sValues.add(sample.s);
            vValues.add(sample.v);
        }
        
        hValues.sort(Integer::compareTo);
        sValues.sort(Integer::compareTo);
        vValues.sort(Integer::compareTo);
        
        // Use 10th and 90th percentile (tighter than before)
        int p10 = Math.max(0, (int)(samples.size() * 0.10));
        int p90 = Math.min(samples.size() - 1, (int)(samples.size() * 0.90));
        
        int hMin = hValues.get(p10);
        int hMax = hValues.get(p90);
        int sMin = sValues.get(p10);
        int sMax = sValues.get(p90);
        int vMin = vValues.get(p10);
        int vMax = vValues.get(p90);
        
        System.out.println("  Raw ranges - H:[" + hMin + "-" + hMax + "] S:[" + sMin + "-" + sMax + "] V:[" + vMin + "-" + vMax + "]");
        
        // Wider tolerance for color robustness (handles lighting, camera differences)
        int hTol = 15;  // Fixed +/-15 for hue (was 10)
        int sTol = 50;  // Fixed +/-50 for saturation (was 30)  
        int vTol = 60;  // Fixed +/-60 for value/brightness (was 40)
        
        hMin = Math.max(0, hMin - hTol);
        hMax = Math.min(179, hMax + hTol);
        sMin = Math.max(0, sMin - sTol);
        sMax = Math.min(255, sMax + sTol);
        vMin = Math.max(0, vMin - vTol);
        vMax = Math.min(255, vMax + vTol);
        
        System.out.println("  Final ranges - H:[" + hMin + "-" + hMax + "] S:[" + sMin + "-" + sMax + "] V:[" + vMin + "-" + vMax + "]");
        
        return new LabelRule(label, hMin, sMin, vMin, hMax, sMax, vMax);
    }
    
    /**
     * Preprocessing pipeline
     */
    private BufferedImage preprocessImage(BufferedImage image) {
        // Apply Gaussian blur to reduce noise
        BufferedImage blurred = gaussianBlur(image, 1.0);
        
        // Normalize brightness/contrast
        BufferedImage normalized = normalizeImage(blurred);
        
        return normalized;
    }
    
    /**
     * Gaussian blur using simple 5x5 kernel approximation
     */
    private BufferedImage gaussianBlur(BufferedImage image, double sigma) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Simple 5x5 Gaussian kernel approximation
        double[][] kernel = {
            {1, 4, 6, 4, 1},
            {4, 16, 24, 16, 4},
            {6, 24, 36, 24, 6},
            {4, 16, 24, 16, 4},
            {1, 4, 6, 4, 1}
        };
        double kernelSum = 256.0;
        
        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                double r = 0, g = 0, b = 0;
                
                for (int ky = -2; ky <= 2; ky++) {
                    for (int kx = -2; kx <= 2; kx++) {
                        int rgb = image.getRGB(x + kx, y + ky);
                        double weight = kernel[ky + 2][kx + 2] / kernelSum;
                        
                        r += ((rgb >> 16) & 0xFF) * weight;
                        g += ((rgb >> 8) & 0xFF) * weight;
                        b += (rgb & 0xFF) * weight;
                    }
                }
                
                int newRgb = (clamp((int)r) << 16) | (clamp((int)g) << 8) | clamp((int)b);
                result.setRGB(x, y, newRgb);
            }
        }
        
        // Copy edges directly
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x < 2 || x >= width - 2 || y < 2 || y >= height - 2) {
                    result.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Normalize brightness using histogram stretching
     */
    private BufferedImage normalizeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Find min/max values for each channel
        int minR = 255, maxR = 0;
        int minG = 255, maxG = 0;
        int minB = 255, maxB = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                minR = Math.min(minR, r);
                maxR = Math.max(maxR, r);
                minG = Math.min(minG, g);
                maxG = Math.max(maxG, g);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }
        }
        
        // Apply histogram stretching
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Stretch each channel independently
                int newR = maxR > minR ? (r - minR) * 255 / (maxR - minR) : r;
                int newG = maxG > minG ? (g - minG) * 255 / (maxG - minG) : g;
                int newB = maxB > minB ? (b - minB) * 255 / (maxB - minB) : b;
                
                int newRgb = (clamp(newR) << 16) | (clamp(newG) << 8) | clamp(newB);
                result.setRGB(x, y, newRgb);
            }
        }
        
        return result;
    }
    
    /**
     * Clamp value to 0-255 range
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
    
    /**
     * Fast image resize using nearest neighbor (fast) or bilinear (quality)
     */
    private BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, image.getType());
        Graphics2D g = resized.createGraphics();
        
        // Use fast rendering for speed
        if (fastMode) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
    
    public void setFastMode(boolean enabled) {
        this.fastMode = enabled;
        System.out.println("Fast mode: " + (enabled ? "ENABLED" : "DISABLED"));
    }
    
    /**
     * Run segmentation on image with mask generation
     */
    public BufferedImage runSegmentation(BufferedImage image) {
        if (learnedRules.isEmpty()) {
            System.err.println("No learned rules. Please teach the model first.");
            return null;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Fast mode: downsample for speed
            BufferedImage processImage = image;
            double scale = 1.0;
            if (fastMode && (image.getWidth() > 1280 || image.getHeight() > 960)) {
                scale = Math.min(1280.0 / image.getWidth(), 960.0 / image.getHeight());
                int newW = (int)(image.getWidth() * scale);
                int newH = (int)(image.getHeight() * scale);
                processImage = resizeImage(image, newW, newH);
                System.out.println("Fast mode: downsampled to " + newW + "x" + newH);
            }
            
            // Skip preprocessing in fast mode OR if it hurts accuracy
            // Training is done on raw images, so inference should match
            BufferedImage processed = processImage;
            
            int width = processed.getWidth();
            int height = processed.getHeight();
            
            // Create binary mask for detection
            boolean[][] detectionMask = new boolean[height][width];
            boolean[][] ignoreMask = new boolean[height][width];
            
            // First pass: classify each pixel (use preprocessed image)
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = processed.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    int[] hsv = rgbToHsv(r, g, b);
                    
                    // Check ignore labels first
                    for (String ignoreLabel : ignoreLabels) {
                        LabelRule rule = learnedRules.get(ignoreLabel);
                        if (rule != null && matchesRule(hsv, rule)) {
                            ignoreMask[y][x] = true;
                            break;
                        }
                    }
                    
                    // If not ignored, check detection labels
                    if (!ignoreMask[y][x]) {
                        for (Map.Entry<String, LabelRule> entry : learnedRules.entrySet()) {
                            if (ignoreLabels.contains(entry.getKey())) {
                                continue; // Skip ignore labels
                            }
                            
                            LabelRule rule = entry.getValue();
                            if (matchesRule(hsv, rule)) {
                                detectionMask[y][x] = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            // Apply morphological operations to clean up mask
            // Use smaller kernels in fast mode
            int closeKernel = fastMode ? 2 : 3;
            int openKernel = fastMode ? 1 : 2;
            detectionMask = morphologicalClose(detectionMask, closeKernel);
            detectionMask = morphologicalOpen(detectionMask, openKernel);
            
            // Create result image
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    if (detectionMask[y][x]) {
                        // Highlight detected regions in green
                        int highlightR = Math.min(255, r + 50);
                        int highlightG = Math.min(255, g + 100);
                        int highlightB = b;
                        result.setRGB(x, y, 0xFF000000 | (highlightR << 16) | (highlightG << 8) | highlightB);
                    } else if (ignoreMask[y][x]) {
                        // Dim ignored regions
                        result.setRGB(x, y, 0xFF000000 | (r/2 << 16) | (g/2 << 8) | (b/2));
                    } else {
                        // Keep original for unclassified
                        result.setRGB(x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
                    }
                }
            }
            
            // Draw contours around detected regions
            result = drawContours(result, detectionMask);
            
            long elapsed = System.currentTimeMillis() - startTime;
            int detectedPixels = countTrue(detectionMask);
            System.out.println("Segmentation complete in " + elapsed + "ms. Detected pixels: " + detectedPixels);
            
            // If we downsampled, scale back up
            if (fastMode && scale < 1.0) {
                result = resizeImage(result, image.getWidth(), image.getHeight());
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if HSV values match a rule
     */
    private boolean matchesRule(int[] hsv, LabelRule rule) {
        return hsv[0] >= rule.hMin && hsv[0] <= rule.hMax &&
               hsv[1] >= rule.sMin && hsv[1] <= rule.sMax &&
               hsv[2] >= rule.vMin && hsv[2] <= rule.vMax;
    }
    
    /**
     * Morphological closing (dilation followed by erosion)
     */
    private boolean[][] morphologicalClose(boolean[][] mask, int kernelSize) {
        mask = dilate(mask, kernelSize);
        mask = erode(mask, kernelSize);
        return mask;
    }
    
    /**
     * Morphological opening (erosion followed by dilation)
     */
    private boolean[][] morphologicalOpen(boolean[][] mask, int kernelSize) {
        mask = erode(mask, kernelSize);
        mask = dilate(mask, kernelSize);
        return mask;
    }
    
    /**
     * Dilate mask
     */
    private boolean[][] dilate(boolean[][] mask, int kernelSize) {
        int height = mask.length;
        int width = mask[0].length;
        boolean[][] result = new boolean[height][width];
        int half = kernelSize / 2;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean hasTrue = false;
                for (int ky = -half; ky <= half; ky++) {
                    for (int kx = -half; kx <= half; kx++) {
                        int ny = y + ky;
                        int nx = x + kx;
                        if (ny >= 0 && ny < height && nx >= 0 && nx < width && mask[ny][nx]) {
                            hasTrue = true;
                            break;
                        }
                    }
                    if (hasTrue) break;
                }
                result[y][x] = hasTrue;
            }
        }
        return result;
    }
    
    /**
     * Erode mask
     */
    private boolean[][] erode(boolean[][] mask, int kernelSize) {
        int height = mask.length;
        int width = mask[0].length;
        boolean[][] result = new boolean[height][width];
        int half = kernelSize / 2;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean allTrue = true;
                for (int ky = -half; ky <= half; ky++) {
                    for (int kx = -half; kx <= half; kx++) {
                        int ny = y + ky;
                        int nx = x + kx;
                        if (ny < 0 || ny >= height || nx < 0 || nx >= width || !mask[ny][nx]) {
                            allTrue = false;
                            break;
                        }
                    }
                    if (!allTrue) break;
                }
                result[y][x] = allTrue;
            }
        }
        return result;
    }
    
    /**
     * Find connected components and their bounding boxes
     */
    private List<Rectangle> findBoundingBoxes(boolean[][] mask) {
        int height = mask.length;
        int width = mask[0].length;
        boolean[][] visited = new boolean[height][width];
        List<Rectangle> boxes = new ArrayList<>();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (mask[y][x] && !visited[y][x]) {
                    // Found new component, flood fill to find bounds
                    Rectangle bounds = floodFillBounds(mask, visited, x, y);
                    if (bounds.width > 10 && bounds.height > 10) {  // Filter tiny detections
                        boxes.add(bounds);
                    }
                }
            }
        }
        
        return boxes;
    }
    
    /**
     * Flood fill to find component bounds
     */
    private Rectangle floodFillBounds(boolean[][] mask, boolean[][] visited, int startX, int startY) {
        int height = mask.length;
        int width = mask[0].length;
        
        int minX = startX, maxX = startX;
        int minY = startY, maxY = startY;
        
        java.util.Queue<Point> queue = new java.util.LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startY][startX] = true;
        
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
            
            // Check 4 neighbors
            int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
            for (int[] dir : dirs) {
                int nx = p.x + dir[0];
                int ny = p.y + dir[1];
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                    mask[ny][nx] && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    queue.add(new Point(nx, ny));
                }
            }
        }
        
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
    
    /**
     * Draw contours around detected regions with measurements
     */
    private BufferedImage drawContours(BufferedImage image, boolean[][] mask) {
        int height = mask.length;
        int width = mask[0].length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        
        // Copy original
        g2d.drawImage(image, 0, 0, null);
        
        // Find bounding boxes for each detection
        List<Rectangle> boxes = findBoundingBoxes(mask);
        
        // Draw contour edges in bright green
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (mask[y][x]) {
                    // Check if this is an edge pixel
                    boolean isEdge = !mask[y-1][x] || !mask[y+1][x] || 
                                     !mask[y][x-1] || !mask[y][x+1];
                    if (isEdge) {
                        result.setRGB(x, y, 0xFF00FF00); // Bright green contour
                    }
                }
            }
        }
        
        // Draw bounding boxes and measurements with pass/fail
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(3));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        
        int detectionNum = 1;
        int passCount = 0;
        int failCount = 0;
        
        for (Rectangle box : boxes) {
            // Convert to mm
            double widthMm = box.width / pixelsPerMm;
            double heightMm = box.height / pixelsPerMm;
            
            // Check pass/fail based on tolerance
            double widthDiff = Math.abs(widthMm - targetWidth) / targetWidth * 100.0;
            double heightDiff = Math.abs(heightMm - targetHeight) / targetHeight * 100.0;
            boolean pass = (widthDiff <= tolerance && heightDiff <= tolerance);
            
            if (pass) {
                passCount++;
            } else {
                failCount++;
            }
            
            // Color based on pass/fail
            Color boxColor = pass ? new Color(0, 255, 0) : new Color(255, 0, 0);
            g2d.setColor(boxColor);
            
            // Draw bounding box
            g2d.drawRect(box.x, box.y, box.width, box.height);
            
            // Draw measurement labels
            String widthLabel = String.format("W: %.1fmm", widthMm);
            String heightLabel = String.format("H: %.1fmm", heightMm);
            String statusLabel = pass ? "✓ PASS" : "✗ REJECT";
            String idLabel = String.format("#%d", detectionNum);
            
            // Background for text
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(box.x, box.y - 38, 120, 36);
            g2d.fillRect(box.x + box.width + 2, box.y + box.height/2 - 18, 85, 36);
            
            // Draw text
            g2d.setColor(boxColor);
            g2d.drawString(idLabel + " " + statusLabel, box.x + 3, box.y - 22);
            g2d.drawString(widthLabel, box.x + 3, box.y - 6);
            g2d.drawString(heightLabel, box.x + box.width + 5, box.y + box.height/2 - 2);
            
            // Show deviation if fail
            if (!pass) {
                String devLabel = String.format("Δ%.0f%%", Math.max(widthDiff, heightDiff));
                g2d.drawString(devLabel, box.x + box.width + 5, box.y + box.height/2 + 14);
            }
            
            detectionNum++;
        }
        
        // Draw summary at top
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(10, 10, 280, 30);
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("Total: %d | ", boxes.size()), 15, 30);
        g2d.setColor(new Color(0, 255, 0));
        g2d.drawString(String.format("Pass: %d | ", passCount), 95, 30);
        g2d.setColor(new Color(255, 0, 0));
        g2d.drawString(String.format("Reject: %d", failCount), 185, 30);
        
        g2d.dispose();
        return result;
    }
    
    /**
     * Count true values in mask
     */
    private int countTrue(boolean[][] mask) {
        int count = 0;
        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[0].length; x++) {
                if (mask[y][x]) count++;
            }
        }
        return count;
    }
    
    
    /**
     * Load learned rules from JSON file on startup
     */
    private void loadRulesFromFile() {
        try {
            File rulesFile = new File("learned_rules.json");
            if (!rulesFile.exists()) {
                System.out.println("No saved rules found at startup.");
                return;
            }
            
            try (FileReader reader = new FileReader(rulesFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                
                if (json.has("rules")) {
                    JsonArray rulesArray = json.getAsJsonArray("rules");
                    for (int i = 0; i < rulesArray.size(); i++) {
                        JsonObject ruleObj = rulesArray.get(i).getAsJsonObject();
                        String label = ruleObj.get("label").getAsString();
                        
                        JsonArray lower = ruleObj.getAsJsonArray("lower");
                        JsonArray upper = ruleObj.getAsJsonArray("upper");
                        
                        LabelRule rule = new LabelRule(
                            label,
                            lower.get(0).getAsInt(),
                            lower.get(1).getAsInt(),
                            lower.get(2).getAsInt(),
                            upper.get(0).getAsInt(),
                            upper.get(1).getAsInt(),
                            upper.get(2).getAsInt()
                        );
                        
                        learnedRules.put(label, rule);
                    }
                }
                
                if (json.has("ignore_labels")) {
                    JsonArray ignoreArray = json.getAsJsonArray("ignore_labels");
                    for (int i = 0; i < ignoreArray.size(); i++) {
                        ignoreLabels.add(ignoreArray.get(i).getAsString());
                    }
                }
                
                System.out.println("Loaded " + learnedRules.size() + " rules from: " + rulesFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error loading rules: " + e.getMessage());
        }
    }
    
    /**
     * Save learned rules to JSON file
     */
    private void saveRulesToFile() {
        try {
            File rulesFile = new File("learned_rules.json");
            try (FileWriter writer = new FileWriter(rulesFile)) {
                JsonObject json = new JsonObject();
                JsonArray rulesArray = new JsonArray();
                JsonArray ignoreArray = new JsonArray();
                
                for (LabelRule rule : learnedRules.values()) {
                    JsonObject ruleObj = new JsonObject();
                    ruleObj.addProperty("label", rule.label);
                    ruleObj.addProperty("type", ignoreLabels.contains(rule.label) ? "ignore" : "detect");
                    
                    JsonArray lower = new JsonArray();
                    lower.add(rule.hMin);
                    lower.add(rule.sMin);
                    lower.add(rule.vMin);
                    ruleObj.add("lower", lower);
                    
                    JsonArray upper = new JsonArray();
                    upper.add(rule.hMax);
                    upper.add(rule.sMax);
                    upper.add(rule.vMax);
                    ruleObj.add("upper", upper);
                    
                    rulesArray.add(ruleObj);
                }
                
                for (String ignoreLabel : ignoreLabels) {
                    ignoreArray.add(ignoreLabel);
                }
                
                json.add("rules", rulesArray);
                json.add("ignore_labels", ignoreArray);
                gson.toJson(json, writer);
                
                System.out.println("Saved rules to: " + rulesFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public VisionConfiguration getConfig() {
        return config;
    }
    
    public void setPixelsPerMm(double pixelsPerMm) {
        this.pixelsPerMm = pixelsPerMm;
        System.out.println("Calibration set: " + pixelsPerMm + " pixels/mm");
    }
    
    public void setTargetDimensions(double widthMm, double heightMm, double tolerancePercent) {
        this.targetWidth = widthMm;
        this.targetHeight = heightMm;
        this.tolerance = tolerancePercent;
        System.out.println(String.format("Target: %.1fmm x %.1fmm ±%.1f%%", widthMm, heightMm, tolerancePercent));
    }
    
    public double getPixelsPerMm() {
        return pixelsPerMm;
    }
    
    public double[] getTargetDimensions() {
        return new double[]{targetWidth, targetHeight, tolerance};
    }
    
    /**
     * Inner class for configuration data
     */
    public static class VisionConfiguration {
        public int[] colorLower;
        public int[] colorUpper;
        
        public int roiX, roiY, roiWidth, roiHeight;
        
        public int minArea, maxArea;
        public double minCircularity, maxCircularity;
        
        public int cameraIndex;
        public int frameWidth, frameHeight, fps;
        
        public int morphKernelSize;
        public boolean enablePreprocessing;
    }
    
    /**
     * Color sample in HSV space
     */
    private static class ColorSample {
        int h, s, v;
        
        ColorSample(int h, int s, int v) {
            this.h = h;
            this.s = s;
            this.v = v;
        }
    }
    
    /**
     * Learned rule for a label
     */
    private static class LabelRule {
        String label;
        int hMin, sMin, vMin;
        int hMax, sMax, vMax;
        
        LabelRule(String label, int hMin, int sMin, int vMin, int hMax, int sMax, int vMax) {
            this.label = label;
            this.hMin = hMin;
            this.sMin = sMin;
            this.vMin = vMin;
            this.hMax = hMax;
            this.sMax = sMax;
            this.vMax = vMax;
        }
    }
    
}
