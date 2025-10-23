package com.doughvision;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel for displaying live vision feed and detection results
 */
public class VisionPanel extends JPanel {
    
    private BufferedImage currentFrame;
    private String statusText = "No camera connected";
    private int detectionCount = 0;
    private boolean showOverlay = true;
    
    public VisionPanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    }
    
    public void updateFrame(BufferedImage frame) {
        this.currentFrame = frame;
        repaint();
    }
    
    public void setStatusText(String text) {
        this.statusText = text;
        repaint();
    }
    
    public void setDetectionCount(int count) {
        this.detectionCount = count;
        repaint();
    }
    
    public void setShowOverlay(boolean show) {
        this.showOverlay = show;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        if (currentFrame != null) {
            // Scale image to fit panel while maintaining aspect ratio
            double scale = Math.min(
                (double) width / currentFrame.getWidth(),
                (double) height / currentFrame.getHeight()
            );
            
            int scaledWidth = (int) (currentFrame.getWidth() * scale);
            int scaledHeight = (int) (currentFrame.getHeight() * scale);
            
            int x = (width - scaledWidth) / 2;
            int y = (height - scaledHeight) / 2;
            
            g2d.drawImage(currentFrame, x, y, scaledWidth, scaledHeight, this);
            
            // Draw overlay information
            if (showOverlay) {
                drawOverlay(g2d, x, y, scaledWidth, scaledHeight);
            }
        } else {
            // Draw placeholder
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, width, height);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            String msg = "No Video Feed";
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, (width - msgWidth) / 2, height / 2);
        }
        
        // Draw status bar at bottom
        drawStatusBar(g2d, width, height);
    }
    
    private void drawOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        // Draw detection count
        g2d.setColor(new Color(0, 255, 0, 200));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String countText = "Dough Count: " + detectionCount;
        g2d.drawString(countText, x + 10, y + 30);
        
        // Draw frame border
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, width, height);
    }
    
    private void drawStatusBar(Graphics2D g2d, int width, int height) {
        // Background
        g2d.setColor(new Color(50, 50, 50, 220));
        g2d.fillRect(0, height - 30, width, 30);
        
        // Status text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString(statusText, 10, height - 10);
        
        // Timestamp
        String time = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        FontMetrics fm = g2d.getFontMetrics();
        int timeWidth = fm.stringWidth(time);
        g2d.drawString(time, width - timeWidth - 10, height - 10);
    }
}
