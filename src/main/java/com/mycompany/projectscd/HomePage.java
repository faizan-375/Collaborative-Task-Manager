package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomePage extends JPanel {

    // Status Colors (mapped to Theme)
    // Live Labels
    private JLabel pendingLbl, inProgressLbl, completedLbl, notifLbl;

    public HomePage() {
        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        // Add scroll pane to handle smaller screens

        // Init DB
        FirebaseManager.initialize();

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new GridBagLayout());
        mainContainer.setBackground(Theme.LIGHT_BG);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // ---------------------------------------
        // 1. HEADER SECTION
        // ---------------------------------------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Greeting
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
        JLabel dateLbl = new JLabel(sdf.format(new Date()));
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dateLbl.setForeground(Theme.TEXT_MUTED);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(dateLbl);

        header.add(titlePanel, BorderLayout.WEST);

        // Add Header
        mainContainer.add(header, gbc);

        // ---------------------------------------
        // 2. STATS ROW
        // ---------------------------------------
        gbc.gridy++;
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        statsPanel.setOpaque(false);

        pendingLbl = new JLabel("...");
        inProgressLbl = new JLabel("...");
        completedLbl = new JLabel("...");
        notifLbl = new JLabel("...");

        statsPanel.add(createStatCard("Pending", pendingLbl, "Tasks to do", Theme.FG_ORANGE, "⏳", "To Do"));
        statsPanel.add(
                createStatCard("In Progress", inProgressLbl, "Happening now", Theme.PRIMARY_BLUE, "⚡", "In Progress"));
        statsPanel.add(createStatCard("Completed", completedLbl, "Finished tasks", Theme.FG_GREEN, "✅", "Done"));
        statsPanel.add(createStatCard("Notifications", notifLbl, "New alerts", Theme.FG_RED, "🔔", null));

        mainContainer.add(statsPanel, gbc);

        // Load Real Data
        loadDashboardStats();

        // ---------------------------------------
        // 3. BOTTOM SECTION (Progress + Quick Actions)
        // ---------------------------------------
        gbc.gridy++;
        gbc.weighty = 1.0; // Fill remaining vertical
        gbc.fill = GridBagConstraints.BOTH;

        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 25, 0));
        bottomSection.setOpaque(false);

        // B1. PROJECT STATUS CARD
        JPanel progressCard = createProjectStatusCard();
        bottomSection.add(progressCard);

        // Load Real Progress
        loadProjectProgress((JPanel) progressCard.getComponent(1));

        // B2. QUICK ACTIONS CARD
        bottomSection.add(createQuickActionsCard());

        mainContainer.add(bottomSection, gbc);

        // Wrap in ScrollPane
        JScrollPane scroll = new JScrollPane(mainContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ---------------------------------------
    // COMPONENT: STATS CARD
    // ---------------------------------------
    private JPanel createStatCard(String title, JLabel vLbl, String subtext, Color color, String icon,
            String targetStatus) {
        Theme.HoverPanel card = new Theme.HoverPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add Click Action
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window win = SwingUtilities.getWindowAncestor(HomePage.this);
                if (win instanceof AdminDashboard) {
                    if (targetStatus == null) {
                        ((AdminDashboard) win).openNotificationsPage();
                    } else {
                        ((AdminDashboard) win).openTasksPage(targetStatus);
                    }
                }
            }
        });

        // Icon Top Right
        JLabel iLbl = new JLabel(icon);
        iLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iLbl.setBorder(new EmptyBorder(5, 5, 0, 0)); // Padding to fix cutoff

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(iLbl, BorderLayout.EAST);

        // Value Center
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 42));
        vLbl.setForeground(Theme.TEXT_DARK);

        // Title Bottom
        JLabel tLbl = new JLabel(title);
        tLbl.setFont(Theme.FONT_BOLD);
        tLbl.setForeground(Theme.TEXT_MUTED);

        // Subtext
        JLabel sLbl = new JLabel(subtext);
        sLbl.setFont(Theme.FONT_SMALL);
        sLbl.setForeground(color);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.setOpaque(false);
        bottom.add(tLbl);
        bottom.add(sLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(vLbl, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        // Colored side strip
        JPanel strip = new JPanel();
        strip.setBackground(color);
        strip.setPreferredSize(new Dimension(5, 0));
        card.add(strip, BorderLayout.WEST);

        return card;
    }

    // ---------------------------------------
    // COMPONENT: PROJECT STATUS CARD
    // ---------------------------------------
    private JPanel createProjectStatusCard() {
        Theme.HoverPanel card = new Theme.HoverPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel title = new JLabel("Project Progress");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        // Progress Bars Panel (Dynamic Content)
        JPanel bars = new JPanel();
        bars.setLayout(new BoxLayout(bars, BoxLayout.Y_AXIS));
        bars.setOpaque(false);
        bars.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Initial Loading State
        JLabel loading = new JLabel("Loading progress...");
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        loading.setForeground(Theme.TEXT_MUTED);
        bars.add(loading);

        card.add(bars, BorderLayout.CENTER);
        return card;
    }

    private void loadProjectProgress(JPanel barsPanel) {
        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        new SwingWorker<Void, Void>() {
            class CatProgress {
                String name;
                int total = 0;
                double done = 0;
                String color;
            }

            java.util.List<CatProgress> progressList = new java.util.ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                // Fixed SDLC Categories
                String[] sdlcPhases = { "Planning", "Analysis", "Design", "Implementation", "Testing", "Maintenance" };
                String[] phaseColors = { "Gray", "Blue", "Purple", "Green", "Orange", "Red" }; // Mapped colors

                // Fetch Tasks
                ApiFuture<QuerySnapshot> taskFuture = db.collection("tasks").get();
                java.util.List<QueryDocumentSnapshot> taskDocs = taskFuture.get().getDocuments();

                for (int i = 0; i < sdlcPhases.length; i++) {
                    CatProgress cp = new CatProgress();
                    cp.name = sdlcPhases[i];
                    cp.color = phaseColors[i];

                    // Calc Progress
                    for (QueryDocumentSnapshot t : taskDocs) {
                        String tCat = t.getString("category");
                        String tStatus = t.getString("status");

                        if (tCat != null && tCat.equalsIgnoreCase(cp.name)) {
                            cp.total++;
                            if (tStatus != null) {
                                if ("Done".equalsIgnoreCase(tStatus)) {
                                    cp.done += 1.0;
                                } else if ("In Progress".equalsIgnoreCase(tStatus)) {
                                    cp.done += 0.75;
                                } else if ("To Do".equalsIgnoreCase(tStatus)) {
                                    cp.done += 0.5;
                                } else if ("Pending".equalsIgnoreCase(tStatus)) {
                                    cp.done += 0.25;
                                }
                            }
                        }
                    }
                    progressList.add(cp);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for errors
                    barsPanel.removeAll();

                    if (progressList.isEmpty()) {
                        barsPanel.add(new JLabel("No categories found."));
                    } else {
                        for (CatProgress cp : progressList) {
                            int percent = (cp.total == 0) ? 0 : (int) ((cp.done / (double) cp.total) * 100);
                            Color c = Theme.PRIMARY_BLUE;
                            if ("Gray".equalsIgnoreCase(cp.color))
                                c = Color.GRAY;
                            if ("Blue".equalsIgnoreCase(cp.color))
                                c = Theme.PRIMARY_BLUE;
                            if ("Purple".equalsIgnoreCase(cp.color))
                                c = Theme.FG_PURPLE;
                            if ("Green".equalsIgnoreCase(cp.color))
                                c = Theme.FG_GREEN;
                            if ("Orange".equalsIgnoreCase(cp.color))
                                c = Theme.FG_ORANGE;
                            if ("Red".equalsIgnoreCase(cp.color))
                                c = Theme.FG_RED;

                            barsPanel.add(createProgressBar(cp.name, (int) percent, c));
                            barsPanel.add(Box.createVerticalStrut(15));
                        }
                    }
                    barsPanel.revalidate();
                    barsPanel.repaint();

                } catch (Exception e) {
                    e.printStackTrace();
                    barsPanel.add(new JLabel("Error loading progress."));
                }
            }
        }.execute();
    }

    private JPanel createProgressBar(String label, int value, Color c) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);

        // Label + Percent
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_DARK);

        JLabel v = new JLabel(value + "%");
        v.setFont(Theme.FONT_BOLD);
        v.setForeground(c);

        header.add(l, BorderLayout.WEST);
        header.add(v, BorderLayout.EAST);

        // Bar
        GlowingProgressBar bar = new GlowingProgressBar(c);
        bar.setValue(value);

        p.add(header, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------
    // COMPONENT: QUICK ACTIONS CARD
    // ---------------------------------------
    private JPanel createQuickActionsCard() {
        Theme.HoverPanel card = new Theme.HoverPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        // Buttons Grid
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        grid.add(createActionButton("New Task", "📝", new Color(235, 242, 255), Theme.PRIMARY_BLUE), gbc);

        gbc.gridx = 1;
        grid.add(createActionButton("Add Member", "👤", new Color(235, 250, 240), Theme.FG_GREEN), gbc);

        // Row 2 - Landscape Reports Button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Span 2 columns
        JButton reportsBtn = createActionButton("View Analytic Reports", "📊", new Color(255, 248, 235),
                Theme.FG_ORANGE);
        grid.add(reportsBtn, gbc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JButton createActionButton(String text, String icon, Color bg, Color fg) {
        JButton btn = new JButton(
                "<html><center><span style='font-size:20px'>" + icon + "</span><br><span style='font-size:12px'>" + text
                        + "</span></center></html>");
        btn.setFont(Theme.FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(new LineBorder(bg.darker(), 0)); // No border
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        // Actions
        btn.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            if (text.contains("New Task")) {
                new AddTaskDialog(parent).setVisible(true);
            } else if (text.contains("Add Member")) {
                new AddMemberDialog(parent).setVisible(true);
            } else if (text.contains("Reports")) {
                if (parent instanceof AdminDashboard) {
                    ((AdminDashboard) parent).switchPage(new ReportsPage());
                }
            }
        });

        return btn;
    }

    // ---------------------------------------
    // LOGIC: FETCH REAL STATS
    // ---------------------------------------
    private void loadDashboardStats() {
        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        new SwingWorker<Void, Void>() {
            int pending = 0, inProgress = 0, completed = 0, notifCount = 0;

            @Override
            protected Void doInBackground() throws Exception {
                // 1. Get Tasks
                ApiFuture<QuerySnapshot> tasksFuture = db.collection("tasks").get();
                List<QueryDocumentSnapshot> tasks = tasksFuture.get().getDocuments();

                for (QueryDocumentSnapshot t : tasks) {
                    String status = t.getString("status");
                    if (status != null) {
                        if (status.equalsIgnoreCase("To Do"))
                            pending++;
                        else if (status.equalsIgnoreCase("In Progress"))
                            inProgress++;
                        else if (status.equalsIgnoreCase("Done"))
                            completed++;
                    }
                }

                // 2. Get Notifications
                ApiFuture<QuerySnapshot> notifFuture = db.collection("notifications").get();
                notifCount = notifFuture.get().size();

                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Ensure no exceptions were thrown
                    pendingLbl.setText(String.valueOf(pending));
                    inProgressLbl.setText(String.valueOf(inProgress));
                    completedLbl.setText(String.valueOf(completed));
                    notifLbl.setText(String.valueOf(notifCount));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}