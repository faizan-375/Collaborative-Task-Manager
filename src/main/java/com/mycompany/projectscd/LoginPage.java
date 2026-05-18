package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class LoginPage extends JFrame {

    private JTextField emailField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;

    // Design Colors
    private final Color COLOR_BLUE_DARK = new Color(20, 80, 160); // Deep Blue
    private final Color COLOR_BLUE_LIGHT = new Color(60, 130, 240); // Lighter Blue
    private final Color COLOR_TEXT_GRAY = new Color(150, 150, 150);

    public LoginPage() {
        setTitle("DevOrbit - Login");
        setSize(900, 600); // Widescreen for the split view
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 500)); // Min size for mobile view

        // 1. INITIALIZE DATABASE
        try {
            FirebaseManager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Main Container (CardLayout-like behavior manually managed or GridBag)
        // We'll use a container with a layout that we control visibility on
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setBackground(Color.WHITE);

        // 3. Left Panel (Brand)
        leftPanel = createLeftPanel();

        // 4. Right Panel (Form)
        rightPanel = createRightPanel();

        // Add to main
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);

        // 5. Responsive Logic
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                checkResponsiveLayout();
            }
        });

        // Initial check
        SwingUtilities.invokeLater(this::checkResponsiveLayout);
    }

    private void checkResponsiveLayout() {
        int width = getWidth();
        if (width < 750) {
            // Mobile Mode: Hide Left Panel
            if (leftPanel.isVisible()) {
                leftPanel.setVisible(false);
            }
        } else {
            // Desktop Mode: Show Left Panel
            if (!leftPanel.isVisible()) {
                leftPanel.setVisible(true);
            }
        }
        revalidate();
        repaint();
    }

    // ---------------------------------------------------
    // LEFT PANEL: Cloud/Wave Design + Branding
    // ---------------------------------------------------
    private JPanel createLeftPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 1. Background Gradient
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BLUE_LIGHT, 0, h, COLOR_BLUE_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // 2. Cloud/Wave Decoration on Right Edge
                // We draw white curvy shapes overlaying the blue on the right side
                // to bridge into the white right panel
                g2.setColor(Color.WHITE);

                GeneralPath wave = new GeneralPath();
                wave.moveTo(w, 0); // Top Right
                wave.lineTo(w, h); // Bottom Right
                wave.lineTo(w - 50, h); // Start curves from bottom

                // Random-ish curves going up
                wave.curveTo(w - 100, h - 100, w - 20, h - 200, w - 60, h - 300);
                wave.curveTo(w - 100, h - 400, w - 20, h - 500, w - 60, 0);

                wave.closePath();
                // To do "Clouds" properly like the image requires multiple overlapping circles
                // Let's draw some overlapping semi-transparent white circles to mimic clouds

                // Helper to draw cloud
                drawCloud(g2, w - 20, 100, 120);
                drawCloud(g2, w - 40, 250, 140);
                drawCloud(g2, w - 10, 400, 100);
                drawCloud(g2, w - 30, 550, 160);
            }

            private void drawCloud(Graphics2D g2, int x, int y, int size) {
                g2.setColor(new Color(255, 255, 255, 50)); // Semi-transparent
                g2.fillOval(x - size / 2, y - size / 2, size, size);

                g2.setColor(new Color(255, 255, 255, 100)); // Inner brighter
                g2.fillOval(x - size / 2 + 20, y - size / 2 + 10, size - 40, size - 40);
            }
        };
        p.setPreferredSize(new Dimension(450, 0)); // Fixed width for desktop
        p.setLayout(new GridBagLayout());

        // Add padding to prevent cut-off
        p.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Content
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Icon
        JLabel icon = new JLabel("\uD83E\uDD16"); // Robot Emoji
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        icon.setForeground(Color.WHITE);
        p.add(icon, gbc);

        gbc.gridy++;
        p.add(Box.createVerticalStrut(20), gbc);

        // Brand Name
        gbc.gridy++;
        JLabel brand = new JLabel("DevOrbit");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 36));
        brand.setForeground(Color.WHITE);
        p.add(brand, gbc);

        gbc.gridy++;
        p.add(Box.createVerticalStrut(20), gbc);

        // Description
        gbc.gridy++;
        JTextArea desc = new JTextArea("Collaborative Task Management\nfor High Performance Teams.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        desc.setForeground(new Color(255, 255, 255, 200));
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setAlignmentX(CENTER_ALIGNMENT);
        // Center align text
        // JTextArea is hard to center align content natively
        // Let's just use HTML Label
        JLabel descLbl = new JLabel(
                "<html><center>Collaborative Task Management<br>for High Performance Teams.</center></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLbl.setForeground(new Color(255, 255, 255, 200));

        p.add(descLbl, gbc);

        return p;
    }

    // ---------------------------------------------------
    // RIGHT PANEL: Login Form
    // ---------------------------------------------------
    private JPanel createRightPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Mobile Header (Only visible if left panel hidden? Hard to detect here cleanly
        // without complex logic)
        // For simplicity, we just show "Sign In"

        // Logo (Small) - Optional
        JLabel icon = new JLabel("\uD83E\uDD16", JLabel.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        p.add(icon, gbc);

        gbc.gridy++;
        JLabel title = new JLabel("Welcome Back", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COLOR_BLUE_DARK);
        p.add(title, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("Sign in to your account", JLabel.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(COLOR_TEXT_GRAY);
        p.add(sub, gbc);

        gbc.gridy++;
        p.add(Box.createVerticalStrut(30), gbc);

        // INPUTS
        gbc.gridy++;
        p.add(createLabel("Email Address"), gbc);

        gbc.gridy++;
        emailField = new JTextField();
        styleTextField(emailField);
        p.add(emailField, gbc);

        gbc.gridy++;
        p.add(createLabel("Password"), gbc);

        gbc.gridy++;
        passField = new JPasswordField();
        styleTextField(passField);
        p.add(passField, gbc);

        gbc.gridy++;
        p.add(Box.createVerticalStrut(10), gbc);

        // Terms checkbox styled
        gbc.gridy++;
        JCheckBox terms = new JCheckBox("Keep me signed in");
        terms.setBackground(Color.WHITE);
        terms.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        terms.setForeground(COLOR_TEXT_GRAY);
        terms.setFocusPainted(false);
        p.add(terms, gbc);

        gbc.gridy++;
        p.add(Box.createVerticalStrut(30), gbc);

        // BUTTONS
        gbc.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        loginBtn = new RoundedButton("Sign In", COLOR_BLUE_LIGHT, Color.WHITE);
        loginBtn.addActionListener(e -> performLogin());

        JButton signUpBtnWrapper = new RoundedButton("Sign Up", Color.WHITE, COLOR_BLUE_LIGHT);
        // Sign Up just shows a message for now as per previous logic (or lack thereof)
        signUpBtnWrapper
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Contact Admin to create an account."));

        btnPanel.add(loginBtn);
        btnPanel.add(signUpBtnWrapper); // Visual placeholder for the "dual pill" look

        p.add(btnPanel, gbc);

        return p;
    }

    // UI Helpers
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(COLOR_TEXT_GRAY);
        return l;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200))); // Underline only
        tf.setBackground(Color.WHITE);
        tf.setPreferredSize(new Dimension(200, 35));
    }

    // ---------------------------------------------------
    // DATABASE LOGIN FUNCTION
    // ---------------------------------------------------
    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password.");
            return;
        }

        loginBtn.setText("...");
        loginBtn.setEnabled(false);

        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                return getUserDetails(email, pass);
            }

            @Override
            protected void done() {
                if (!isDisplayable())
                    return;

                loginBtn.setText("Sign In");
                loginBtn.setEnabled(true);
                try {
                    String[] userDetails = get();
                    if (userDetails != null) {
                        String role = userDetails[0];
                        String name = userDetails[1];

                        // Success Animation or Transition
                        // JOptionPane.showMessageDialog(LoginPage.this, "Welcome, " + name);
                        // Removed popup for smoother feel

                        dispose();

                        new IntroScreen(() -> {
                            boolean isAdmin = role.equalsIgnoreCase("admin")
                                    || role.equalsIgnoreCase("project manager")
                                    || email.equalsIgnoreCase("admin@gmail.com");
                            if (isAdmin) {
                                new AdminDashboard(email, role, name).setVisible(true);
                            } else {
                                new UserDashboard(email, role, name).setVisible(true);
                            }
                        }).setVisible(true);

                    } else {
                        JOptionPane.showMessageDialog(LoginPage.this, "Invalid credentials.", "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginPage.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private String[] getUserDetails(String email, String password) {
        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return null;

        try {
            ApiFuture<QuerySnapshot> future = db.collection("members")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .get();

            QuerySnapshot documents = future.get();
            if (!documents.isEmpty()) {
                String role = documents.getDocuments().get(0).getString("role");
                String name = documents.getDocuments().get(0).getString("name");
                role = (role != null) ? role : "Member";
                name = (name != null) ? name : "User";
                return new String[] { role, name };
            }
            return null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Custom Rounded Button
    class RoundedButton extends JButton {
        private Color bgColor;
        private Color fgColor;

        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 45));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Pill
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 45, 45));

            // Border if white bg
            if (bgColor.equals(Color.WHITE)) {
                g2.setColor(fgColor);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 45, 45));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
