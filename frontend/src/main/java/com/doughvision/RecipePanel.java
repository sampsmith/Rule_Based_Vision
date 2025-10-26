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
 * Panel for managing detection recipes
 */
public class RecipePanel extends JPanel {
    
    private ConfigurationManager configManager;
    
    // Recipe management
    private JList<String> recipeList;
    private DefaultListModel<String> recipeListModel;
    private JButton saveRecipeButton;
    private JButton loadRecipeButton;
    private JButton deleteRecipeButton;
    private JButton newRecipeButton;
    private List<DetectionRecipe> recipes;
    
    // Current recipe display
    private JTextArea recipeDetailsArea;
    private JLabel recipeNameLabel;
    private JLabel recipeStatusLabel;
    
    // Measurement settings for recipes
    private JSpinner pixelsPerMmSpinner;
    private JSpinner targetWidthSpinner, targetHeightSpinner;
    private JSpinner toleranceSpinner;
    
    public RecipePanel(ConfigurationManager configManager) {
        this.configManager = configManager;
        
        initializeComponents();
        layoutComponents();
    }
    
    private void initializeComponents() {
        // Initialize recipes
        recipes = new ArrayList<>();
        loadRecipes();
        
        // Recipe management UI
        recipeListModel = new DefaultListModel<>();
        updateRecipeListModel();
        
        recipeList = new JList<>(recipeListModel);
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeList.addListSelectionListener(e -> {
            loadRecipeButton.setEnabled(!recipeList.isSelectionEmpty());
            deleteRecipeButton.setEnabled(!recipeList.isSelectionEmpty());
            showRecipeDetails();
        });
        
        saveRecipeButton = new JButton("💾 Save Recipe");
        saveRecipeButton.addActionListener(e -> saveRecipe());
        
        loadRecipeButton = new JButton("📂 Load Recipe");
        loadRecipeButton.setEnabled(false);
        loadRecipeButton.addActionListener(e -> loadSelectedRecipe());
        
        deleteRecipeButton = new JButton("🗑️ Delete Recipe");
        deleteRecipeButton.setEnabled(false);
        deleteRecipeButton.addActionListener(e -> deleteSelectedRecipe());
        
        newRecipeButton = new JButton("➕ New Recipe");
        newRecipeButton.addActionListener(e -> createNewRecipe());
        
        // Recipe details display
        recipeDetailsArea = new JTextArea(15, 40);
        recipeDetailsArea.setEditable(false);
        recipeDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        recipeDetailsArea.setBackground(new Color(250, 250, 250));
        recipeDetailsArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        recipeNameLabel = new JLabel("Recipe: None selected");
        recipeNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        recipeStatusLabel = new JLabel("Ready");
        recipeStatusLabel.setForeground(new Color(46, 125, 50));
        
        // Initialize measurement spinners
        pixelsPerMmSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 50.0, 0.1));
        targetWidthSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        targetHeightSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        toleranceSpinner = new JSpinner(new SpinnerNumberModel(10.0, 1.0, 50.0, 1.0));
        
        // Load current values from config manager
        pixelsPerMmSpinner.setValue(configManager.getPixelsPerMm());
        double[] targetDims = configManager.getTargetDimensions();
        if (targetDims != null && targetDims.length >= 3) {
            targetWidthSpinner.setValue(targetDims[0]);
            targetHeightSpinner.setValue(targetDims[1]);
            toleranceSpinner.setValue(targetDims[2]);
        }
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Left panel - recipe list
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBorder(BorderFactory.createTitledBorder("📋 Saved Recipes"));
        
        JPanel listHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        listHeaderPanel.add(new JLabel("Recipe List:"));
        listPanel.add(listHeaderPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(recipeList);
        scrollPane.setPreferredSize(new Dimension(250, 400));
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Right panel - buttons and details
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        buttonPanel.add(newRecipeButton);
        buttonPanel.add(saveRecipeButton);
        buttonPanel.add(loadRecipeButton);
        buttonPanel.add(deleteRecipeButton);
        
        // Measurement settings panel
        JPanel measurementPanel = createMeasurementSettingsPanel();
        
        // Details panel
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Recipe Details"));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(recipeNameLabel, BorderLayout.WEST);
        headerPanel.add(recipeStatusLabel, BorderLayout.EAST);
        detailsPanel.add(headerPanel, BorderLayout.NORTH);
        
        JScrollPane detailsScrollPane = new JScrollPane(recipeDetailsArea);
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);
        
        // Combine panels vertically
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(measurementPanel, BorderLayout.NORTH);
        centerPanel.add(detailsPanel, BorderLayout.CENTER);
        
        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        rightPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(listPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }
    
    private JPanel createMeasurementSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("📏 Measurement Settings (per recipe)"));
        
        panel.add(new JLabel("Pixels/mm:"));
        panel.add(pixelsPerMmSpinner);
        panel.add(new JLabel("Target Width (mm):"));
        panel.add(targetWidthSpinner);
        
        panel.add(new JLabel("Target Height (mm):"));
        panel.add(targetHeightSpinner);
        panel.add(new JLabel("Tolerance (%):"));
        panel.add(toleranceSpinner);
        
        return panel;
    }
    
    private void createNewRecipe() {
        String name = JOptionPane.showInputDialog(this,
            "Enter recipe name:",
            "New Recipe",
            JOptionPane.QUESTION_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            // Create new recipe from current configuration
            DetectionRecipe recipe = createRecipeFromCurrentConfig();
            recipe.name = name;
            recipe.description = "Created from current configuration";
            recipe.dateCreated = new java.util.Date().toString();
            
            recipes.add(recipe);
            updateRecipeListModel();
            saveRecipes();
            
            // Select the new recipe
            int index = recipes.size() - 1;
            recipeList.setSelectedIndex(index);
            
            recipeStatusLabel.setText("Recipe created successfully!");
            recipeStatusLabel.setForeground(new Color(46, 125, 50));
        }
    }
    
    private void saveRecipe() {
        int selectedIndex = recipeList.getSelectedIndex();
        
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            DetectionRecipe recipe = recipes.get(selectedIndex);
            recipe.dateModified = new java.util.Date().toString();
            updateRecipeFromCurrentConfig(recipe);
            updateRecipeListModel();
            saveRecipes();
            
            recipeStatusLabel.setText("Recipe saved successfully!");
            recipeStatusLabel.setForeground(new Color(46, 125, 50));
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select a recipe to save.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void loadSelectedRecipe() {
        int selectedIndex = recipeList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            DetectionRecipe recipe = recipes.get(selectedIndex);
            
            // Update spinners with recipe values
            pixelsPerMmSpinner.setValue(recipe.pixelsPerMm);
            targetWidthSpinner.setValue(recipe.targetWidth);
            targetHeightSpinner.setValue(recipe.targetHeight);
            toleranceSpinner.setValue(recipe.tolerance);
            
            // Apply recipe to current configuration
            applyRecipe(recipe);
            
            // Update status
            recipeStatusLabel.setText("Recipe loaded successfully!");
            recipeStatusLabel.setForeground(new Color(46, 125, 50));
            
            // Notify config panel to reload
            JOptionPane.showMessageDialog(this,
                "Recipe '" + recipe.name + "' loaded successfully!",
                "Recipe Loaded",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteSelectedRecipe() {
        int selectedIndex = recipeList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            DetectionRecipe recipe = recipes.get(selectedIndex);
            
            int result = JOptionPane.showConfirmDialog(this,
                "Delete recipe '" + recipe.name + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                recipes.remove(selectedIndex);
                updateRecipeListModel();
                saveRecipes();
                
                recipeNameLabel.setText("Recipe: None selected");
                recipeDetailsArea.setText("");
                recipeStatusLabel.setText("Recipe deleted successfully!");
                recipeStatusLabel.setForeground(new Color(211, 47, 47));
            }
        }
    }
    
    private void showRecipeDetails() {
        int selectedIndex = recipeList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            DetectionRecipe recipe = recipes.get(selectedIndex);
            
            recipeNameLabel.setText("Recipe: " + recipe.name);
            
            StringBuilder details = new StringBuilder();
            details.append("Description: ").append(recipe.description).append("\n\n");
            details.append("Created: ").append(recipe.dateCreated).append("\n");
            details.append("Modified: ").append(recipe.dateModified).append("\n\n");
            
            details.append("=== Color Detection Settings ===\n");
            details.append("HSV Lower: [").append(recipe.hueMin).append(", ")
                    .append(recipe.satMin).append(", ").append(recipe.valMin).append("]\n");
            details.append("HSV Upper: [").append(recipe.hueMax).append(", ")
                    .append(recipe.satMax).append(", ").append(recipe.valMax).append("]\n\n");
            
            details.append("=== Detection Rules ===\n");
            details.append("Min Area: ").append(recipe.minArea).append(" pixels\n");
            details.append("Max Area: ").append(recipe.maxArea).append(" pixels\n");
            details.append("Min Circularity: ").append(recipe.minCircularity).append("\n");
            details.append("Max Circularity: ").append(recipe.maxCircularity).append("\n\n");
            
            details.append("=== Measurement Settings ===\n");
            details.append("Pixels per mm: ").append(recipe.pixelsPerMm).append("\n");
            details.append("Target Width: ").append(recipe.targetWidth).append(" mm\n");
            details.append("Target Height: ").append(recipe.targetHeight).append(" mm\n");
            details.append("Tolerance: ").append(recipe.tolerance).append("%\n\n");
            
            recipeDetailsArea.setText(details.toString());
        }
    }
    
    private void updateRecipeListModel() {
        recipeListModel.clear();
        for (DetectionRecipe recipe : recipes) {
            recipeListModel.addElement(recipe.name);
        }
    }
    
    private DetectionRecipe createRecipeFromCurrentConfig() {
        DetectionRecipe recipe = new DetectionRecipe();
        updateRecipeFromCurrentConfig(recipe);
        return recipe;
    }
    
    private void updateRecipeFromCurrentConfig(DetectionRecipe recipe) {
        ConfigurationManager.VisionConfiguration config = configManager.getConfig();
        
        // Copy color settings
        if (config.colorLower != null && config.colorLower.length >= 3) {
            recipe.hueMin = config.colorLower[0];
            recipe.satMin = config.colorLower[1];
            recipe.valMin = config.colorLower[2];
        }
        if (config.colorUpper != null && config.colorUpper.length >= 3) {
            recipe.hueMax = config.colorUpper[0];
            recipe.satMax = config.colorUpper[1];
            recipe.valMax = config.colorUpper[2];
        }
        
        // Copy detection rules
        recipe.minArea = config.minArea;
        recipe.maxArea = config.maxArea;
        recipe.minCircularity = config.minCircularity;
        recipe.maxCircularity = config.maxCircularity;
        
        // Copy measurement settings from spinners
        recipe.pixelsPerMm = (double)pixelsPerMmSpinner.getValue();
        recipe.targetWidth = (double)targetWidthSpinner.getValue();
        recipe.targetHeight = (double)targetHeightSpinner.getValue();
        recipe.tolerance = (double)toleranceSpinner.getValue();
    }
    
    private void applyRecipe(DetectionRecipe recipe) {
        // Apply color settings
        configManager.setColorRange(
            recipe.hueMin, recipe.satMin, recipe.valMin,
            recipe.hueMax, recipe.satMax, recipe.valMax
        );
        
        // Apply detection rules
        configManager.setMinArea(recipe.minArea);
        configManager.setMaxArea(recipe.maxArea);
        
        // Apply measurement settings
        configManager.setPixelsPerMm(recipe.pixelsPerMm);
        configManager.setTargetDimensions(
            recipe.targetWidth,
            recipe.targetHeight,
            recipe.tolerance
        );
    }
    
    private void loadRecipes() {
        try {
            File recipesFile = new File("detection_recipes.json");
            if (!recipesFile.exists()) {
                return;
            }
            
            try (FileReader reader = new FileReader(recipesFile)) {
                Gson gson = new Gson();
                JsonElement jsonElement = JsonParser.parseReader(reader);
                
                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        DetectionRecipe recipe = gson.fromJson(element, DetectionRecipe.class);
                        recipes.add(recipe);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading recipes: " + e.getMessage());
        }
    }
    
    private void saveRecipes() {
        try {
            File recipesFile = new File("detection_recipes.json");
            try (FileWriter writer = new FileWriter(recipesFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(recipes, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving recipes: " + e.getMessage());
        }
    }
    
    private static class DetectionRecipe {
        String name;
        String description;
        String dateCreated;
        String dateModified;
        
        // Color detection
        int hueMin, satMin, valMin;
        int hueMax, satMax, valMax;
        
        // Detection rules
        int minArea, maxArea;
        double minCircularity, maxCircularity;
        
        // Measurement settings
        double pixelsPerMm;
        double targetWidth, targetHeight;
        double tolerance;
    }
}
