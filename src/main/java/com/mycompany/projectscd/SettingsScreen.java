package com.mycompany.projectscd;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class SettingsScreen extends JPanel {

    private String userEmail;
    private String userName;

    public SettingsScreen(String email, String name) {
        this.userEmail = email;
        this.userName = name;

        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // ---------------------------------------
        // 1. TOP HEADER SECTION
        // ---------------------------------------
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);
        headerContent.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("⚙ Settings & Preferences");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JButton saveBtn = Theme.createPrimaryButton("💾 Save Changes");
        saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings Saved Successfully!"));

        headerContent.add(title, BorderLayout.WEST);
        headerContent.add(saveBtn, BorderLayout.EAST);

        add(headerContent, BorderLayout.NORTH);

        // ---------------------------------------
        // 2. SCROLLABLE SETTINGS CONTENT
        // ---------------------------------------
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- SECTION A: PROFILE SUMMARY ---
        content.add(createProfileHeader());
        content.add(Box.createVerticalStrut(30));
        content.add(createSeparator());
        content.add(Box.createVerticalStrut(20));

        // --- SECTION B: GENERAL SETTINGS ---
        content.add(createSectionTitle("Task Dashboard"));
        content.add(createToggleRow("Show Completed Tasks", "Keep finished tasks visible in your list"));
        content.add(createToggleRow("Compact View", "Reduce row height for denser lists"));
        content.add(Box.createVerticalStrut(20));

        // --- SECTION C: NOTIFICATIONS ---
        content.add(createSectionTitle("Notifications"));
        content.add(createToggleRow("Email Alerts", "Receive updates via email when tasks are assigned"));
        content.add(createToggleRow("In-App Popups", "Show floating notifications while working"));
        content.add(Box.createVerticalStrut(20));

        // --- SECTION D: ACCOUNT & SECURITY ---
        content.add(createSectionTitle("Account & Security"));

        // Password Row with Action
        JPanel passRow = createActionRow("Password", "Change your account password", "Update");
        JButton updatePassBtn = (JButton) passRow.getComponent(1); // Get the button
        updatePassBtn.addActionListener(e -> openChangePasswordDialog());
        content.add(passRow);

        // Logical Project Settings
        content.add(createActionRow("Session Management", "Log out of all other devices", "Sign Out All"));

        // Wrap in ScrollPane
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Card Wrapper
        Theme.RoundedPanel settingsCard = new Theme.RoundedPanel(25, Color.WHITE);
        settingsCard.setLayout(new BorderLayout());
        settingsCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        settingsCard.add(scroll);

        // Spacer
        JPanel spacer = new JPanel(new BorderLayout());
        spacer.setOpaque(false);
        spacer.setBorder(new EmptyBorder(20, 0, 0, 0));
        spacer.add(settingsCard);

        add(spacer, BorderLayout.CENTER);
    }

    private void openChangePasswordDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Password", true);
        d.setSize(400, 350);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(0, 1, 10, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(25, 25, 25, 25));

        p.add(new JLabel("Current Password:"));
        JPasswordField txtOld = new JPasswordField();
        p.add(txtOld);

        p.add(new JLabel("New Password:"));
        JPasswordField txtNew = new JPasswordField();
        p.add(txtNew);

        p.add(new JLabel("Confirm New Password:"));
        JPasswordField txtConfirm = new JPasswordField();
        p.add(txtConfirm);

        p.add(Box.createVerticalStrut(10));

        JButton btnChange = Theme.createPrimaryButton("Update Password");

        btnChange.addActionListener(ev -> {
            String oldP = new String(txtOld.getPassword());
            String newP = new String(txtNew.getPassword());
            String conP = new String(txtConfirm.getPassword());

            if (oldP.isEmpty() || newP.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Fields cannot be empty.");
                return;
            }
            if (!newP.equals(conP)) {
                JOptionPane.showMessageDialog(d, "New passwords do not match.");
                return;
            }

            // Verify and Update
            com.google.cloud.firestore.Firestore db = FirebaseManager.getFirestore();
            if (db != null) {
                btnChange.setEnabled(false);
                btnChange.setText("Updating...");

                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        // 1. Find user by email AND old password
                        com.google.api.core.ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = db
                                .collection("members")
                                .whereEqualTo("email", userEmail)
                                .whereEqualTo("password", oldP)
                                .get();

                        java.util.List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = future.get()
                                .getDocuments();

                        if (!docs.isEmpty()) {
                            // 2. Update found document
                            String docId = docs.get(0).getId();
                            db.collection("members").document(docId).update("password", newP).get();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    protected void done() {
                        btnChange.setEnabled(true);
                        btnChange.setText("Update Password");
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(d, "Password Updated Successfully!");
                                d.dispose();
                            } else {
                                JOptionPane.showMessageDialog(d, "Incorrect Current Password.");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
                        }
                    }
                }.execute();
            }
        });

        p.add(btnChange);
        d.add(p);
        d.setVisible(true);
    }

    // ---------------------------------------
    // COMPONENT: PROFILE HEADER
    // ---------------------------------------
    private JPanel createProfileHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(2000, 80));

        // Avatar
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setOpaque(false);
        String initial = (userName != null && !userName.isEmpty()) ? userName.substring(0, 1).toUpperCase() : "U";
        left.add(new CircleAvatar(initial, Theme.PRIMARY_BLUE, new Color(220, 235, 255)));

        // Text
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel name = new JLabel(userName);
        name.setFont(Theme.FONT_TITLE);
        name.setForeground(Theme.TEXT_DARK);

        JLabel email = new JLabel(userEmail);
        email.setFont(Theme.FONT_REGULAR);
        email.setForeground(Theme.TEXT_MUTED);

        textPanel.add(name);
        textPanel.add(email);
        left.add(textPanel);

        // Edit Button
        JButton editProfile = new JButton("Edit Profile");
        editProfile.setFont(Theme.FONT_BOLD);
        editProfile.setBackground(Color.WHITE);
        editProfile.setForeground(Theme.PRIMARY_BLUE);
        editProfile.setBorder(new LineBorder(Theme.PRIMARY_BLUE, 1));
        editProfile.setFocusPainted(false);
        editProfile.setPreferredSize(new Dimension(100, 30));

        p.add(left, BorderLayout.WEST);
        p.add(editProfile, BorderLayout.EAST);

        return p;
    }

    // ---------------------------------------
    // HELPER: CREATE SECTION TITLE
    // ---------------------------------------
    private JPanel createSectionTitle(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(2000, 40));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Theme.PRIMARY_BLUE);

        p.add(lbl, BorderLayout.SOUTH);
        p.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        return p;
    }

    // ---------------------------------------
    // HELPER: DROPDOWN ROW
    // ---------------------------------------
    private JPanel createComboRow(String label, String[] options) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(2000, 50));
        p.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_REGULAR);
        lbl.setForeground(Theme.TEXT_DARK);

        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(250, 35));

        p.add(lbl, BorderLayout.WEST);
        p.add(combo, BorderLayout.EAST);
        return p;
    }

    // ---------------------------------------
    // HELPER: TOGGLE ROW (CHECKBOX)
    // ---------------------------------------
    private JPanel createToggleRow(String label, String description) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(2000, 50));
        p.setBorder(new EmptyBorder(5, 0, 5, 0));

        // Text Part
        JPanel textP = new JPanel(new GridLayout(2, 1));
        textP.setOpaque(false);

        JLabel l1 = new JLabel(label);
        l1.setFont(Theme.FONT_REGULAR);
        l1.setForeground(Theme.TEXT_DARK);

        JLabel l2 = new JLabel(description);
        l2.setFont(Theme.FONT_SMALL);
        l2.setForeground(Theme.TEXT_MUTED);

        textP.add(l1);
        textP.add(l2);

        // Checkbox
        JCheckBox cb = new JCheckBox();
        cb.setBackground(Color.WHITE);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.add(textP, BorderLayout.CENTER);
        p.add(cb, BorderLayout.EAST);
        return p;
    }

    // ---------------------------------------
    // HELPER: ACTION BUTTON ROW
    // ---------------------------------------
    private JPanel createActionRow(String label, String description, String btnText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(2000, 50));
        p.setBorder(new EmptyBorder(5, 0, 5, 0));

        JPanel textP = new JPanel(new GridLayout(2, 1));
        textP.setOpaque(false);

        JLabel l1 = new JLabel(label);
        l1.setFont(Theme.FONT_REGULAR);
        l1.setForeground(Theme.TEXT_DARK);

        JLabel l2 = new JLabel(description);
        l2.setFont(Theme.FONT_SMALL);
        l2.setForeground(Theme.TEXT_MUTED);

        textP.add(l1);
        textP.add(l2);

        JButton btn = new JButton(btnText);
        btn.setFont(Theme.FONT_BOLD);
        btn.setBackground(new Color(245, 245, 245));
        btn.setForeground(Theme.TEXT_DARK);
        btn.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 30));

        p.add(textP, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    private JSeparator createSeparator() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(220, 220, 220));
        s.setMaximumSize(new Dimension(2000, 1));
        return s;
    }

    // =============================================================
    // INTERNAL HELPER CLASSES
    // =============================================================

    private static class CircleAvatar extends JComponent {
        private String letter;
        private Color fg, bg;

        public CircleAvatar(String letter, Color fg, Color bg) {
            this.letter = letter;
            this.fg = fg;
            this.bg = bg;
            setPreferredSize(new Dimension(60, 60));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, (getWidth() - fm.stringWidth(letter)) / 2,
                    ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
        }
    }
}