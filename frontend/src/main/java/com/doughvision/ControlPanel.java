package com.doughvision;

import javax.swing.*;
import java.awt.*;

/**
 * Control panel for adjusting detection parameters in real-time
 */
public class ControlPanel extends JPanel {
    
    private VisionPanel visionPanel;
    private ConfigurationManager configManager;
    
    private JButton startButton;
    private JButton stopButton;
    private JButton snapshotButton;
    private JToggleButton overlayToggle;
    
    private JSlider minAreaSlider;
    private JSlider maxAreaSlider;
    private JLabel minAreaLabel;
    private JLabel maxAreaLabel;
    
    private boolean isRunning = false;
    
    public ControlPanel(VisionPanel visionPanel, ConfigurationManager configManager) {
        this.visionPanel = visionPanel;
        this.configManager = configManager;
        
        initializeComponents();
        layoutComponents();
    }
    
    private void initializeComponents() {
        startButton = new JButton("Start Detection");
        startButton.setIcon(createColorIcon(Color.GREEN));
        startButton.addActionListener(e -> startDetection());
        
        stopButton = new JButton("Stop Detection");
        stopButton.setIcon(createColorIcon(Color.RED));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopDetection());
        
        snapshotButton = new JButton("Snapshot");
        snapshotButton.setIcon(createColorIcon(Color.BLUE));
        snapshotButton.addActionListener(e -> takeSnapshot());
        
        overlayToggle = new JToggleButton("Show Overlay", true);
        overlayToggle.addActionListener(e -> visionPanel.setShowOverlay(overlayToggle.isSelected()));
        
        // Area sliders
        minAreaSlider = new JSlider(100, 10000, 500);
        minAreaSlider.setMajorTickSpacing(2000);
        minAreaSlider.setMinorTickSpacing(500);
        minAreaSlider.setPaintTicks(true);
        minAreaSlider.addChangeListener(e -> updateMinArea());
        
        maxAreaSlider = new JSlider(1000, 100000, 50000);
        maxAreaSlider.setMajorTickSpacing(20000);
        maxAreaSlider.setMinorTickSpacing(5000);
        maxAreaSlider.setPaintTicks(true);
        maxAreaSlider.addChangeListener(e -> updateMaxArea());
        
        minAreaLabel = new JLabel("Min Area: 500");
        maxAreaLabel = new JLabel("Max Area: 50000");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(snapshotButton);
        buttonPanel.add(overlayToggle);
        
        // Slider panel
        JPanel sliderPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        sliderPanel.add(minAreaLabel);
        sliderPanel.add(minAreaSlider);
        sliderPanel.add(maxAreaLabel);
        sliderPanel.add(maxAreaSlider);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(sliderPanel, BorderLayout.CENTER);
    }
    
    private Icon createColorIcon(Color color) {
        return new Icon() {
            public int getIconWidth() { return 12; }
            public int getIconHeight() { return 12; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillOval(x, y, 12, 12);
            }
        };
    }
    
    private void startDetection() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        visionPanel.setStatusText("Detection running...");
        
        // TODO: Start backend processing
        System.out.println("Starting detection...");
    }
    
    private void stopDetection() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        visionPanel.setStatusText("Detection stopped");
        
        // TODO: Stop backend processing
        System.out.println("Stopping detection...");
    }
    
    private void takeSnapshot() {
        visionPanel.setStatusText("Snapshot saved");
        // TODO: Implement snapshot functionality
        System.out.println("Taking snapshot...");
    }
    
    private void updateMinArea() {
        int value = minAreaSlider.getValue();
        minAreaLabel.setText("Min Area: " + value);
        configManager.setMinArea(value);
    }
    
    private void updateMaxArea() {
        int value = maxAreaSlider.getValue();
        maxAreaLabel.setText("Max Area: " + value);
        configManager.setMaxArea(value);
    }
}
