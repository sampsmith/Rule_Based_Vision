package com.doughvision;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;

/**
 * Panel for editing configuration parameters
 */
public class ConfigurationPanel extends JPanel {
    
    private ConfigurationManager configManager;
    
    private JSpinner hueMinSpinner, hueMaxSpinner;
    private JSpinner satMinSpinner, satMaxSpinner;
    private JSpinner valMinSpinner, valMaxSpinner;
    
    private JSpinner cameraIndexSpinner;
    private JSpinner widthSpinner, heightSpinner, fpsSpinner;
    
    // Measurement settings
    private JSpinner pixelsPerMmSpinner;
    private JSpinner targetWidthSpinner, targetHeightSpinner;
    private JSpinner widthToleranceSpinner, heightToleranceSpinner;
    
    // Template management
    private JList<String> templateList;
    private DefaultListModel<String> templateListModel;
    private JButton saveTemplateButton;
    private JButton loadTemplateButton;
    private JButton deleteTemplateButton;
    private List<MeasurementTemplate> templates;
    
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
        
        // Camera spinners
        cameraIndexSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        widthSpinner = new JSpinner(new SpinnerNumberModel(640, 320, 1920, 10));
        heightSpinner = new JSpinner(new SpinnerNumberModel(480, 240, 1080, 10));
        fpsSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 120, 1));
        
        // Measurement spinners
        pixelsPerMmSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 50.0, 0.1));
        targetWidthSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        targetHeightSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        widthToleranceSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.1, 50.0, 0.1));
        heightToleranceSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.1, 50.0, 0.1));
        
        // Buttons
        applyButton = new JButton("Apply Changes");
        applyButton.addActionListener(e -> applyConfiguration());
        
        resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
        
        // Initialize templates
        templates = new ArrayList<>();
        loadTemplates();
        
        // Template management UI
        templateListModel = new DefaultListModel<>();
        updateTemplateListModel();
        
        templateList = new JList<>(templateListModel);
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.addListSelectionListener(e -> loadTemplateButton.setEnabled(!templateList.isSelectionEmpty()));
        
        saveTemplateButton = new JButton("💾 Save Template");
        saveTemplateButton.addActionListener(e -> saveTemplate());
        
        loadTemplateButton = new JButton("📂 Load Template");
        loadTemplateButton.setEnabled(false);
        loadTemplateButton.addActionListener(e -> loadSelectedTemplate());
        
        deleteTemplateButton = new JButton("🗑️ Delete Template");
        deleteTemplateButton.addActionListener(e -> deleteSelectedTemplate());
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Color segmentation section
        mainPanel.add(createColorSegmentationPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Camera settings section
        mainPanel.add(createCameraSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Measurement settings section
        mainPanel.add(createMeasurementSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Template management section
        mainPanel.add(createTemplateManagementPanel());
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
        JPanel panel = new JPanel(new GridLayout(3, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Measurement Settings (Pass/Fail Criteria)"));
        
        panel.add(new JLabel("Pixels per mm:"));
        panel.add(pixelsPerMmSpinner);
        panel.add(new JLabel("Target Width (mm):"));
        panel.add(targetWidthSpinner);
        
        panel.add(new JLabel("Target Height (mm):"));
        panel.add(targetHeightSpinner);
        panel.add(new JLabel("Width Tolerance (mm):"));
        panel.add(widthToleranceSpinner);
        
        panel.add(new JLabel("Height Tolerance (mm):"));
        panel.add(heightToleranceSpinner);
        
        return panel;
    }
    
    private JPanel createTemplateManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("💾 Measurement Templates"));
        
        // List panel
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.add(new JLabel("Saved Templates:"), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(templateList);
        scrollPane.setPreferredSize(new Dimension(200, 120));
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.add(saveTemplateButton);
        buttonPanel.add(loadTemplateButton);
        buttonPanel.add(deleteTemplateButton);
        
        panel.add(listPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void saveTemplate() {
        String name = JOptionPane.showInputDialog(this,
            "Enter template name:",
            "Save Template",
            JOptionPane.QUESTION_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            // Check if template with this name already exists
            for (MeasurementTemplate template : templates) {
                if (template.name.equals(name)) {
                    int result = JOptionPane.showConfirmDialog(this,
                        "Template '" + name + "' already exists. Overwrite?",
                        "Template Exists",
                        JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.NO_OPTION) {
                        return;
                    }
                    // Remove existing template
                    templates.remove(template);
                    break;
                }
            }
            
            // Create new template from current spinner values
            MeasurementTemplate template = new MeasurementTemplate();
            template.name = name;
            template.pixelsPerMm = (double)pixelsPerMmSpinner.getValue();
            template.targetWidth = (double)targetWidthSpinner.getValue();
            template.targetHeight = (double)targetHeightSpinner.getValue();
            template.widthTolerance = (double)widthToleranceSpinner.getValue();
            template.heightTolerance = (double)heightToleranceSpinner.getValue();
            
            templates.add(template);
            updateTemplateListModel();
            saveTemplates();
            
            JOptionPane.showMessageDialog(this,
                "Template '" + name + "' saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void loadSelectedTemplate() {
        int selectedIndex = templateList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < templates.size()) {
            MeasurementTemplate template = templates.get(selectedIndex);
            
            pixelsPerMmSpinner.setValue(template.pixelsPerMm);
            targetWidthSpinner.setValue(template.targetWidth);
            targetHeightSpinner.setValue(template.targetHeight);
            widthToleranceSpinner.setValue(template.widthTolerance);
            heightToleranceSpinner.setValue(template.heightTolerance);
            
            JOptionPane.showMessageDialog(this,
                "Template '" + template.name + "' loaded successfully!",
                "Template Loaded",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteSelectedTemplate() {
        int selectedIndex = templateList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < templates.size()) {
            MeasurementTemplate template = templates.get(selectedIndex);
            
            int result = JOptionPane.showConfirmDialog(this,
                "Delete template '" + template.name + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                templates.remove(selectedIndex);
                updateTemplateListModel();
                saveTemplates();
                
                JOptionPane.showMessageDialog(this,
                    "Template deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void updateTemplateListModel() {
        templateListModel.clear();
        for (MeasurementTemplate template : templates) {
            templateListModel.addElement(template.name);
        }
    }
    
    private void loadTemplates() {
        try {
            File templatesFile = new File("measurement_templates.json");
            if (!templatesFile.exists()) {
                return;
            }
            
            try (FileReader reader = new FileReader(templatesFile)) {
                Gson gson = new Gson();
                JsonElement jsonElement = JsonParser.parseReader(reader);
                
                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        MeasurementTemplate template = gson.fromJson(element, MeasurementTemplate.class);
                        templates.add(template);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading templates: " + e.getMessage());
        }
    }
    
    private void saveTemplates() {
        try {
            File templatesFile = new File("measurement_templates.json");
            try (FileWriter writer = new FileWriter(templatesFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(templates, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving templates: " + e.getMessage());
        }
    }
    
    private static class MeasurementTemplate {
        String name;
        double pixelsPerMm;
        double targetWidth;
        double targetHeight;
        double widthTolerance;
        double heightTolerance;
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
        
        // Load camera settings
        cameraIndexSpinner.setValue(config.cameraIndex);
        widthSpinner.setValue(config.frameWidth);
        heightSpinner.setValue(config.frameHeight);
        fpsSpinner.setValue(config.fps);
        
        // Load measurement settings
        pixelsPerMmSpinner.setValue(configManager.getPixelsPerMm());
        double[] targetDims = configManager.getTargetDimensions();
        if (targetDims != null && targetDims.length >= 4) {
            targetWidthSpinner.setValue(targetDims[0]);
            targetHeightSpinner.setValue(targetDims[1]);
            widthToleranceSpinner.setValue(targetDims[2]);
            heightToleranceSpinner.setValue(targetDims[3]);
        }
    }
    
    private void applyConfiguration() {
        // Apply configuration from spinners to configManager
        configManager.setColorRange(
            (int)hueMinSpinner.getValue(), (int)satMinSpinner.getValue(), (int)valMinSpinner.getValue(),
            (int)hueMaxSpinner.getValue(), (int)satMaxSpinner.getValue(), (int)valMaxSpinner.getValue()
        );
        
        // Apply measurement settings
        configManager.setPixelsPerMm((double)pixelsPerMmSpinner.getValue());
        configManager.setTargetDimensions(
            (double)targetWidthSpinner.getValue(),
            (double)targetHeightSpinner.getValue(),
            (double)widthToleranceSpinner.getValue(),
            (double)heightToleranceSpinner.getValue()
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
