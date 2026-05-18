package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsPage extends JPanel {

    public ReportsPage() {
        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("📊 Analytical Reports");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Content - 2 Columns
        JPanel content = new JPanel(new GridLayout(1, 2, 25, 0));
        content.setOpaque(false);

        // Chart Panels
        PieChartPanel piePanel = new PieChartPanel();
        BarChartPanel barPanel = new BarChartPanel();

        content.add(piePanel);
        content.add(barPanel);

        add(content, BorderLayout.CENTER);

        // Load Data
        loadChartData(piePanel, barPanel);
    }

    private void loadChartData(PieChartPanel pie, BarChartPanel bar) {
        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        new SwingWorker<Void, Void>() {
            Map<String, Integer> statusCount = new HashMap<>();

            // For now, let's use fixed categories for bar chart demo, or derived from data
            // We'll count tasks per priority for the bar chart as an example
            Map<String, Integer> priorityCount = new HashMap<>();

            @Override
            protected Void doInBackground() throws Exception {
                ApiFuture<QuerySnapshot> future = db.collection("tasks").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();

                // Initialize maps
                statusCount.put("To Do", 0);
                statusCount.put("In Progress", 0);
                statusCount.put("Done", 0);

                priorityCount.put("Low", 0);
                priorityCount.put("Medium", 0);
                priorityCount.put("High", 0);

                for (QueryDocumentSnapshot doc : docs) {
                    String status = doc.getString("status");
                    String priority = doc.getString("priority");

                    if (status != null)
                        statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);

                    if (priority != null)
                        priorityCount.put(priority, priorityCount.getOrDefault(priority, 0) + 1);
                }
                return null;
            }

            @Override
            protected void done() {
                pie.setData(statusCount);
                bar.setData(priorityCount);
                pie.startAnimation();
                bar.startAnimation();
            }
        }.execute();
    }

    // =========================================
    // PIE CHART PANEL
    // =========================================
    private static class PieChartPanel extends Theme.RoundedPanel {
        private Map<String, Integer> data = new HashMap<>();
        private final Color[] colors = { Theme.PRIMARY_BLUE, Theme.FG_ORANGE, Theme.FG_GREEN };
        private final String[] keys = { "To Do", "In Progress", "Done" }; // Order matters for colors

        // Animation
        private double animProgress = 0.0;
        private Timer animTimer;

        public PieChartPanel() {
            super(20, Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("Task Status Distribution");
            title.setFont(Theme.FONT_TITLE);
            title.setForeground(Theme.TEXT_DARK);
            add(title, BorderLayout.NORTH);
        }

        public void startAnimation() {
            animProgress = 0.0;
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(16, e -> {
                animProgress += 0.03; // Speed
                if (animProgress >= 1.0) {
                    animProgress = 1.0;
                    animTimer.stop();
                }
                repaint();
            });
            animTimer.start();
        }

        public void setData(Map<String, Integer> d) {
            this.data = d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty())
                return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int minDim = Math.min(width, height) - 100; // Padding
            int x = (width - minDim) / 2;
            int y = (height - minDim) / 2 + 20;

            double total = 0;
            for (int val : data.values())
                total += val;

            double maxAngle = 360 * animProgress;
            double currentShapeStart = 90;

            // Draw Pie
            if (total > 0) {
                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i];
                    int val = data.getOrDefault(key, 0);
                    double angle = (val / total) * 360;

                    // Determine how much of this slice we can draw based on maxAngle
                    // We consume maxAngle as we go around
                    double angleToDraw = 0;

                    // Logic: The pie fills clockwise starting from 12 o'clock (90 deg).
                    // We want to draw the full slice if it's within the animated range.
                    // But simpler visual: just scale the sweep of EACH slice?
                    // No, user asked for "filling in the clock wise", distinct from scaling.
                    // "Starts filling in the clock wise".
                    // So we must draw slice by slice up to the total animated angle.

                    if (maxAngle <= 0)
                        break;

                    if (maxAngle >= angle) {
                        angleToDraw = angle;
                        maxAngle -= angle;
                    } else {
                        angleToDraw = maxAngle;
                        maxAngle = 0;
                    }

                    g2.setColor(colors[i]);
                    g2.fill(new Arc2D.Double(x, y, minDim, minDim, currentShapeStart, -angleToDraw, Arc2D.PIE));

                    currentShapeStart -= angle; // Advancing the real start for next slice
                }
            } else {
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawOval(x, y, minDim, minDim);
                g2.drawString("No Data", x + minDim / 2 - 20, y + minDim / 2);
            }

            // Draw Legend
            int lx = 20;
            int ly = height - 40;
            for (int i = 0; i < keys.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(lx, ly, 15, 15, 4, 4);

                g2.setColor(Theme.TEXT_DARK);
                g2.setFont(Theme.FONT_SMALL);
                g2.drawString(keys[i] + " (" + data.getOrDefault(keys[i], 0) + ")", lx + 20, ly + 12);

                lx += 100;
            }
        }
    }

    // =========================================
    // BAR CHART PANEL
    // =========================================
    private static class BarChartPanel extends Theme.RoundedPanel {
        private Map<String, Integer> data = new HashMap<>();
        // Priority Colors
        private final Map<String, Color> colorMap = new HashMap<>();

        // Animation
        private double animProgress = 0.0;
        private Timer animTimer;

        public BarChartPanel() {
            super(20, Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("Tasks by Priority");
            title.setFont(Theme.FONT_TITLE);
            title.setForeground(Theme.TEXT_DARK);
            add(title, BorderLayout.NORTH);

            colorMap.put("High", Theme.FG_RED);
            colorMap.put("Medium", Theme.FG_ORANGE);
            colorMap.put("Low", Theme.FG_GREEN);
        }

        public void startAnimation() {
            animProgress = 0.0;
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(16, e -> {
                animProgress += 0.03; // Speed
                if (animProgress >= 1.0) {
                    animProgress = 1.0;
                    animTimer.stop();
                }
                repaint();
            });
            animTimer.start();
        }

        public void setData(Map<String, Integer> d) {
            this.data = d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty())
                return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padding = 40;
            int graphW = w - 2 * padding;
            int graphH = h - 2 * padding - 30; // 30 for title space
            int x0 = padding;
            int y0 = h - padding;

            // Find max value
            int maxVal = 0;
            for (int val : data.values())
                maxVal = Math.max(maxVal, val);
            if (maxVal == 0)
                maxVal = 1; // Avoid div by zero

            // Draw Bars
            String[] keys = { "High", "Medium", "Low" };
            int barWidth = graphW / (keys.length * 2);
            int spacing = barWidth;

            int curX = x0 + spacing / 2;

            for (String key : keys) {
                int val = data.getOrDefault(key, 0);
                int fullHeight = (int) ((double) val / maxVal * graphH);
                int barHeight = (int) (fullHeight * animProgress);

                // Bar
                g2.setColor(colorMap.getOrDefault(key, Color.GRAY));
                g2.fillRoundRect(curX, y0 - barHeight, barWidth, barHeight, 10, 10);

                // Label
                g2.setColor(Theme.TEXT_DARK);
                g2.setFont(Theme.FONT_BOLD); // Make priority text bolder
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(key);
                g2.drawString(key, curX + (barWidth - labelW) / 2, y0 + 20);

                // Value Top
                String valStr = String.valueOf(val);
                int valW = fm.stringWidth(valStr);
                g2.drawString(valStr, curX + (barWidth - valW) / 2, y0 - barHeight - 5);

                curX += barWidth + spacing;
            }
        }
    }
}
