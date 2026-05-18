package com.mycompany.projectscd;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GlowingProgressBar extends JComponent {
    private int value = 0;
    private final int MAX_VALUE = 100;
    private Color themeColor;

    public GlowingProgressBar(Color color) {
        this.themeColor = color;
        setPreferredSize(new Dimension(200, 12)); // Increased height slightly for glow
        setOpaque(false);
    }

    public void setValue(int v) {
        this.value = Math.max(0, Math.min(v, MAX_VALUE));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int barH = h - 4; // Actual bar height (padding for glow)
        int y = 2;

        // 1. Background Track
        g2.setColor(new Color(240, 240, 245));
        g2.fill(new RoundRectangle2D.Double(0, y, w, barH, barH, barH));

        // Inner Shadow / Border for depth
        g2.setColor(new Color(220, 220, 230));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(0, y, w - 1, barH - 1, barH, barH));

        if (value > 0) {
            // Calc fill width
            double fillW = (value / (double) MAX_VALUE) * w;
            fillW = Math.max(fillW, barH); // Min width is height for circle cap

            // 2. Glow Effect (Soft shadowed layer behind)
            // We draw a slightly larger, translucent version of the bar
            Color glowColor = new Color(
                    themeColor.getRed(),
                    themeColor.getGreen(),
                    themeColor.getBlue(),
                    100 // Alpha
            );

            // Multiple layers for "bloom" look
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 40));
            g2.fill(new RoundRectangle2D.Double(0, 0, fillW, h, h, h)); // Full height (outer glow)

            // 3. Main Gradient Fill
            // Gradient from slightly lighter to main color
            Color colorStart = new Color(
                    Math.min(255, themeColor.getRed() + 40),
                    Math.min(255, themeColor.getGreen() + 40),
                    Math.min(255, themeColor.getBlue() + 40));
            GradientPaint gp = new GradientPaint(0, y, colorStart, (float) fillW, y, themeColor);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, y, fillW, barH, barH, barH));

            // 4. "Light" Shine (Top highlight)
            g2.setPaint(new Color(255, 255, 255, 70));
            g2.fill(new RoundRectangle2D.Double(2, y + 1, fillW - 4, barH / 2.0, barH, barH));
        }
    }
}
