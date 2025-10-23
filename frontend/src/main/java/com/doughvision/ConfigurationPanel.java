package com.doughvision;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for editing configuration parameters
 */
public class ConfigurationPanel extends JPanel {
    
    private ConfigurationManager configManager;
    
    private JSpinner hueMinSpinner, hueMaxSpinner;
    private JSpinner satMinSpinner, satMaxSpinner;
    private JSpinner valMinSpinner, valMaxSpinner;
    
    private JSpinner minAreaSpinner, maxAreaSpinner;
    private JSpinner minCircularitySpinner, maxCircularitySpinner;
    
    private JSpinner cameraIndexSpinner;
    private JSpinner widthSpinner, heightSpinner, fpsSpinner;
    
    // Measurement settings
    private JSpinner pixelsPerMmSpinner;
    private JSpinner targetWidthSpinner, targetHeightSpinner;
    private JSpinner toleranceSpinner;
    
    private JButton applyButton;
    private JButton resetButton;
    
    public ConfigurationPanel(ConfigurationManager configManager) {
        this.configManager = configManager;
        
        initializeComponents();
        layoutComponents();
        loadCurrentConfig();
    }
    
    private void initializeComponents() {
        // Color range spinners
        hueMinSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 180, 1));
        hueMaxSpinner = new JSpinner(new SpinnerNumberModel(40, 0, 180, 1));
        satMinSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 255, 1));
        satMaxSpinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
        valMinSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 255, 1));
        valMaxSpinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
        
        // Detection rules spinners
        minAreaSpinner = new JSpinner(new SpinnerNumberModel(500, 0, 100000, 100));
        maxAreaSpinner = new JSpinner(new SpinnerNumberModel(50000, 0, 1000000, 1000));
        minCircularitySpinner = new JSpinner(new SpinnerNumberModel(0.3, 0.0, 1.0, 0.05));
        maxCircularitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.05));
        
        // Camera spinners
        cameraIndexSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        widthSpinner = new JSpinner(new SpinnerNumberModel(640, 320, 1920, 10));
        heightSpinner = new JSpinner(new SpinnerNumberModel(480, 240, 1080, 10));
        fpsSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 120, 1));
        
        // Measurement spinners
        pixelsPerMmSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 50.0, 0.1));
        targetWidthSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        targetHeightSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        toleranceSpinner = new JSpinner(new SpinnerNumberModel(10.0, 1.0, 50.0, 1.0));
        
        // Buttons
        applyButton = new JButton("Apply Changes");
        applyButton.addActionListener(e -> applyConfiguration());
        
        resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Color segmentation section
        mainPanel.add(createColorSegmentationPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Detection rules section
        mainPanel.add(createDetectionRulesPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Camera settings section
        mainPanel.add(createCameraSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Measurement settings section
        mainPanel.add(createMeasurementSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createColorSegmentationPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Color Segmentation (HSV)"));
        
        panel.add(new JLabel("Hue Min:"));
        panel.add(hueMinSpinner);
        panel.add(new JLabel("Hue Max:"));
        panel.add(hueMaxSpinner);
        
        panel.add(new JLabel("Saturation Min:"));
        panel.add(satMinSpinner);
        panel.add(new JLabel("Saturation Max:"));
        panel.add(satMaxSpinner);
        
        panel.add(new JLabel("Value Min:"));
        panel.add(valMinSpinner);
        panel.add(new JLabel("Value Max:"));
        panel.add(valMaxSpinner);
        
        return panel;
    }
    
    private JPanel createDetectionRulesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Detection Rules"));
        
        panel.add(new JLabel("Min Area (px):"));
        panel.add(minAreaSpinner);
        panel.add(new JLabel("Max Area (px):"));
        panel.add(maxAreaSpinner);
        
        panel.add(new JLabel("Min Circularity:"));
        panel.add(minCircularitySpinner);
        panel.add(new JLabel("Max Circularity:"));
        panel.add(maxCircularitySpinner);
        
        return panel;
    }
    
    private JPanel createCameraSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Camera Settings"));
        
        panel.add(new JLabel("Camera Index:"));
        panel.add(cameraIndexSpinner);
        panel.add(new JLabel("FPS:"));
        panel.add(fpsSpinner);
        
        panel.add(new JLabel("Width:"));
        panel.add(widthSpinner);
        panel.add(new JLabel("Height:"));
        panel.add(heightSpinner);
        
        return panel;
    }
    
    private JPanel createMeasurementSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Measurement Settings (Pass/Fail Criteria)"));
        
        panel.add(new JLabel("Pixels per mm:"));
        panel.add(pixelsPerMmSpinner);
        panel.add(new JLabel("Target Width (mm):"));
        panel.add(targetWidthSpinner);
        
        panel.add(new JLabel("Target Height (mm):"));
        panel.add(targetHeightSpinner);
        panel.add(new JLabel("Tolerance (%):"));
        panel.add(toleranceSpinner);
        
        return panel;
    }
    
    private void loadCurrentConfig() {
        // Load current configuration into spinners
        ConfigurationManager.VisionConfiguration config = configManager.getConfig();
        
        // Load color segmentation values
        if (config.colorLower != null && config.colorLower.length >= 3) {
            hueMinSpinner.setValue(config.colorLower[0]);
            satMinSpinner.setValue(config.colorLower[1]);
            valMinSpinner.setValue(config.colorLower[2]);
        }
        if (config.colorUpper != null && config.colorUpper.length >= 3) {
            hueMaxSpinner.setValue(config.colorUpper[0]);
            satMaxSpinner.setValue(config.colorUpper[1]);
            valMaxSpinner.setValue(config.colorUpper[2]);
        }
        
        // Load detection rules
        minAreaSpinner.setValue(config.minArea);
        maxAreaSpinner.setValue(config.maxArea);
        minCircularitySpinner.setValue(config.minCircularity);
        maxCircularitySpinner.setValue(config.maxCircularity);
        
        // Load camera settings
        cameraIndexSpinner.setValue(config.cameraIndex);
        widthSpinner.setValue(config.frameWidth);
        heightSpinner.setValue(config.frameHeight);
        fpsSpinner.setValue(config.fps);
        
        // Load measurement settings
        pixelsPerMmSpinner.setValue(configManager.getPixelsPerMm());
        double[] targetDims = configManager.getTargetDimensions();
        if (targetDims != null && targetDims.length >= 3) {
            targetWidthSpinner.setValue(targetDims[0]);
            targetHeightSpinner.setValue(targetDims[1]);
            toleranceSpinner.setValue(targetDims[2]);
        }
    }
    
    private void applyConfiguration() {
        // Apply configuration from spinners to configManager
        configManager.setColorRange(
            (int)hueMinSpinner.getValue(), (int)satMinSpinner.getValue(), (int)valMinSpinner.getValue(),
            (int)hueMaxSpinner.getValue(), (int)satMaxSpinner.getValue(), (int)valMaxSpinner.getValue()
        );
        
        configManager.setMinArea((int)minAreaSpinner.getValue());
        configManager.setMaxArea((int)maxAreaSpinner.getValue());
        
        // Apply measurement settings
        configManager.setPixelsPerMm((double)pixelsPerMmSpinner.getValue());
        configManager.setTargetDimensions(
            (double)targetWidthSpinner.getValue(),
            (double)targetHeightSpinner.getValue(),
            (double)toleranceSpinner.getValue()
        );
        
        JOptionPane.showMessageDialog(this, "Configuration applied successfully",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(this,
            "Reset all settings to default values?",
            "Confirm Reset", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            configManager.loadDefaultConfig();
            loadCurrentConfig();
            JOptionPane.showMessageDialog(this, "Configuration reset to defaults",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
