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
import java.util.stream.Collectors;

public class UserHomePage extends JPanel {

    private String currentUserName;
    private JLabel pendingLbl, inProgressLbl, completedLbl, notifLbl;

    public UserHomePage(String userName) {
        this.currentUserName = userName;
        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);

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

        JLabel title = new JLabel("Welcome back, " + userName + "!");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
        JLabel dateLbl = new JLabel(sdf.format(new Date()));
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dateLbl.setForeground(Theme.TEXT_MUTED);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(dateLbl);

        header.add(titlePanel, BorderLayout.WEST);
        mainContainer.add(header, gbc);

        // ---------------------------------------
        // 2. PERSONAL STATS ROW
        // ---------------------------------------
        gbc.gridy++;
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        statsPanel.setOpaque(false);

        pendingLbl = new JLabel("...");
        inProgressLbl = new JLabel("...");
        completedLbl = new JLabel("...");
        notifLbl = new JLabel("...");

        statsPanel.add(createStatCard("My Pending", pendingLbl, "To-do lists", Theme.FG_ORANGE, "⏳", "To Do"));
        statsPanel.add(
                createStatCard("In Progress", inProgressLbl, "Working on", Theme.PRIMARY_BLUE, "⚡", "In Progress"));
        statsPanel.add(createStatCard("Completed", completedLbl, "Finished jobs", Theme.FG_GREEN, "✅", "Done"));
        statsPanel.add(createStatCard("Notifications", notifLbl, "Alerts", Theme.FG_RED, "🔔", null));

        mainContainer.add(statsPanel, gbc);

        loadUserStats();

        // ---------------------------------------
        // 3. BOTTOM SECTION (Recent Tasks)
        // ---------------------------------------
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JPanel bottomSection = new JPanel(new GridLayout(1, 1)); // Single column full width
        bottomSection.setOpaque(false);

        // RECENT TASKS CARD (Replacing Admin Quick Actions)
        bottomSection.add(createRecentTasksCard());

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

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window win = SwingUtilities.getWindowAncestor(UserHomePage.this);
                if (win instanceof UserDashboard) {
                    if (targetStatus == null) {
                        ((UserDashboard) win).openNotificationsPage();
                    } else {
                        ((UserDashboard) win).openTasksPage(targetStatus);
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

        JPanel strip = new JPanel();
        strip.setBackground(color);
        strip.setPreferredSize(new Dimension(5, 0));
        card.add(strip, BorderLayout.WEST);

        return card;
    }

    // ---------------------------------------
    // COMPONENT: RECENT TASKS CARD
    // ---------------------------------------
    private JPanel createRecentTasksCard() {
        Theme.HoverPanel card = new Theme.HoverPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("My Activity Feed (Recent Tasks)");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setOpaque(false);
        taskListPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Loading...
        JLabel loading = new JLabel("Loading your tasks...");
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        loading.setForeground(Theme.TEXT_MUTED);
        taskListPanel.add(loading);

        card.add(taskListPanel, BorderLayout.CENTER);

        loadRecentTasks(taskListPanel);

        return card;
    }

    private void loadRecentTasks(JPanel listPanel) {
        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        new SwingWorker<List<QueryDocumentSnapshot>, Void>() {
            @Override
            protected List<QueryDocumentSnapshot> doInBackground() throws Exception {
                ApiFuture<QuerySnapshot> future = db.collection("tasks").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();

                // Filter by assignee = currentUserName
                return docs.stream()
                        .filter(d -> {
                            String assignee = d.getString("assignee");
                            return assignee != null && assignee.equalsIgnoreCase(currentUserName);
                        })
                        .limit(5) // Just take 5 for now
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<QueryDocumentSnapshot> tasks = get();
                    listPanel.removeAll();

                    if (tasks.isEmpty()) {
                        listPanel.add(new JLabel("You have no assigned tasks yet."));
                    } else {
                        for (QueryDocumentSnapshot t : tasks) {
                            String name = t.getString("name");
                            String status = t.getString("status");
                            String priority = t.getString("priority");

                            listPanel.add(createTaskRow(name, status, priority));
                            listPanel.add(Box.createVerticalStrut(10));
                        }
                    }
                    listPanel.revalidate();
                    listPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    listPanel.add(new JLabel("Error loading tasks."));
                }
            }
        }.execute();
    }

    private JPanel createTaskRow(String name, String status, String priority) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        row.setMaximumSize(new Dimension(2000, 50));
        row.setPreferredSize(new Dimension(0, 50));

        JLabel nLbl = new JLabel(name);
        nLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nLbl.setForeground(Theme.TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);

        JLabel sLbl = new JLabel(status);
        sLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sLbl.setOpaque(true);
        sLbl.setBackground(getStatusColor(status));
        sLbl.setForeground(Color.WHITE);
        sLbl.setBorder(new EmptyBorder(4, 8, 4, 8)); // Badge

        JLabel pLbl = new JLabel(priority);
        pLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        pLbl.setForeground(Theme.TEXT_MUTED);

        right.add(pLbl);
        right.add(sLbl);

        row.add(nLbl, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private Color getStatusColor(String status) {
        if ("To Do".equalsIgnoreCase(status))
            return Theme.FG_ORANGE;
        if ("In Progress".equalsIgnoreCase(status))
            return Theme.PRIMARY_BLUE;
        if ("Done".equalsIgnoreCase(status))
            return Theme.FG_GREEN;
        return Color.GRAY;
    }

    // ---------------------------------------
    // LOGIC: FETCH PERSONAL STATS
    // ---------------------------------------
    private void loadUserStats() {
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

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date now = new Date();

                for (QueryDocumentSnapshot doc : tasks) {
                    Task t = doc.toObject(Task.class);
                    // Manually inject ID since toObject doesn't get it
                    t.setId(doc.getId());

                    // --- RECALC STATUS (Keep stats accurate) ---
                    try {
                        Date assign = (t.getAssignDate() != null && !t.getAssignDate().isEmpty())
                                ? sdf.parse(t.getAssignDate())
                                : null;
                        Date deadline = (t.getDeadline() != null && !t.getDeadline().isEmpty())
                                ? sdf.parse(t.getDeadline())
                                : null;
                        String oldStatus = t.getStatus();
                        String newStatus = oldStatus;

                        if (assign != null && now.before(assign)) {
                            newStatus = "To Do";
                        } else if (assign != null && deadline != null && now.after(assign) && now.before(deadline)) {
                            if ("To Do".equalsIgnoreCase(oldStatus))
                                newStatus = "In Progress";
                        } else if (deadline != null && now.after(deadline)) {
                            newStatus = "Done";
                        }

                        if (!newStatus.equalsIgnoreCase(oldStatus)) {
                            // Update DB if we found a discrepancy so stats are real
                            t.setStatus(newStatus);
                            db.collection("tasks").document(t.getId()).update("status", newStatus);
                        }
                    } catch (Exception e) {
                    }

                    // Now count
                    String assignee = t.getAssignee();
                    // STRICT FILTER: Only count if assigned to ME
                    if (assignee != null && assignee.equalsIgnoreCase(currentUserName)) {
                        String status = t.getStatus();
                        if (status != null) {
                            if (status.equalsIgnoreCase("To Do"))
                                pending++;
                            else if (status.equalsIgnoreCase("In Progress"))
                                inProgress++;
                            else if (status.equalsIgnoreCase("Done"))
                                completed++;
                        }
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
                    get();
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
