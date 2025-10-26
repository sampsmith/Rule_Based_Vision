package com.doughvision;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window
 */
public class MainFrame extends JFrame {
    
    private TeachModePanel teachModePanel;
    private InferencePanel inferencePanel;
    private ConfigurationPanel configurationPanel;
    private RecipePanel recipePanel;
    private ConfigurationManager configManager;
    
    private JTabbedPane tabbedPane;
    private double dpiScale = 1.0; // DPI scaling factor
    
    public MainFrame() {
        super("Dough Vision Detector");
        
        configManager = new ConfigurationManager();
        
        // Detect and apply DPI scaling
        detectDPIScaling();
        
        // Set modern look and feel
        setModernLookAndFeel();
        
        initializeComponents();
        layoutComponents();
        setupMenuBar();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Scale window size based on DPI
        int baseWidth = 1600;
        int baseHeight = 1000;
        setSize((int)(baseWidth * dpiScale), (int)(baseHeight * dpiScale));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized for touch screens
        
        // Load default configuration
        configManager.loadDefaultConfig();
    }
    
    private void detectDPIScaling() {
        try {
            // Get screen DPI
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();
            
            // Calculate DPI
            int dpiX = Toolkit.getDefaultToolkit().getScreenResolution();
            int dpiY = dpiX;
            
            // Default DPI is 96 (Windows) or 72 (Mac)
            // Scale factor: actual DPI / 96
            dpiScale = dpiX / 96.0;
            
            // Ensure minimum scale of 1.0
            if (dpiScale < 1.0) {
                dpiScale = 1.0;
            }
            
            // Clamp to reasonable range (1x to 3x)
            if (dpiScale > 3.0) {
                dpiScale = 3.0;
            }
            
            System.out.println("Screen DPI: " + dpiX + ", Scale factor: " + dpiScale);
            
            // Set system property for Java scaling
            System.setProperty("sun.java2d.uiScale", String.valueOf(dpiScale));
            
        } catch (Exception e) {
            System.err.println("Error detecting DPI: " + e.getMessage());
            dpiScale = 1.0;
        }
    }
    
    private void setModernLookAndFeel() {
        try {
            // Use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Calculate scaled sizes based on DPI
            int baseButtonFontSize = 16;
            int baseLabelFontSize = 15;
            int baseTabFontSize = 18;
            int baseSpinnerFontSize = 16;
            
            int buttonFontSize = (int)(baseButtonFontSize * dpiScale);
            int labelFontSize = (int)(baseLabelFontSize * dpiScale);
            int tabFontSize = (int)(baseTabFontSize * dpiScale);
            int spinnerFontSize = (int)(baseSpinnerFontSize * dpiScale);
            
            int buttonPadding = (int)(12 * dpiScale);
            int horizontalPadding = (int)(20 * dpiScale);
            
            // Touch-friendly sizing (scaled by DPI)
            UIManager.put("Panel.background", new Color(245, 245, 247));
            UIManager.put("Button.background", new Color(0, 122, 255));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, buttonFontSize));
            UIManager.put("Button.margin", new Insets(buttonPadding, horizontalPadding, buttonPadding, horizontalPadding));
            UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, labelFontSize));
            UIManager.put("TabbedPane.font", new Font("SansSerif", Font.BOLD, tabFontSize));
            UIManager.put("TabbedPane.selected", new Color(0, 122, 255));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets((int)(8*dpiScale), (int)(8*dpiScale), (int)(8*dpiScale), (int)(8*dpiScale)));
            UIManager.put("TabbedPane.tabInsets", new Insets(buttonPadding, horizontalPadding, buttonPadding, horizontalPadding));
            UIManager.put("Spinner.font", new Font("SansSerif", Font.PLAIN, spinnerFontSize));
            UIManager.put("CheckBox.font", new Font("SansSerif", Font.PLAIN, labelFontSize));
            UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, spinnerFontSize));
            UIManager.put("TextArea.font", new Font("SansSerif", Font.PLAIN, labelFontSize));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() {
        teachModePanel = new TeachModePanel(configManager);
        inferencePanel = new InferencePanel(configManager);
        configurationPanel = new ConfigurationPanel(configManager);
        recipePanel = new RecipePanel(configManager);
        
        tabbedPane = new JTabbedPane();
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Touch-friendly tab styling (scaled by DPI)
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, (int)(18 * dpiScale)));
        tabbedPane.setBackground(new Color(245, 245, 247));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        // Add tabs with icons and better labels (bigger spacing for touch)
        tabbedPane.addTab("  📚  Teach  ", teachModePanel);
        tabbedPane.addTab("  🔍  Inference  ", inferencePanel);
        tabbedPane.addTab("  ⚙️  Config  ", configurationPanel);
        tabbedPane.addTab("  📋  Recipes  ", recipePanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem loadConfigItem = new JMenuItem("Load Configuration...");
        loadConfigItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadConfigItem.addActionListener(e -> loadConfiguration());
        
        JMenuItem saveConfigItem = new JMenuItem("Save Configuration...");
        saveConfigItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveConfigItem.addActionListener(e -> saveConfiguration());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(loadConfigItem);
        fileMenu.add(saveConfigItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        JMenuItem fullscreenItem = new JMenuItem("Toggle Fullscreen");
        fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        fullscreenItem.addActionListener(e -> toggleFullscreen());
        
        viewMenu.add(fullscreenItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void loadConfiguration() {
        JFileChooser fileChooser = new JFileChooser("../config");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (configManager.loadConfiguration(path)) {
                JOptionPane.showMessageDialog(this, "Configuration loaded successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load configuration", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveConfiguration() {
        JFileChooser fileChooser = new JFileChooser("../config");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".json")) {
                path += ".json";
            }
            if (configManager.saveConfiguration(path)) {
                JOptionPane.showMessageDialog(this, "Configuration saved successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save configuration", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void toggleFullscreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.getFullScreenWindow() == this) {
            device.setFullScreenWindow(null);
        } else {
            device.setFullScreenWindow(this);
        }
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "Dough Vision Detector v1.0\n\n" +
            "Rule-based machine vision system for dough detection\n" +
            "on production lines using OpenCV.\n\n" +
            "© 2024",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
