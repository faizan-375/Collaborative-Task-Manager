package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class TeamMembersScreen extends JPanel {

    // Avatar Background Colors (Pre-defined themes)
    private final Color[][] THEMES = {
            { new Color(100, 50, 160), new Color(240, 230, 255) }, // Purple
            { new Color(30, 80, 160), new Color(220, 235, 255) }, // Blue
            { new Color(40, 120, 60), new Color(230, 255, 235) }, // Green
            { new Color(180, 90, 20), new Color(255, 240, 220) }, // Orange
            { new Color(180, 40, 40), new Color(255, 230, 230) } // Red
    };

    private JPanel grid;
    private JComboBox<String> filterCombo;
    private String currentFilter = "All";
    private final String currentUserRole;
    private final String currentUserName;
    private List<Member> cachedMembers = null;

    public TeamMembersScreen(String userRole, String userName) {
        this.currentUserRole = userRole;
        this.currentUserName = userName;
        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. INITIALIZE FIREBASE
        FirebaseManager.initialize();

        // ---------------------------------------
        // 2. TOP HEADER SECTION
        // ---------------------------------------
        JPanel headerContent = new JPanel(new BorderLayout(15, 0));
        headerContent.setOpaque(false);
        headerContent.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Title
        JLabel title = new JLabel("👥 Team Directory");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        // RIGHT SIDE: Filter + Add Button
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);

        // FILTER DROPDOWN
        String[] categories = { "All", "Planning", "Analysis", "Design", "Implementation", "Testing",
                "Maintenance" };
        filterCombo = new JComboBox<>(categories);
        filterCombo.setPreferredSize(new Dimension(180, 40));
        filterCombo.setBackground(Color.WHITE);
        filterCombo.setFont(Theme.FONT_REGULAR);
        filterCombo.setFocusable(false);

        filterCombo.addActionListener(e -> {
            currentFilter = (String) filterCombo.getSelectedItem();
            refreshGrid();
        });

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(Theme.FONT_BOLD);
        controls.add(filterLbl);
        controls.add(filterCombo);

        // ADD BUTTON
        JButton addBtn = Theme.createPrimaryButton("+  Add Member");
        addBtn.addActionListener(e -> openAddMemberDialog());

        // HIDE ADD BUTTON IF NOT ADMIN/PM
        boolean isAdmin = userRole != null
                && (userRole.toLowerCase().contains("admin") || userRole.equalsIgnoreCase("project manager"))
                || (currentUserName != null && currentUserName.equalsIgnoreCase("Project Manager"));
        if (!isAdmin) {
            addBtn.setVisible(false);
        } else {
            controls.add(addBtn);
        }

        headerContent.add(title, BorderLayout.WEST);
        headerContent.add(controls, BorderLayout.EAST);

        add(headerContent, BorderLayout.NORTH);

        // ---------------------------------------
        // 3. MEMBER GRID
        // ---------------------------------------
        grid = new JPanel(new GridLayout(0, 3, 30, 30)); // Increased spacing
        grid.setBackground(Theme.LIGHT_BG);
        grid.setBorder(new EmptyBorder(10, 5, 25, 5));

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.LIGHT_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        // ---------------------------------------
        // 4. LOAD REAL DATA FROM FIREBASE
        // ---------------------------------------
        loadMembersFromFirebase();
    }

    // ---------------------------------------
    // READ DATA FROM FIREBASE
    // ---------------------------------------
    private void loadMembersFromFirebase() {
        grid.removeAll();
        grid.revalidate();
        grid.repaint();

        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        // Get all documents from "members" collection
        ApiFuture<QuerySnapshot> future = db.collection("members").get();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Get the list of documents
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                cachedMembers = new java.util.ArrayList<>();

                for (QueryDocumentSnapshot document : documents) {
                    try {
                        Member member = document.toObject(Member.class);
                        member.setId(document.getId());

                        // NEW: Check Performance (Logic runs here to be up-to-date)
                        PerformanceManager.checkAndDeductPerformance(member);

                        cachedMembers.add(member);
                    } catch (Exception e) {
                        System.err.println("Skipping malformed member");
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                refreshGrid();
            }
        }.execute();
    }

    // REFRESF GRID BASED ON CACHE AND FILTER
    private void refreshGrid() {
        grid.removeAll();
        if (cachedMembers != null) {
            int themeIndex = 0;
            for (Member m : cachedMembers) {
                // FILTER LOGIC
                boolean matches = "All".equals(currentFilter) ||
                        (m.getSdlcCategory() != null && m.getSdlcCategory().equalsIgnoreCase(currentFilter));

                if (matches) {
                    addMemberToGrid(m, themeIndex % THEMES.length);
                    themeIndex++;
                }
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    // ---------------------------------------
    // UI HELPER: ADD CARD TO GRID
    // ---------------------------------------
    private void addMemberToGrid(Member member, int themeIndex) {
        if (themeIndex < 0 || themeIndex >= THEMES.length)
            themeIndex = 0;
        Color fg = THEMES[themeIndex][0];
        Color bg = THEMES[themeIndex][1];

        grid.add(createMemberCard(member, fg, bg));
    }

    private void openAddMemberDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        AddMemberDialog d = new AddMemberDialog(parent);
        d.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadMembersFromFirebase();
            }
        });
        d.setVisible(true);
    }

    // Member Overload for Edit
    private void openEditMemberDialog(Member m) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        AddMemberDialog d = new AddMemberDialog(parent, m, m.getId());
        d.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadMembersFromFirebase();
            }
        });
        d.setVisible(true);
    }

    // DELETE MEMBER
    private void deleteMember(String id, String name) {
        if (id == null)
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete member: " + name + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Firestore db = FirebaseManager.getFirestore();
            if (db != null) {
                db.collection("members").document(id).delete();
                // Optimistic remove for speed
                if (cachedMembers != null) {
                    cachedMembers.removeIf(m -> m.getId() != null && m.getId().equals(id));
                }
                refreshGrid();
                JOptionPane.showMessageDialog(this, "Member deleted.");
            }
        }
    }

    // ---------------------------------------
    // CREATE CARD UI
    // ---------------------------------------
    private JPanel createMemberCard(Member member, Color fgColor, Color bgColor) {
        Theme.HoverPanel card = new Theme.HoverPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(250, 260));

        // CENTER: Avatar + Info
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        String initial = (member.getName() != null && member.getName().length() > 0) ? member.getName().substring(0, 1)
                : "?";
        CircleAvatar avatar = new CircleAvatar(initial, fgColor, bgColor);
        avatarPanel.add(avatar);

        JLabel nameLbl = new JLabel(member.getName());
        nameLbl.setFont(Theme.FONT_TITLE);
        nameLbl.setForeground(Theme.TEXT_DARK);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLbl = new JLabel(member.getSdlcCategory() != null ? member.getSdlcCategory() : member.getRole());
        roleLbl.setFont(Theme.FONT_BOLD);
        roleLbl.setForeground(Theme.PRIMARY_BLUE);
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Show Experience Level in small text
        JLabel expLbl = new JLabel(member.getExperienceLevel() != null ? member.getExperienceLevel() : "");
        expLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        expLbl.setForeground(Theme.TEXT_MUTED);
        expLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(avatarPanel);
        center.add(Box.createVerticalStrut(15));
        center.add(nameLbl);
        center.add(Box.createVerticalStrut(5));
        center.add(roleLbl);
        center.add(Box.createVerticalStrut(5));
        center.add(expLbl);

        center.add(expLbl);
        center.add(Box.createVerticalStrut(10));

        // NEW: Performance Bar (Admin/PM Only)
        boolean isAdmin = currentUserRole != null
                && (currentUserRole.toLowerCase().contains("admin")
                        || currentUserRole.equalsIgnoreCase("project manager"))
                || (currentUserName != null && currentUserName.equalsIgnoreCase("Project Manager"));

        if (isAdmin) {
            // Check if this card belongs to "Project Manager"
            boolean isTargetPM = (member.getName() != null && member.getName().equalsIgnoreCase("Project Manager"))
                    || (member.getRole() != null && member.getRole().equalsIgnoreCase("Project Manager"));

            if (!isTargetPM) {
                JProgressBar perfBar = new JProgressBar(0, 100);
                int score = (int) member.getPerformance();
                perfBar.setValue(score);
                perfBar.setStringPainted(true);
                perfBar.setString("Performance: " + score + "%");
                perfBar.setFont(new Font("Segoe UI", Font.BOLD, 10));

                // Color Logic
                if (score > 80)
                    perfBar.setForeground(new Color(40, 167, 69)); // Green
                else if (score > 50)
                    perfBar.setForeground(new Color(255, 193, 7)); // Orange
                else
                    perfBar.setForeground(new Color(220, 53, 69)); // Red

                perfBar.setBackground(new Color(230, 230, 230));
                perfBar.setBorderPainted(false);
                perfBar.setPreferredSize(new Dimension(180, 15));
                perfBar.setAlignmentX(Component.CENTER_ALIGNMENT);

                center.add(perfBar);
            }
        }

        card.add(center, BorderLayout.CENTER);

        // SOUTH: ACTION BUTTONS (Edit/Delete) - Admin/PM Only
        // boolean isAdmin check already done above
        if (isAdmin) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            actions.setOpaque(false);
            actions.setBorder(new EmptyBorder(15, 0, 0, 0));

            // Edit
            JButton editBtn = Theme.createIconButton("✎", new Color(240, 245, 255), Theme.PRIMARY_BLUE);
            editBtn.setToolTipText("Edit Member");
            editBtn.addActionListener(e -> openEditMemberDialog(member));

            // Delete
            JButton delBtn = Theme.createIconButton("🗑", new Color(255, 235, 235), Theme.FG_RED);
            delBtn.setToolTipText("Delete Member");
            delBtn.addActionListener(e -> deleteMember(member.getId(), member.getName()));

            actions.add(editBtn);
            actions.add(delBtn);

            card.add(actions, BorderLayout.SOUTH);
        }
        // } (removed)

        return card;
    }

    private static class CircleAvatar extends JComponent {
        private String letter;
        private Color fg;
        private Color bg;

        public CircleAvatar(String letter, Color fg, Color bg) {
            this.letter = letter;
            this.fg = fg;
            this.bg = bg;
            setPreferredSize(new Dimension(80, 80));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(letter)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(letter, x, y);
        }
    }
}