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
    private ConfigurationManager configManager;
    
    private JTabbedPane tabbedPane;
    
    public MainFrame() {
        super("Dough Vision Detector");
        
        configManager = new ConfigurationManager();
        
        // Set modern look and feel
        setModernLookAndFeel();
        
        initializeComponents();
        layoutComponents();
        setupMenuBar();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Load default configuration
        configManager.loadDefaultConfig();
    }
    
    private void setModernLookAndFeel() {
        try {
            // Use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Modern color scheme
            UIManager.put("Panel.background", new Color(245, 245, 247));
            UIManager.put("Button.background", new Color(0, 122, 255));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 13));
            UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 13));
            UIManager.put("TabbedPane.font", new Font("SansSerif", Font.BOLD, 14));
            UIManager.put("TabbedPane.selected", new Color(0, 122, 255));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() {
        teachModePanel = new TeachModePanel(configManager);
        inferencePanel = new InferencePanel(configManager);
        configurationPanel = new ConfigurationPanel(configManager);
        
        tabbedPane = new JTabbedPane();
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Modern tab styling
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(245, 245, 247));
        
        // Add tabs with icons and better labels
        tabbedPane.addTab("  📚 Teach  ", teachModePanel);
        tabbedPane.addTab("  🔍 Inference  ", inferencePanel);
        tabbedPane.addTab("  ⚙️ Config  ", configurationPanel);
        
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
