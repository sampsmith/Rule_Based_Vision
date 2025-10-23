package com.doughvision;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Teach mode panel for defining ROIs and detection regions
 */
public class TeachModePanel extends JPanel {
    
    enum DrawMode { NONE, RECTANGLE, POLYGON }
    
    private ConfigurationManager configManager;
    private DrawingCanvas canvas;
    private JPanel toolPanel;
    
    private JButton loadImageButton;
    private JButton clearButton;
    private JButton saveRegionsButton;
    private JButton teachModelButton;
    private JButton runSegmentationButton;
    private JToggleButton drawRectButton;
    private JToggleButton drawPolyButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomResetButton;
    private JLabel zoomLabel;
    
    private JList<String> regionList;
    private DefaultListModel<String> regionListModel;
    private JTextField labelField;
    private String currentLabel = "dough";
    
    public TeachModePanel(ConfigurationManager configManager) {
        this.configManager = configManager;
        
        initializeComponents();
        layoutComponents();
    }
    
    private void initializeComponents() {
        canvas = new DrawingCanvas();
        
        loadImageButton = new JButton("Load Image");
        loadImageButton.addActionListener(e -> loadImage());
        
        clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> canvas.clearAll());
        
        saveRegionsButton = new JButton("Save Regions");
        saveRegionsButton.addActionListener(e -> saveRegions());
        
        teachModelButton = new JButton("Teach Model");
        teachModelButton.addActionListener(e -> teachModel());
        
        runSegmentationButton = new JButton("Run Segmentation");
        runSegmentationButton.addActionListener(e -> runSegmentation());
        
        drawRectButton = new JToggleButton("Rectangle Tool");
        drawRectButton.addActionListener(e -> {
            canvas.setDrawingMode(DrawMode.RECTANGLE);
            drawPolyButton.setSelected(false);
        });
        
        drawPolyButton = new JToggleButton("Polygon Tool");
        drawPolyButton.addActionListener(e -> {
            canvas.setDrawingMode(DrawMode.POLYGON);
            drawRectButton.setSelected(false);
        });
        
        zoomInButton = new JButton("🔍 Zoom In");
        zoomInButton.addActionListener(e -> canvas.zoomIn());
        
        zoomOutButton = new JButton("🔎 Zoom Out");
        zoomOutButton.addActionListener(e -> canvas.zoomOut());
        
        zoomResetButton = new JButton("↺ Reset Zoom");
        zoomResetButton.addActionListener(e -> canvas.resetZoom());
        
        zoomLabel = new JLabel("Zoom: 100%");
        zoomLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        
        labelField = new JTextField(currentLabel, 15);
        labelField.addActionListener(e -> currentLabel = labelField.getText());
        
        regionListModel = new DefaultListModel<>();
        regionList = new JList<>(regionListModel);
        regionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tool panel
        toolPanel = new JPanel();
        toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
        toolPanel.setBorder(BorderFactory.createTitledBorder("Tools"));
        
        toolPanel.add(loadImageButton);
        toolPanel.add(Box.createVerticalStrut(10));
        
        JLabel labelLabel = new JLabel("Label:");
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolPanel.add(labelLabel);
        toolPanel.add(labelField);
        toolPanel.add(Box.createVerticalStrut(10));
        
        toolPanel.add(drawRectButton);
        toolPanel.add(drawPolyButton);
        toolPanel.add(Box.createVerticalStrut(10));
        
        JLabel zoomTitle = new JLabel("Zoom:");
        zoomTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolPanel.add(zoomTitle);
        toolPanel.add(zoomInButton);
        toolPanel.add(zoomOutButton);
        toolPanel.add(zoomResetButton);
        toolPanel.add(zoomLabel);
        toolPanel.add(Box.createVerticalStrut(10));
        
        toolPanel.add(saveRegionsButton);
        toolPanel.add(teachModelButton);
        toolPanel.add(runSegmentationButton);
        toolPanel.add(clearButton);
        toolPanel.add(Box.createVerticalStrut(20));
        
        JLabel regionsLabel = new JLabel("Defined Regions:");
        regionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolPanel.add(regionsLabel);
        
        JScrollPane listScroll = new JScrollPane(regionList);
        listScroll.setPreferredSize(new Dimension(200, 300));
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolPanel.add(listScroll);
        
        // Layout
        add(canvas, BorderLayout.CENTER);
        add(toolPanel, BorderLayout.EAST);
        
        // Instructions at bottom
        JLabel instructions = new JLabel(
            "<html><b>Instructions:</b> Load an image, set a label, select a tool, and draw regions. " +
            "<b>Rectangle:</b> Click-drag-release. <b>Polygon:</b> Click points, right-click to finish. " +
            "<b>Zoom:</b> Use buttons or mouse wheel. " +
            "Then click 'Teach Model' to train the rule-based CV system.</html>"
        );
        instructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(instructions, BorderLayout.SOUTH);
    }
    
    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "bmp"
        ));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = javax.imageio.ImageIO.read(chooser.getSelectedFile());
                canvas.setImage(img);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveRegions() {
        List<AnnotatedRegion> regions = canvas.getAnnotatedRegions();
        configManager.setAnnotatedRegions(regions);
        
        // Update list
        regionListModel.clear();
        for (int i = 0; i < regions.size(); i++) {
            AnnotatedRegion ar = regions.get(i);
            String shapeType = ar.isPolygon ? "Polygon" : "Rectangle";
            regionListModel.addElement(String.format("%s %d: '%s' [%d points]", 
                shapeType, i+1, ar.label, ar.isPolygon ? ar.polygonPoints.size() : 4));
        }
        
        JOptionPane.showMessageDialog(this, 
            "Saved " + regions.size() + " region(s)", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void teachModel() {
        List<AnnotatedRegion> regions = canvas.getAnnotatedRegions();
        if (regions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please draw and save regions first!", 
                "No Regions", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BufferedImage image = canvas.getImage();
        if (image == null) {
            JOptionPane.showMessageDialog(this, 
                "No image loaded!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Teach the model using annotated regions
        boolean success = configManager.teachModel(image, regions);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Model trained successfully with " + regions.size() + " example(s)!\n" +
                "Rule-based segmentation model updated.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear annotations after successful teaching
            canvas.clearAnnotations();
            regionListModel.clear();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to teach model. Check console for errors.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void runSegmentation() {
        BufferedImage image = canvas.getImage();
        if (image == null) {
            JOptionPane.showMessageDialog(this, 
                "Please load an image first!", 
                "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Run segmentation and display results
        BufferedImage result = configManager.runSegmentation(image);
        if (result != null) {
            canvas.setSegmentationResult(result);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Segmentation failed. Make sure the model is trained.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Annotated region with label
     */
    public static class AnnotatedRegion {
        public String label;
        public Rectangle boundingBox;
        public List<Point> polygonPoints;
        public boolean isPolygon;
        
        public AnnotatedRegion(String label, Rectangle rect) {
            this.label = label;
            this.boundingBox = rect;
            this.isPolygon = false;
        }
        
        public AnnotatedRegion(String label, List<Point> points) {
            this.label = label;
            this.polygonPoints = new ArrayList<>(points);
            this.isPolygon = true;
            
            // Calculate bounding box
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Point p : points) {
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }
            this.boundingBox = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
    }
    
    /**
     * Canvas for drawing ROIs
     */
    private class DrawingCanvas extends JPanel {
        
        private BufferedImage image;
        private BufferedImage segmentationResult;
        private List<AnnotatedRegion> annotatedRegions = new ArrayList<>();
        private Rectangle currentRect;
        private List<Point> currentPolygon = new ArrayList<>();
        private Point startPoint;
        private DrawMode mode = DrawMode.RECTANGLE;
        private boolean showSegmentation = false;
        
        private double zoomLevel = 1.0;
        private Point viewOffset = new Point(0, 0);
        private Point lastDragPoint = null;
        
        public DrawingCanvas() {
            setBackground(Color.LIGHT_GRAY);
            setPreferredSize(new Dimension(800, 600));
            
            // Mouse wheel zoom
            addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.getWheelRotation() < 0) {
                        zoomIn();
                    } else {
                        zoomOut();
                    }
                }
            });
            
            MouseAdapter mouseHandler = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    // Middle mouse button for panning
                    if (SwingUtilities.isMiddleMouseButton(e) || 
                        (SwingUtilities.isLeftMouseButton(e) && e.isControlDown())) {
                        lastDragPoint = e.getPoint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        return;
                    }
                    
                    if (SwingUtilities.isRightMouseButton(e)) {
                        // Right-click: finish polygon
                        if (mode == DrawMode.POLYGON && currentPolygon.size() >= 3) {
                            // Convert to image coordinates before storing
                            List<Point> imagePoints = new ArrayList<>();
                            for (Point p : currentPolygon) {
                                Point imageP = screenToImageCoords(p);
                                imagePoints.add(imageP);
                            }
                            annotatedRegions.add(new AnnotatedRegion(currentLabel, imagePoints));
                            currentPolygon = new ArrayList<>();
                            repaint();
                        }
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (mode == DrawMode.RECTANGLE) {
                            startPoint = e.getPoint();
                            currentRect = new Rectangle(startPoint);
                        } else if (mode == DrawMode.POLYGON) {
                            currentPolygon.add(e.getPoint());
                            repaint();
                        }
                    }
                }
                
                public void mouseDragged(MouseEvent e) {
                    // Handle panning
                    if (lastDragPoint != null) {
                        int dx = e.getX() - lastDragPoint.x;
                        int dy = e.getY() - lastDragPoint.y;
                        viewOffset.x += dx;
                        viewOffset.y += dy;
                        lastDragPoint = e.getPoint();
                        repaint();
                        return;
                    }
                    
                    if (mode == DrawMode.RECTANGLE && startPoint != null) {
                        int x = Math.min(startPoint.x, e.getX());
                        int y = Math.min(startPoint.y, e.getY());
                        int w = Math.abs(e.getX() - startPoint.x);
                        int h = Math.abs(e.getY() - startPoint.y);
                        currentRect = new Rectangle(x, y, w, h);
                        repaint();
                    }
                }
                
                public void mouseReleased(MouseEvent e) {
                    if (lastDragPoint != null) {
                        lastDragPoint = null;
                        setCursor(Cursor.getDefaultCursor());
                        return;
                    }
                    
                    if (mode == DrawMode.RECTANGLE && currentRect != null && 
                        currentRect.width > 5 && currentRect.height > 5) {
                        // Convert to image coordinates before storing
                        Point topLeft = screenToImageCoords(new Point(currentRect.x, currentRect.y));
                        Point bottomRight = screenToImageCoords(new Point(
                            currentRect.x + currentRect.width, 
                            currentRect.y + currentRect.height));
                        Rectangle imageRect = new Rectangle(
                            topLeft.x, topLeft.y, 
                            bottomRight.x - topLeft.x, 
                            bottomRight.y - topLeft.y);
                        annotatedRegions.add(new AnnotatedRegion(currentLabel, imageRect));
                        currentRect = null;
                        startPoint = null;
                        repaint();
                    }
                }
                
                public void mouseMoved(MouseEvent e) {
                    // Show preview line for polygon mode
                    if (mode == DrawMode.POLYGON && !currentPolygon.isEmpty()) {
                        repaint();
                    }
                }
            };
            
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        public void zoomIn() {
            zoomLevel = Math.min(zoomLevel * 1.2, 5.0);
            updateZoomLabel();
            repaint();
        }
        
        public void zoomOut() {
            zoomLevel = Math.max(zoomLevel / 1.2, 0.25);
            updateZoomLabel();
            repaint();
        }
        
        public void resetZoom() {
            zoomLevel = 1.0;
            viewOffset = new Point(0, 0);
            updateZoomLabel();
            repaint();
        }
        
        private void updateZoomLabel() {
            zoomLabel.setText(String.format("Zoom: %d%%", (int)(zoomLevel * 100)));
        }
        
        /**
         * Convert screen coordinates to image coordinates
         */
        private Point screenToImageCoords(Point screen) {
            if (image == null) return screen;
            
            int w = getWidth();
            int h = getHeight();
            double baseScale = Math.min((double)w / image.getWidth(), (double)h / image.getHeight());
            double displayScale = baseScale * zoomLevel;
            
            int scaledW = (int)(image.getWidth() * displayScale);
            int scaledH = (int)(image.getHeight() * displayScale);
            int offsetX = (w - scaledW) / 2 + viewOffset.x;
            int offsetY = (h - scaledH) / 2 + viewOffset.y;
            
            int imageX = (int)((screen.x - offsetX) / displayScale);
            int imageY = (int)((screen.y - offsetY) / displayScale);
            
            return new Point(imageX, imageY);
        }
        
        /**
         * Convert image coordinates to screen coordinates
         */
        private Point imageToScreenCoords(Point image) {
            if (this.image == null) return image;
            
            int w = getWidth();
            int h = getHeight();
            double baseScale = Math.min((double)w / this.image.getWidth(), (double)h / this.image.getHeight());
            double displayScale = baseScale * zoomLevel;
            
            int scaledW = (int)(this.image.getWidth() * displayScale);
            int scaledH = (int)(this.image.getHeight() * displayScale);
            int offsetX = (w - scaledW) / 2 + viewOffset.x;
            int offsetY = (h - scaledH) / 2 + viewOffset.y;
            
            int screenX = (int)(image.x * displayScale + offsetX);
            int screenY = (int)(image.y * displayScale + offsetY);
            
            return new Point(screenX, screenY);
        }
        
        public void setImage(BufferedImage img) {
            this.image = img;
            this.segmentationResult = null;
            this.showSegmentation = false;
            zoomLevel = 1.0;
            viewOffset = new Point(0, 0);
            updateZoomLabel();
            repaint();
        }
        
        public BufferedImage getImage() {
            return image;
        }
        
        public void setSegmentationResult(BufferedImage result) {
            this.segmentationResult = result;
            this.showSegmentation = true;
            repaint();
        }
        
        public void clearAnnotations() {
            this.annotatedRegions.clear();
            this.currentRect = null;
            this.currentPolygon.clear();
            repaint();
        }
        
        public void setDrawingMode(DrawMode mode) {
            this.mode = mode;
            // Clear current drawing
            currentPolygon.clear();
            currentRect = null;
            repaint();
        }
        
        public List<AnnotatedRegion> getAnnotatedRegions() {
            // Annotations are already stored in image coordinates, just return them
            return new ArrayList<>(annotatedRegions);
        }
        
        public void clearAll() {
            annotatedRegions.clear();
            currentRect = null;
            currentPolygon.clear();
            segmentationResult = null;
            showSegmentation = false;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw image if loaded
            BufferedImage displayImage = showSegmentation && segmentationResult != null ? segmentationResult : image;
            
            if (displayImage != null) {
                int w = getWidth();
                int h = getHeight();
                double baseScale = Math.min((double)w / displayImage.getWidth(), (double)h / displayImage.getHeight());
                double scale = baseScale * zoomLevel;
                
                int scaledW = (int)(displayImage.getWidth() * scale);
                int scaledH = (int)(displayImage.getHeight() * scale);
                int x = (w - scaledW) / 2 + viewOffset.x;
                int y = (h - scaledH) / 2 + viewOffset.y;
                
                g2d.drawImage(displayImage, x, y, scaledW, scaledH, this);
            } else {
                g2d.setColor(Color.DARK_GRAY);
                String msg = "Load an image to start";
                FontMetrics fm = g2d.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            }
            
            // Don't draw annotations if showing segmentation result
            if (!showSegmentation) {
                // Draw saved annotated regions (transform from image to screen coords)
                g2d.setStroke(new BasicStroke(2));
                for (int i = 0; i < annotatedRegions.size(); i++) {
                    AnnotatedRegion ar = annotatedRegions.get(i);
                    
                    g2d.setColor(new Color(0, 255, 0, 100));
                    if (ar.isPolygon) {
                        // Draw polygon (transform to screen coords)
                        Path2D path = new Path2D.Double();
                        Point firstScreen = imageToScreenCoords(ar.polygonPoints.get(0));
                        path.moveTo(firstScreen.x, firstScreen.y);
                        for (int j = 1; j < ar.polygonPoints.size(); j++) {
                            Point pScreen = imageToScreenCoords(ar.polygonPoints.get(j));
                            path.lineTo(pScreen.x, pScreen.y);
                        }
                        path.closePath();
                        g2d.draw(path);
                        g2d.fill(path);
                        
                        // Draw vertices
                        g2d.setColor(Color.GREEN);
                        for (Point p : ar.polygonPoints) {
                            Point pScreen = imageToScreenCoords(p);
                            g2d.fillOval(pScreen.x - 3, pScreen.y - 3, 6, 6);
                        }
                    } else {
                        // Draw rectangle (transform to screen coords)
                        Point topLeft = imageToScreenCoords(new Point(ar.boundingBox.x, ar.boundingBox.y));
                        Point bottomRight = imageToScreenCoords(new Point(
                            ar.boundingBox.x + ar.boundingBox.width,
                            ar.boundingBox.y + ar.boundingBox.height));
                        Rectangle screenRect = new Rectangle(
                            topLeft.x, topLeft.y,
                            bottomRight.x - topLeft.x,
                            bottomRight.y - topLeft.y);
                        g2d.draw(screenRect);
                        g2d.fill(screenRect);
                    }
                    
                    // Draw label
                    g2d.setColor(Color.GREEN);
                    Point labelPos = imageToScreenCoords(new Point(ar.boundingBox.x, ar.boundingBox.y));
                    g2d.drawString(ar.label + " " + (i+1), labelPos.x + 5, labelPos.y + 20);
                }
                
                // Draw current polygon being drawn
                if (mode == DrawMode.POLYGON && !currentPolygon.isEmpty()) {
                    g2d.setColor(new Color(255, 255, 0, 150));
                    g2d.setStroke(new BasicStroke(2));
                    
                    // Draw edges
                    for (int i = 0; i < currentPolygon.size() - 1; i++) {
                        Point p1 = currentPolygon.get(i);
                        Point p2 = currentPolygon.get(i + 1);
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                    
                    // Draw preview line to mouse
                    Point mouse = getMousePosition();
                    if (mouse != null && !currentPolygon.isEmpty()) {
                        Point last = currentPolygon.get(currentPolygon.size() - 1);
                        g2d.setColor(new Color(255, 255, 0, 80));
                        g2d.drawLine(last.x, last.y, mouse.x, mouse.y);
                    }
                    
                    // Draw vertices
                    g2d.setColor(Color.YELLOW);
                    for (Point p : currentPolygon) {
                        g2d.fillOval(p.x - 4, p.y - 4, 8, 8);
                    }
                }
                
                // Draw current rectangle being drawn
                if (currentRect != null) {
                    g2d.setColor(new Color(255, 255, 0, 150));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(currentRect);
                    g2d.setColor(new Color(255, 255, 0, 50));
                    g2d.fill(currentRect);
                }
            }
        }
    }
}
