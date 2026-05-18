package com.mycompany.projectscd;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Theme {

    // --- COLORS (VIBRANT & PROFESSIONAL) ---
    public static final Color PRIMARY_BLUE = new Color(0, 110, 255); // Electric Blue
    public static final Color PRIMARY_HOVER = new Color(0, 90, 220);

    public static final Color LIGHT_BG = new Color(248, 250, 252);
    public static final Color TEXT_DARK = new Color(15, 23, 42); // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139); // Slate 500
    public static final Color WHITE = Color.WHITE;

    // BADGE COLORS (Backgrounds)
    public static final Color BG_ORANGE = new Color(255, 247, 237);
    public static final Color BG_GREEN = new Color(240, 253, 244);
    public static final Color BG_PURPLE = new Color(250, 245, 255);
    public static final Color BG_RED = new Color(254, 242, 242);
    public static final Color BG_BLUE = new Color(239, 246, 255);

    // BADGE COLORS (Text/Foreground - High Contrast)
    public static final Color FG_ORANGE = new Color(194, 65, 12); // Orange 700
    public static final Color FG_GREEN = new Color(21, 128, 61); // Green 700
    public static final Color FG_PURPLE = new Color(126, 34, 206); // Purple 700
    public static final Color FG_RED = new Color(185, 28, 28); // Red 700
    public static final Color FG_BLUE = new Color(29, 78, 216); // Blue 700

    public static final Color SIDEBAR_BG_START = new Color(15, 23, 42); // Dark Slate
    public static final Color SIDEBAR_BG_END = new Color(30, 41, 59); // Slightly lighter

    // --- FONTS ---
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_ICON = new Font("Segoe UI Emoji", Font.PLAIN, 20);

    // --- COMPONENTS ---

    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int r, Color bg) {
            this.radius = r;
            this.bgColor = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }

    public static class HoverPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public HoverPanel(int r, Color bg) {
            this.radius = r;
            this.bgColor = bg;
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(220, 220, 220));
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, radius, radius);

            // Background
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, radius, radius);

            super.paintComponent(g);
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_BLUE.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY_BLUE);
            }
        });
        return btn;
    }

    public static JButton createIconButton(String icon, Color bg, Color fg) {
        JButton btn = new JButton(icon);
        btn.setFont(FONT_ICON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }
}
