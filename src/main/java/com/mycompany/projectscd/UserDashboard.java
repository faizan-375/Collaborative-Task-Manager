package com.mycompany.projectscd;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserDashboard extends JFrame {

    private JPanel contentArea;
    private NavButton activeButton = null;

    private String userRole;
    private String userName;

    public UserDashboard(String userEmail, String userRole, String userName) {
        this.userRole = userRole;
        this.userName = userName;

        setTitle("CTM User Dashboard - " + userName + " [" + userRole + "]");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize Dashboard Directly
        initDashboard(userEmail);
    }

    private void initDashboard(String userEmail) {
        add(createSidebar(userEmail), BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.LIGHT_BG);

        // Default to Tasks for Users? Or Home?
        // User asked for exact duplicate, so Home.
        switchPage(new UserHomePage(userName));

        add(contentArea, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createSidebar(String email) {

        JPanel side = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                        0, 0, Theme.SIDEBAR_BG_START,
                        0, getHeight(), Theme.SIDEBAR_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        side.setPreferredSize(new Dimension(280, 0));
        side.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(40, 25, 20, 20));

        JLabel logo = new JLabel("⚡ devorbit");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(logo);
        topPanel.add(Box.createVerticalStrut(60));

        // MENU BUTTONS
        topPanel.add(makeButton("Dashboard", () -> switchPage(new UserHomePage(userName))));
        topPanel.add(Box.createVerticalStrut(12));

        topPanel.add(makeButton("Tasks", () -> switchPage(new TaskScreen(userRole, userName))));
        topPanel.add(Box.createVerticalStrut(12));

        topPanel.add(makeButton("Team Members", () -> switchPage(new TeamMembersScreen(userRole, userName))));
        topPanel.add(Box.createVerticalStrut(12));

        topPanel.add(makeButton("Notifications", () -> switchPage(new NotificationsScreen(userRole, userName))));
        topPanel.add(Box.createVerticalStrut(12));

        topPanel.add(makeButton("Settings", () -> switchPage(new SettingsScreen(email, userName))));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        side.add(topPanel, gbc);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 0, 40, 0));

        // Custom Gradient Logout Button
        JButton logout = new JButton(" Sign Out") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient
                Color start = new Color(255, 81, 47); // Shocking Orange-Red
                Color end = new Color(221, 36, 118); // Deep Pink-Red

                if (getModel().isRollover()) {
                    start = start.brighter();
                    end = end.brighter();
                }

                GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Pill shape

                super.paintComponent(g);
            }
        };

        logout.setPreferredSize(new Dimension(200, 45));
        logout.setForeground(Color.WHITE);
        logout.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logout.setText("⏻  Sign Out"); // Unicode Power Symbol
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false); // We paint it
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logout.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        bottom.add(logout);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        side.add(bottom, gbc);

        return side;
    }

    private NavButton makeButton(String text, Runnable action) {

        NavButton btn = new NavButton(text);

        btn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (activeButton != null) {
                    activeButton.setActive(false);
                }

                activeButton = btn;
                activeButton.setActive(true);

                action.run();
            }
        });

        return btn;
    }

    public void switchPage(JPanel page) {
        contentArea.removeAll();
        contentArea.add(page, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    public void openTasksPage(String statusFilter) {
        // Switch to Tasks Screen with specific filter
        switchPage(new TaskScreen(userRole, userName, statusFilter));
    }

    public void openNotificationsPage() {
        switchPage(new NotificationsScreen(userRole, userName));
    }
}
