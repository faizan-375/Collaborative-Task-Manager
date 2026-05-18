package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NotificationsScreen extends JPanel {

    // Notification Type Colors
    private final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private final Color COLOR_BG_SUCCESS = new Color(230, 255, 235);

    private final Color COLOR_DANGER = new Color(220, 53, 69);
    private final Color COLOR_BG_DANGER = new Color(255, 235, 235);

    private final Color COLOR_INFO = new Color(23, 162, 184);
    private final Color COLOR_BG_INFO = new Color(225, 250, 255);

    private final Color COLOR_WARNING = new Color(255, 193, 7);
    private final Color COLOR_BG_WARNING = new Color(255, 250, 230);

    private JPanel listPanel;

    private String userRole; // Store role for permissions
    private String userName;

    public NotificationsScreen(String userRole, String userName) {
        this.userRole = (userRole != null) ? userRole.toLowerCase() : "member";
        this.userName = userName;

        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // Initialize DB
        FirebaseManager.initialize();

        // ---------------------------------------
        // 1. TOP HEADER SECTION
        // ---------------------------------------
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);
        headerContent.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("🔔 Recent Notifications");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        // --- CHANGED: ADD BUTTON INSTEAD OF MARK READ ---
        JButton addBtn = Theme.createPrimaryButton("+  Add Notification");
        addBtn.setPreferredSize(new Dimension(180, 40));

        // CHECK ROLE - REMOVED to allow all users to see the button
        // if (!this.userRole.contains("admin")) {
        // addBtn.setVisible(false);
        // }

        // Action: Open Dialog
        addBtn.addActionListener(e -> openAddNotificationDialog());

        headerContent.add(title, BorderLayout.WEST);
        // Check Admin or Project Manager
        boolean isAdmin = this.userRole.contains("admin") || this.userRole.equalsIgnoreCase("project manager")
                || (this.userName != null && this.userName.equalsIgnoreCase("Project Manager"));

        if (isAdmin) {
            headerContent.add(addBtn, BorderLayout.EAST);
        }

        add(headerContent, BorderLayout.NORTH);

        // ---------------------------------------
        // 2. SCROLLABLE NOTIFICATION LIST
        // ---------------------------------------
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        // --- LOAD DATA FROM DB ---
        loadNotificationsFromFirebase();

        // Wrapper to keep list at top
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(listPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Card Container for the list
        Theme.RoundedPanel listCard = new Theme.RoundedPanel(25, Color.WHITE);
        listCard.setLayout(new BorderLayout());
        listCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        listCard.add(scroll);

        // Outer spacer
        JPanel spacer = new JPanel(new BorderLayout());
        spacer.setOpaque(false);
        spacer.setBorder(new EmptyBorder(20, 0, 0, 0));
        spacer.add(listCard);

        add(spacer, BorderLayout.CENTER);
    }

    // ---------------------------------------
    // NEW: OPEN DIALOG TO ADD NOTIFICATION
    // ---------------------------------------
    private void openAddNotificationDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Notification", true);
        d.setSize(400, 400);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(0, 1, 10, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        Border fieldBorder = new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 8, 5, 8));

        p.add(createLabel("Title:"));
        JTextField txtTitle = new JTextField();
        txtTitle.setBorder(fieldBorder);
        p.add(txtTitle);

        p.add(createLabel("Message:"));
        JTextField txtMessage = new JTextField();
        txtMessage.setBorder(fieldBorder);
        p.add(txtMessage);

        p.add(createLabel("Type:"));
        String[] types = { "INFO", "SUCCESS", "WARNING", "DANGER" };
        JComboBox<String> cbType = new JComboBox<>(types);
        cbType.setBackground(Color.WHITE);
        p.add(cbType);

        p.add(Box.createVerticalStrut(15));

        JButton save = Theme.createPrimaryButton("Send Notification");

        save.addActionListener(e -> {
            if (!txtTitle.getText().isEmpty() && !txtMessage.getText().isEmpty()) {

                // 1. Create Data Object
                // Generate current time string (e.g., "10:30 AM")
                String time = new SimpleDateFormat("hh:mm a").format(new Date());

                Notification notif = new Notification(
                        txtTitle.getText(),
                        txtMessage.getText(),
                        time,
                        cbType.getSelectedItem().toString());

                // 2. Save to Firebase
                Firestore db = FirebaseManager.getFirestore();
                if (db != null) {
                    ApiFuture<DocumentReference> future = db.collection("notifications").add(notif);
                    try {
                        future.get(); // Wait for save
                        loadNotificationsFromFirebase(); // Refresh
                        JOptionPane.showMessageDialog(d, "Notification Sent!");
                        d.dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(d, "Please fill all fields.");
            }
        });

        p.add(save);
        d.add(p);
        d.setVisible(true);
    }

    // ---------------------------------------
    // LOAD FROM FIREBASE
    // ---------------------------------------
    private void loadNotificationsFromFirebase() {
        listPanel.removeAll(); // Clear UI

        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        ApiFuture<QuerySnapshot> future = db.collection("notifications").get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                Notification n = doc.toObject(Notification.class);
                // Store ID in Notification Object if possible, or pass it
                // Assuming Notification class might need an ID field for deleting
                addNotification(doc.getId(), n.getTitle(), n.getMessage(), n.getTime(), n.getType());
            }
            listPanel.revalidate();
            listPanel.repaint();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.err.println("Error loading notifications: " + e.getMessage());
        }
    }

    // ---------------------------------------
    // METHOD TO ADD A NOTIFICATION ROW
    // ---------------------------------------
    private void addNotification(String docId, String title, String message, String time, String type) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(15, 10, 15, 10)));

        // 1. ICON
        Color fg = COLOR_INFO;
        Color bg = COLOR_BG_INFO;
        String symbol = "ℹ";

        if ("SUCCESS".equalsIgnoreCase(type)) {
            fg = COLOR_SUCCESS;
            bg = COLOR_BG_SUCCESS;
            symbol = "✔";
        }
        if ("DANGER".equalsIgnoreCase(type)) {
            fg = COLOR_DANGER;
            bg = COLOR_BG_DANGER;
            symbol = "!";
        }
        if ("WARNING".equalsIgnoreCase(type)) {
            fg = COLOR_WARNING;
            bg = COLOR_BG_WARNING;
            symbol = "💬";
        }

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setOpaque(false);
        iconPanel.add(new CircleIcon(symbol, fg, bg));

        // 2. CONTENT
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_BOLD);
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgLbl.setForeground(Theme.TEXT_MUTED);

        textPanel.add(titleLbl);
        textPanel.add(msgLbl);

        // 3. META
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(Theme.FONT_SMALL);
        timeLbl.setForeground(Theme.TEXT_MUTED);
        timeLbl.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Close Button (For All Users)
        // if (userRole.contains("admin")) {
        boolean isAdmin = userRole.contains("admin") || userRole.equalsIgnoreCase("project manager")
                || (userName != null && userName.equalsIgnoreCase("Project Manager"));
        if (isAdmin) {
            JButton closeBtn = new JButton("×");
            closeBtn.setFont(new Font("Arial", Font.BOLD, 18));
            closeBtn.setForeground(new Color(200, 200, 200));
            closeBtn.setBorder(null);
            closeBtn.setContentAreaFilled(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.setFocusPainted(false);

            closeBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    closeBtn.setForeground(Color.RED);
                }

                public void mouseExited(MouseEvent e) {
                    closeBtn.setForeground(new Color(200, 200, 200));
                }
            });

            closeBtn.addActionListener(e -> {
                // DELETE FROM FIREBASE
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this notification?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Firestore db = FirebaseManager.getFirestore();
                    if (db != null) {
                        db.collection("notifications").document(docId).delete();
                        listPanel.remove(row);
                        listPanel.revalidate();
                        listPanel.repaint();
                    }
                }
            });
            rightPanel.add(closeBtn, BorderLayout.EAST);
        }
        // }

        rightPanel.add(timeLbl, BorderLayout.WEST);

        row.add(iconPanel, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(250, 252, 255));
            }

            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
            }
        });

        listPanel.add(row);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    // =============================================================
    // HELPER CLASSES
    // =============================================================

    private static class CircleIcon extends JComponent {
        private String symbol;
        private Color fg;
        private Color bg;

        public CircleIcon(String symbol, Color fg, Color bg) {
            this.symbol = symbol;
            this.fg = fg;
            this.bg = bg;
            setPreferredSize(new Dimension(50, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Double(10, 10, 30, 30));
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int x = 10 + (30 - fm.stringWidth(symbol)) / 2;
            int y = 10 + ((30 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(symbol, x, y);
        }
    }
}