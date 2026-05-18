package com.mycompany.projectscd;

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class IntroPanel extends JPanel {

    private final Runnable onComplete;
    private final Timer timer;

    // Animation State
    private int tick = 0;

    private static final int PHASE_CTM_ENTER = 0;
    private static final int PHASE_TRANSITION = 1;
    private static final int PHASE_FULL_TEXT = 2;
    private static final int PHASE_EXIT = 3;

    private int phase = PHASE_CTM_ENTER;

    // Properties
    private double ctmScale = 0.0;
    private float ctmAlpha = 0.0f;

    private double fullTextScale = 0.5;
    private float fullTextAlpha = 0.0f;

    private double globalScale = 1.0;
    private float globalAlpha = 1.0f;

    public IntroPanel(Runnable onComplete) {
        this.onComplete = onComplete;
        setBackground(new Color(245, 245, 250));
        setLayout(null);

        // 60 FPS
        timer = new Timer(16, e -> updateLoop());
        timer.start();
    }

    private void updateLoop() {
        tick++;

        switch (phase) {
            case PHASE_CTM_ENTER:
                // "CTM" zooms in from 0 to 1
                // Duration: 0.8s (48 ticks)
                double t = Math.min(1.0, tick / 48.0);
                // Elastic out
                ctmScale = elasticOut(t);
                ctmAlpha = (float) Math.min(1.0, t * 2); // Fade in faster

                if (t >= 1.0) {
                    tick = 0;
                    phase = PHASE_TRANSITION;
                }
                break;

            case PHASE_TRANSITION:
                // "CTM" expands massive and fades out
                // "Collaborative Task Manager" zooms in from 0.8 to 1.0 and fades in
                // Duration: 1.2s (72 ticks)
                double t2 = Math.min(1.0, tick / 72.0);
                double easeInOut = (t2 < 0.5) ? 2 * t2 * t2 : -1 + (4 - 2 * t2) * t2;

                // CTM goes 1.0 -> 3.0 scale, alpha 1.0 -> 0.0
                ctmScale = 1.0 + (2.0 * easeInOut);
                ctmAlpha = (float) Math.max(0, 1.0 - (t2 * 1.5)); // Fades out by 2/3rds

                // Full Text goes 0.5 -> 1.0 scale, alpha 0.0 -> 1.0
                fullTextScale = 0.5 + (0.5 * easeInOut);
                fullTextAlpha = (float) t2;

                if (t2 >= 1.0) {
                    tick = 0;
                    phase = PHASE_FULL_TEXT;
                }
                break;

            case PHASE_FULL_TEXT:
                // Hold for 1.5s
                if (tick >= 90) {
                    tick = 0;
                    phase = PHASE_EXIT;
                }
                break;

            case PHASE_EXIT:
                // Zoom through
                double t3 = Math.min(1.0, tick / 30.0);
                double easeEx = t3 * t3 * t3;

                globalScale = 1.0 + (10.0 * easeEx);
                globalAlpha = (float) (1.0 - t3);

                if (t3 >= 1.0) {
                    timer.stop();
                    if (onComplete != null)
                        onComplete.run();
                }
                break;
        }

        repaint();
    }

    private double elasticOut(double t) {
        if (t == 0)
            return 0;
        if (t == 1)
            return 1;
        double p = 0.3;
        return Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (2 * Math.PI) / p) + 1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;

        // --- GLOBAL EXIT TRANSFORM ---
        AffineTransform baseTr = g2.getTransform();
        g2.translate(cx, cy);
        g2.scale(globalScale, globalScale);
        g2.translate(-cx, -cy);

        if (globalAlpha < 1.0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, globalAlpha)));
        }

        // --- DRAW CTM ---
        if (ctmAlpha > 0.01f) {
            g2.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, ctmAlpha * globalAlpha)));

            AffineTransform ctmTr = g2.getTransform();
            g2.translate(cx, cy);
            g2.scale(ctmScale, ctmScale);

            g2.setColor(new Color(20, 60, 100)); // Dark Navy
            g2.setFont(new Font("Segoe UI", Font.BOLD, 120)); // Huge Font
            String text = "CTM";
            FontMetrics fm = g2.getFontMetrics();
            int tx = -fm.stringWidth(text) / 2;
            int ty = fm.getAscent() / 2 - 10;

            g2.drawString(text, tx, ty);
            g2.setTransform(ctmTr);
        }

        // --- DRAW COLLABORATIVE TASK MANAGER ---
        if (fullTextAlpha > 0.01f) {
            float combinedAlpha = Math.min(1.0f, fullTextAlpha * globalAlpha);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, combinedAlpha));

            AffineTransform fullTr = g2.getTransform();
            g2.translate(cx, cy);
            g2.scale(fullTextScale, fullTextScale);

            g2.setColor(new Color(20, 60, 100));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
            String title = "Collaborative Task Manager";
            FontMetrics fm = g2.getFontMetrics();
            int tx = -fm.stringWidth(title) / 2;
            int ty = fm.getAscent() / 2 - 10;
            g2.drawString(title, tx, ty);

            // Optional Subtitle
            /*
             * g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
             * g2.setColor(new Color(100, 100, 120));
             * String sub = "Streamline your workflow";
             * g2.drawString(sub, -g2.getFontMetrics().stringWidth(sub)/2, ty + 40);
             */

            g2.setTransform(fullTr);
        }

        g2.setTransform(baseTr);
    }
}
