package com.doughvision;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.imageio.ImageIO;

/**
 * Panel for running inference on images
 */
public class InferencePanel extends JPanel {
    
    private ConfigurationManager configManager;
    private BufferedImage originalImage;
    private BufferedImage resultImage;
    private JPanel imagePanel;
    private JButton loadButton;
    private JButton runButton;
    private JCheckBox fastModeCheckbox;
    private JCheckBox useROICheckbox;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomResetButton;
    private JLabel zoomLabel;
    
    // ROI settings
    private JSpinner roiXSpinner, roiYSpinner;
    private JSpinner roiWidthSpinner, roiHeightSpinner;
    private JButton setROIButton;
    
    // ROI drawing
    private boolean isDrawingROI = false;
    private Point roiStartPoint = null;
    private Rectangle currentROI = null;
    private Rectangle savedROI = null;
    
    private double zoomLevel = 1.0;
    private Point viewOffset = new Point(0, 0);
    private Point lastDragPoint = null;
    
    // Background task management
    private SwingWorker<BufferedImage, String> currentTask = null;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public InferencePanel(ConfigurationManager configManager) {
        this.configManager = configManager;
        
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 247));
        
        // Control panel at top
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(new Color(245, 245, 247));
        
        loadButton = createStyledButton("📁 Load Image", new Color(52, 152, 219));
        loadButton.addActionListener(e -> loadImage());
        
        runButton = createStyledButton("▶️ Run Inference", new Color(46, 204, 113));
        runButton.setEnabled(false);
        runButton.addActionListener(e -> runInference());
        
        fastModeCheckbox = new JCheckBox("⚡ Fast Mode (constrained hardware)");
        fastModeCheckbox.setFont(new Font("SansSerif", Font.BOLD, 12));
        fastModeCheckbox.setBackground(new Color(245, 245, 247));
        fastModeCheckbox.setToolTipText("Downsample large images and skip preprocessing for faster inference");
        fastModeCheckbox.addActionListener(e -> configManager.setFastMode(fastModeCheckbox.isSelected()));
        
        useROICheckbox = new JCheckBox("🎯 Use ROI (Region of Interest)");
        useROICheckbox.setFont(new Font("SansSerif", Font.BOLD, 12));
        useROICheckbox.setBackground(new Color(245, 245, 247));
        useROICheckbox.setToolTipText("Restrict detection to a specific region of the image");
        
        // ROI spinners
        roiXSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 2000, 10));
        roiYSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 2000, 10));
        roiWidthSpinner = new JSpinner(new SpinnerNumberModel(640, 50, 2000, 10));
        roiHeightSpinner = new JSpinner(new SpinnerNumberModel(480, 50, 2000, 10));
        
        setROIButton = createStyledButton("🎯 Draw ROI", new Color(155, 89, 182));
        setROIButton.setPreferredSize(new Dimension(120, 35));
        setROIButton.addActionListener(e -> toggleROIDrawing());
        
        zoomInButton = createStyledButton("🔍 Zoom In", new Color(76, 175, 80));
        zoomInButton.setPreferredSize(new Dimension(120, 35));
        zoomInButton.addActionListener(e -> zoomIn());
        
        zoomOutButton = createStyledButton("🔎 Zoom Out", new Color(76, 175, 80));
        zoomOutButton.setPreferredSize(new Dimension(120, 35));
        zoomOutButton.addActionListener(e -> zoomOut());
        
        zoomResetButton = createStyledButton("↺ Reset", new Color(158, 158, 158));
        zoomResetButton.setPreferredSize(new Dimension(100, 35));
        zoomResetButton.addActionListener(e -> resetZoom());
        
        zoomLabel = new JLabel("Zoom: 100%");
        zoomLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel infoLabel = new JLabel("💡 Train model in Teach tab first");
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        
        // Progress bar and status label
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        controlPanel.add(loadButton);
        controlPanel.add(runButton);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(zoomResetButton);
        controlPanel.add(zoomLabel);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(setROIButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(fastModeCheckbox);
        controlPanel.add(useROICheckbox);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(infoLabel);
        
        // Add progress indicators
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(progressBar);
        controlPanel.add(statusLabel);
        
        // Add ROI settings panel
        JPanel roiPanel = createROISettingsPanel();
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(roiPanel);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Create custom image panel with zoom/pan support
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (resultImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    int w = getWidth();
                    int h = getHeight();
                    double baseScale = Math.min((double)w / resultImage.getWidth(), (double)h / resultImage.getHeight());
                    double displayScale = baseScale * zoomLevel;
                    
                    int scaledW = (int)(resultImage.getWidth() * displayScale);
                    int scaledH = (int)(resultImage.getHeight() * displayScale);
                    int x = (w - scaledW) / 2 + viewOffset.x;
                    int y = (h - scaledH) / 2 + viewOffset.y;
                    
                    g2d.drawImage(resultImage, x, y, scaledW, scaledH, this);
                    
                    // Draw saved ROI if it exists
                    if (savedROI != null) {
                        drawROI(g2d, savedROI, x, y, displayScale, new Color(0, 255, 0, 100));
                    }
                } else {
                    g.setColor(new Color(149, 165, 166));
                    g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    String msg = "📷 Load an image to run inference";
                    FontMetrics fm = g.getFontMetrics();
                    int msgWidth = fm.stringWidth(msg);
                    g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
                }
                
                // Draw current ROI being drawn
                if (currentROI != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(255, 255, 0, 150));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(currentROI);
                    g2d.setColor(new Color(255, 255, 0, 50));
                    g2d.fill(currentROI);
                }
            }
        };
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        imagePanel.setPreferredSize(new Dimension(1000, 700));
        
        // Add mouse listeners for pan and ROI drawing
        imagePanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (isDrawingROI && SwingUtilities.isLeftMouseButton(e)) {
                    // Start drawing ROI
                    roiStartPoint = e.getPoint();
                    currentROI = null;
                } else if (SwingUtilities.isMiddleMouseButton(e) || 
                    (SwingUtilities.isLeftMouseButton(e) && e.isControlDown())) {
                    // Pan mode
                    lastDragPoint = e.getPoint();
                    imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                if (isDrawingROI && SwingUtilities.isLeftMouseButton(e) && roiStartPoint != null) {
                    // Finish drawing ROI
                    Point endPoint = e.getPoint();
                    if (Math.abs(endPoint.x - roiStartPoint.x) > 10 && 
                        Math.abs(endPoint.y - roiStartPoint.y) > 10) {
                        
                        // Convert screen coordinates to image coordinates
                        Point imageStart = screenToImageCoords(roiStartPoint);
                        Point imageEnd = screenToImageCoords(endPoint);
                        
                        int x = Math.min(imageStart.x, imageEnd.x);
                        int y = Math.min(imageStart.y, imageEnd.y);
                        int width = Math.abs(imageEnd.x - imageStart.x);
                        int height = Math.abs(imageEnd.y - imageStart.y);
                        
                        savedROI = new Rectangle(x, y, width, height);
                        
                        // Update spinners
                        roiXSpinner.setValue(x);
                        roiYSpinner.setValue(y);
                        roiWidthSpinner.setValue(width);
                        roiHeightSpinner.setValue(height);
                        
                        // Enable ROI checkbox
                        useROICheckbox.setSelected(true);
                        
                        // Exit ROI drawing mode
                        isDrawingROI = false;
                        setROIButton.setText("🎯 Draw ROI");
                        setROIButton.setBackground(new Color(155, 89, 182));
                        
                        System.out.println("ROI set: " + x + "," + y + " " + width + "x" + height);
                    }
                    
                    roiStartPoint = null;
                    currentROI = null;
                    imagePanel.repaint();
                } else {
                    // Pan mode
                    lastDragPoint = null;
                    imagePanel.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        
        imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDrawingROI && roiStartPoint != null) {
                    // Update current ROI while dragging
                    Point endPoint = e.getPoint();
                    int x = Math.min(roiStartPoint.x, endPoint.x);
                    int y = Math.min(roiStartPoint.y, endPoint.y);
                    int width = Math.abs(endPoint.x - roiStartPoint.x);
                    int height = Math.abs(endPoint.y - roiStartPoint.y);
                    currentROI = new Rectangle(x, y, width, height);
                    imagePanel.repaint();
                } else if (lastDragPoint != null) {
                    // Pan mode
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;
                    viewOffset.x += dx;
                    viewOffset.y += dy;
                    lastDragPoint = e.getPoint();
                    imagePanel.repaint();
                }
            }
        });
        
        // Mouse wheel zoom
        imagePanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoomIn();
            } else {
                zoomOut();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        
        // Get DPI scale for this display
        double dpiScale = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
        if (dpiScale < 1.0) dpiScale = 1.0;
        if (dpiScale > 3.0) dpiScale = 3.0;
        
        int fontSize = (int)(16 * dpiScale);
        int width = (int)(180 * dpiScale);
        int height = (int)(50 * dpiScale);
        
        btn.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg.brighter());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        
        return btn;
    }
    
    private JPanel createROISettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(new Color(245, 245, 247));
        panel.setBorder(BorderFactory.createTitledBorder("ROI Settings"));
        
        // Add ROI controls
        panel.add(new JLabel("X:"));
        panel.add(roiXSpinner);
        panel.add(new JLabel("Y:"));
        panel.add(roiYSpinner);
        panel.add(new JLabel("W:"));
        panel.add(roiWidthSpinner);
        panel.add(new JLabel("H:"));
        panel.add(roiHeightSpinner);
        
        return panel;
    }
    
    private void toggleROIDrawing() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first", 
                "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (isDrawingROI) {
            // Exit ROI drawing mode
            isDrawingROI = false;
            setROIButton.setText("🎯 Draw ROI");
            setROIButton.setBackground(new Color(155, 89, 182));
            roiStartPoint = null;
            currentROI = null;
            imagePanel.repaint();
        } else {
            // Enter ROI drawing mode
            isDrawingROI = true;
            setROIButton.setText("❌ Cancel");
            setROIButton.setBackground(new Color(231, 76, 60));
            imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }
    
    private void drawROI(Graphics2D g2d, Rectangle roi, int imageX, int imageY, double scale, Color color) {
        // Convert image coordinates to screen coordinates
        int screenX = (int)(roi.x * scale) + imageX;
        int screenY = (int)(roi.y * scale) + imageY;
        int screenW = (int)(roi.width * scale);
        int screenH = (int)(roi.height * scale);
        
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(screenX, screenY, screenW, screenH);
        
        // Draw label
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString("ROI", screenX + 5, screenY + 20);
    }
    
    private Point screenToImageCoords(Point screen) {
        if (resultImage == null) return screen;
        
        int w = imagePanel.getWidth();
        int h = imagePanel.getHeight();
        double baseScale = Math.min((double)w / resultImage.getWidth(), (double)h / resultImage.getHeight());
        double displayScale = baseScale * zoomLevel;
        
        int scaledW = (int)(resultImage.getWidth() * displayScale);
        int scaledH = (int)(resultImage.getHeight() * displayScale);
        int offsetX = (w - scaledW) / 2 + viewOffset.x;
        int offsetY = (h - scaledH) / 2 + viewOffset.y;
        
        int imageX = (int)((screen.x - offsetX) / displayScale);
        int imageY = (int)((screen.y - offsetY) / displayScale);
        
        return new Point(imageX, imageY);
    }
    
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "bmp"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                originalImage = ImageIO.read(file);
                
                if (originalImage != null) {
                    displayImage(originalImage);
                    runButton.setEnabled(true);
                    
                    // Set ROI spinners based on image dimensions
                    roiWidthSpinner.setValue(originalImage.getWidth());
                    roiHeightSpinner.setValue(originalImage.getHeight());
                    roiXSpinner.setValue(0);
                    roiYSpinner.setValue(0);
                    
                    System.out.println("Loaded image: " + file.getName() + 
                        " (" + originalImage.getWidth() + "x" + originalImage.getHeight() + ")");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to load image", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void runInference() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first",
                "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Cancel any existing task
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        
        // Validate ROI settings if enabled
        if (useROICheckbox.isSelected()) {
            int x = (int)roiXSpinner.getValue();
            int y = (int)roiYSpinner.getValue();
            int width = (int)roiWidthSpinner.getValue();
            int height = (int)roiHeightSpinner.getValue();
            
            // Validate ROI bounds
            if (x < 0 || y < 0 || x + width > originalImage.getWidth() || y + height > originalImage.getHeight()) {
                JOptionPane.showMessageDialog(this, 
                    "ROI bounds exceed image dimensions. Please adjust ROI settings.",
                    "Invalid ROI", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Create background task
        currentTask = new SwingWorker<BufferedImage, String>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                // Show progress
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                progressBar.setString("Processing...");
                statusLabel.setText("Running inference...");
                
                // Apply ROI settings if enabled
                BufferedImage processImage = originalImage;
                final boolean useROI = useROICheckbox.isSelected();
                
                if (useROI) {
                    publish("Applying ROI...");
                    int x = (int)roiXSpinner.getValue();
                    int y = (int)roiYSpinner.getValue();
                    int width = (int)roiWidthSpinner.getValue();
                    int height = (int)roiHeightSpinner.getValue();
                    processImage = originalImage.getSubimage(x, y, width, height);
                }
                
                // Run segmentation in background thread
                publish("Segmenting image (this may take a moment)...");
                BufferedImage result = configManager.runSegmentation(processImage);
                
                if (result == null) {
                    throw new Exception("No learned model. Please teach the model first.");
                }
                
                // If ROI was used, composite the result back onto the original image
                if (useROI) {
                    publish("Compositing results...");
                    BufferedImage compositeResult = new BufferedImage(
                        originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = compositeResult.createGraphics();
                    g2d.drawImage(originalImage, 0, 0, null);
                    int x = (int)roiXSpinner.getValue();
                    int y = (int)roiYSpinner.getValue();
                    g2d.drawImage(result, x, y, null);
                    g2d.dispose();
                    result = compositeResult;
                }
                
                return result;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                // Update status text
                if (!chunks.isEmpty()) {
                    statusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                // Hide progress bar
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                
                try {
                    if (isCancelled()) {
                        statusLabel.setText("Cancelled");
                        return;
                    }
                    
                    BufferedImage result = get();
                    if (result != null) {
                        displayImage(result);
                        statusLabel.setText("Inference complete");
                        System.out.println("Inference complete");
                    }
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    statusLabel.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(InferencePanel.this, 
                        cause.getMessage(),
                        "Inference Error", JOptionPane.ERROR_MESSAGE);
                } catch (java.lang.InterruptedException e) {
                    statusLabel.setText("Interrupted");
                } finally {
                    runButton.setEnabled(true);
                    currentTask = null;
                }
            }
        };
        
        // Disable button and start task
        runButton.setEnabled(false);
        currentTask.execute();
    }
    
    private void zoomIn() {
        zoomLevel = Math.min(zoomLevel * 1.2, 5.0);
        updateZoomLabel();
        imagePanel.repaint();
    }
    
    private void zoomOut() {
        zoomLevel = Math.max(zoomLevel / 1.2, 0.25);
        updateZoomLabel();
        imagePanel.repaint();
    }
    
    private void resetZoom() {
        zoomLevel = 1.0;
        viewOffset = new Point(0, 0);
        updateZoomLabel();
        imagePanel.repaint();
    }
    
    private void updateZoomLabel() {
        zoomLabel.setText(String.format("Zoom: %d%%", (int)(zoomLevel * 100)));
    }
    
    private void displayImage(BufferedImage image) {
        resultImage = image;
        zoomLevel = 1.0;
        viewOffset = new Point(0, 0);
        updateZoomLabel();
        imagePanel.repaint();
    }
}
