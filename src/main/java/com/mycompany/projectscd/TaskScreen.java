package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskScreen extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private List<Task> taskList = new ArrayList<>();
    private final long MAX_FILE_SIZE = 1024 * 1024;

    private String userRole;
    private String userName;
    private JComboBox<String> filterTaskCombo;
    private JComboBox<String> filterStatusCombo;
    private String currentTaskFilter = "My Tasks";
    private String currentStatusFilter = "All Status";

    public TaskScreen(String role, String name) {
        this(role, name, null);
    }

    public TaskScreen(String role, String name, String initialStatus) {
        this.userRole = (role != null) ? role.toLowerCase() : "member";
        this.userName = name;

        // Apply Initial Filter if provided
        if (initialStatus != null && !initialStatus.isEmpty()) {
            this.currentStatusFilter = initialStatus;
        }

        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        FirebaseManager.initialize();

        // 1. HEADER
        initHeader();

        // 2. TABLE
        initTable();
    }

    private void initHeader() {
        // CONTENT
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("📝 Tasks");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);

        // Filter Task (Ownership)
        filterTaskCombo = new JComboBox<>(new String[] { "My Tasks", "All Tasks" });
        filterTaskCombo.setPreferredSize(new Dimension(150, 40));
        filterTaskCombo.setBackground(Color.WHITE);
        filterTaskCombo.setFocusable(false);
        filterTaskCombo.addActionListener(e -> {
            currentTaskFilter = (String) filterTaskCombo.getSelectedItem();
            loadTasksFromFirebase();
        });

        // Filter Status
        filterStatusCombo = new JComboBox<>(new String[] { "All Status", "To Do", "In Progress", "Done" });
        filterStatusCombo.setPreferredSize(new Dimension(150, 40));
        filterStatusCombo.setBackground(Color.WHITE);
        filterStatusCombo.setFocusable(false);
        // Set Initial Selection from Constructor
        if (!"All Status".equals(currentStatusFilter)) {
            filterStatusCombo.setSelectedItem(currentStatusFilter);
        }
        filterStatusCombo.addActionListener(e -> {
            currentStatusFilter = (String) filterStatusCombo.getSelectedItem();
            loadTasksFromFirebase();
        });

        controls.add(new JLabel("View:"));
        controls.add(filterTaskCombo);
        controls.add(filterStatusCombo);

        // Add Button
        JButton addBtn = Theme.createPrimaryButton("+  Assign New Task");
        addBtn.setPreferredSize(new Dimension(180, 40));
        addBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            new AddTaskDialog(win).setVisible(true);
            loadTasksFromFirebase(); // Reload after dialog closes
        });

        System.out.println("DEBUG: TaskScreen init - Role: " + userRole + ", Name: " + userName);
        boolean isAdmin = userRole.contains("admin") || userRole.equalsIgnoreCase("project manager")
                || (userName != null && userName.equalsIgnoreCase("Project Manager"));
        System.out.println("DEBUG: isAdmin calculated as: " + isAdmin);
        if (isAdmin) {
            currentTaskFilter = "All Tasks";
            filterTaskCombo.setSelectedItem("All Tasks");
            controls.add(addBtn);
        }

        content.add(title, BorderLayout.WEST);
        content.add(controls, BorderLayout.EAST);
        add(content, BorderLayout.NORTH);
    }

    private void initTable() {
        boolean isAdmin = userRole.contains("admin") || userRole.equalsIgnoreCase("project manager")
                || (userName != null && userName.equalsIgnoreCase("Project Manager"));

        String[] cols;
        if (isAdmin) {
            cols = new String[] { "Task Name", "Priority", "Status", "Assigned To", "Assign Date", "Deadline",
                    "Instructions", "User Submission", "" };
        } else {
            cols = new String[] { "Task Name", "Priority", "Status", "Assigned To", "Assign Date", "Deadline",
                    "Instructions", "User Submission" };
        }

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 6=Instructions, 7=Submission, 8=Actions(only if admin)
                if (column == 6 || column == 7)
                    return true;
                if (isAdmin && column == 8)
                    return true;
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(55);
        table.setFont(Theme.FONT_REGULAR);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));

        // Column Config
        // Column Config
        // Task Name (0)
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        // Priority (1)
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        // Status (2)
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        // Assignee (3)
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        // Dates (4, 5)
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        // BADGE RENDERERS (Vibrant Pills)
        table.getColumnModel().getColumn(1).setCellRenderer(new BadgeRenderer()); // Priority
        table.getColumnModel().getColumn(2).setCellRenderer(new BadgeRenderer()); // Status

        table.getColumnModel().getColumn(6).setCellRenderer(new FileButtonRenderer(false));
        table.getColumnModel().getColumn(6).setCellEditor(new FileButtonEditor(false));
        table.getColumnModel().getColumn(6).setPreferredWidth(130);

        table.getColumnModel().getColumn(7).setCellRenderer(new FileButtonRenderer(true));
        table.getColumnModel().getColumn(7).setCellEditor(new FileButtonEditor(true));
        table.getColumnModel().getColumn(7).setPreferredWidth(130);

        if (isAdmin) {
            table.getColumnModel().getColumn(8).setCellRenderer(new ActionMenuRenderer());
            table.getColumnModel().getColumn(8).setCellEditor(new ActionMenuEditor());
            table.getColumnModel().getColumn(8).setMaxWidth(40);
            table.getColumnModel().getColumn(8).setMinWidth(40);
        }

        setupHeaderRenderer();
        // setupRowRenderer(); // REMOVED: No more full row tints

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(null);

        Theme.RoundedPanel card = new Theme.RoundedPanel(25, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.add(scroll);

        add(card, BorderLayout.CENTER);

        // Load Data
        loadTasksFromFirebase();
    }

    public void loadTasksFromFirebase() {
        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        new SwingWorker<List<Task>, Void>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                ApiFuture<QuerySnapshot> future = db.collection("tasks").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                List<Task> loaded = new ArrayList<>();

                for (QueryDocumentSnapshot doc : docs) {
                    Task t = doc.toObject(Task.class);
                    t.setId(doc.getId());

                    // --- AUTOMATIC LIFECYCLE CHECK ---
                    boolean changed = recalculateStatus(t);
                    if (changed) {
                        db.collection("tasks").document(t.getId()).set(t); // Update DB immediately
                    }

                    boolean show = true;

                    // 1. Ownership Filter
                    if ("My Tasks".equals(currentTaskFilter)) {
                        if (t.getAssignee() == null || !t.getAssignee().equalsIgnoreCase(userName)) {
                            show = false;
                        }
                    }

                    // 2. Status Filter
                    if (show && !"All Status".equals(currentStatusFilter)) {
                        String s = t.getStatus();
                        // Case-insensitive check
                        if (s == null || !s.equalsIgnoreCase(currentStatusFilter)) {
                            show = false;
                        }
                    }

                    if (show)
                        loaded.add(t);
                }
                return loaded;
            }

            @Override
            protected void done() {
                try {
                    taskList = get();
                    model.setRowCount(0);
                    for (Task t : taskList) {
                        boolean isAdmin = userRole.contains("admin") || userRole.equalsIgnoreCase("project manager")
                                || (userName != null && userName.equalsIgnoreCase("Project Manager"));

                        Object[] rowData;
                        if (isAdmin) {
                            rowData = new Object[] {
                                    t.getName(), t.getPriority(), t.getStatus(), t.getAssignee(),
                                    t.getAssignDate(), t.getDeadline(),
                                    t.getInstructionFileName(), t.getSubmissionFileName(), ""
                            };
                        } else {
                            rowData = new Object[] {
                                    t.getName(), t.getPriority(), t.getStatus(), t.getAssignee(),
                                    t.getAssignDate(), t.getDeadline(),
                                    t.getInstructionFileName(), t.getSubmissionFileName()
                            };
                        }
                        model.addRow(rowData);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // --- RECALCULATE STATUS LOGIC ---
    private boolean recalculateStatus(Task t) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date now = new Date(); // Server time (simulated via client for now)
            Date assign = (t.getAssignDate() != null && !t.getAssignDate().isEmpty()) ? sdf.parse(t.getAssignDate())
                    : null;
            Date deadline = (t.getDeadline() != null && !t.getDeadline().isEmpty()) ? sdf.parse(t.getDeadline()) : null;

            String oldStatus = t.getStatus();
            String newStatus = oldStatus;

            if (assign != null && now.before(assign)) {
                newStatus = "To Do";
            } else if (assign != null && deadline != null && now.after(assign) && now.before(deadline)) {
                // Only force "In Progress" if currently "To Do".
                // If user marked it "Done" early, maybe we shouldn't revert?
                // USER REQUEST: "Status automatically changes to In Progress"
                // Implication: If it's valid time, it IS In Progress (unless done?).
                // Let's stick to strict rules: IF valid window AND not done -> In Progress?
                // Or strict: Valid window == In Progress.
                // But what if they finish early?
                // Req says: "If current server time >= assign_datetime AND < deadline_datetime:
                // Status automatically changes to In Progress"
                // This implies it Forces it. But if I finish early, it reverts?
                // I will assume if it's "Done", we leave it "Done" unless it was premature?
                // Actually, "Task becomes read-only" only after deadline.
                // Let's implement strict transition from To Do -> In Progress.
                // If already Done, we might leave it. But simple logic is robust.
                if ("To Do".equalsIgnoreCase(oldStatus)) {
                    newStatus = "In Progress";
                }
            } else if (deadline != null && now.after(deadline)) {
                newStatus = "Done";
            }

            if (!newStatus.equalsIgnoreCase(oldStatus)) {
                t.setStatus(newStatus);
                return true;
            }
        } catch (Exception e) {
            // checking failed, maybe bad date format
        }
        return false;
    }

    // =================================================================
    // RENDERERS & EDITORS
    // =================================================================

    class FileButtonRenderer extends JPanel implements TableCellRenderer {
        JButton btn = new JButton();
        boolean isSubmission;

        public FileButtonRenderer(boolean isSubmission) {
            this.isSubmission = isSubmission;
            setLayout(new FlowLayout(FlowLayout.CENTER));
            setOpaque(true);
            setBackground(Color.WHITE);
            btn.setPreferredSize(new Dimension(140, 35));
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            add(btn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            String fileName = (String) value;
            setBackground(isSelected ? new Color(200, 220, 255) : Color.WHITE);

            // Logic for Submission Column
            if (isSubmission && (fileName == null || fileName.isEmpty())) {
                Task t = taskList.get(row);
                // IF User is Assignee -> Show Upload Button
                if (t.getAssignee() != null && t.getAssignee().equalsIgnoreCase(userName)) {
                    btn.setText("📤 Upload Work");
                    btn.setEnabled(true);
                    btn.setBackground(new Color(235, 255, 235));
                    btn.setForeground(new Color(30, 120, 60));
                    btn.setBorder(BorderFactory.createLineBorder(new Color(30, 120, 60)));
                } else {
                    btn.setText("Pending");
                    btn.setEnabled(false);
                    btn.setBackground(new Color(245, 245, 245));
                    btn.setForeground(Color.GRAY);
                    btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                }
            }
            // Standard File View Logic
            else if (fileName != null && !fileName.isEmpty()) {
                // --- VISIBILITY CHECK ---
                // If this is INSTRUCTION file (not submission), and task hasn't started...
                if (!isSubmission) {
                    Task t = taskList.get(row);
                    boolean isAdmin = userRole.contains("admin") || userRole.equalsIgnoreCase("project manager")
                            || (userName != null && userName.equalsIgnoreCase("Project Manager"));

                    // If NOT admin, check start time
                    if (!isAdmin) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            Date now = new Date();
                            Date assign = (t.getAssignDate() != null && !t.getAssignDate().isEmpty())
                                    ? sdf.parse(t.getAssignDate())
                                    : null;

                            if (assign != null && now.before(assign)) {
                                // HIDE FILE
                                btn.setText("\uD83D\uDD12 Locked"); // Lock Icon
                                btn.setEnabled(false);
                                btn.setBackground(new Color(245, 245, 245));
                                btn.setForeground(Color.GRAY);
                                btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                                return this;
                            }
                        } catch (Exception e) {
                        }
                    }
                }

                btn.setText("📄 " + (fileName.length() > 12 ? fileName.substring(0, 10) + "..." : fileName));
                btn.setEnabled(true);
                btn.setBackground(isSubmission ? new Color(255, 248, 220) : new Color(235, 245, 255));
                btn.setForeground(Color.BLACK);
                btn.setBorder(
                        BorderFactory.createLineBorder(isSubmission ? new Color(255, 200, 100) : Theme.PRIMARY_BLUE));
            } else {
                // Instructions Empty
                btn.setText("No Instructions");
                btn.setEnabled(false);
                btn.setBackground(new Color(245, 245, 245));
                btn.setForeground(Color.GRAY);
                btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
            return this;
        }
    }

    class FileButtonEditor extends AbstractCellEditor implements TableCellEditor {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btn = new JButton();
        Task currentTask;
        boolean isSubmission;

        public FileButtonEditor(boolean isSubmission) {
            this.isSubmission = isSubmission;
            this.btn.setPreferredSize(new Dimension(140, 35));
            this.panel.add(btn);

            btn.addActionListener(e -> {
                fireEditingStopped();
                handleFileAction();
            });
        }

        private void handleFileAction() {
            if (currentTask == null)
                return;

            // CHECK: Is this an Upload Action?
            // If Submission col, and empty, and user is assignee -> UPLOAD
            boolean isUpload = isSubmission
                    && (currentTask.getSubmissionFileName() == null || currentTask.getSubmissionFileName().isEmpty())
                    && currentTask.getAssignee() != null
                    && currentTask.getAssignee().equalsIgnoreCase(userName);

            if (isUpload) {
                // Check Lifecycle Restrictions
                String status = currentTask.getStatus();
                if ("To Do".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(panel, "Task has not started yet. Please wait for Assign Date.");
                    return;
                }
                if ("Done".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(panel, "Task deadline passed or completed. Uploads disabled.");
                    return;
                }

                performUpload();
            } else {
                openFile();
            }
        }

        private void performUpload() {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (f.length() > MAX_FILE_SIZE) {
                    JOptionPane.showMessageDialog(panel, "File too large! Max 1MB.");
                    return;
                }
                try {
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    String b64 = Base64.getEncoder().encodeToString(bytes);

                    // Update Task in DB
                    currentTask.setSubmissionFileName(f.getName());
                    currentTask.setSubmissionFile(b64);
                    // Optionally set status to 'Done' or 'In Progress' if desired, but user didn't
                    // ask.

                    Firestore db = FirebaseManager.getFirestore();
                    if (db != null && currentTask.getId() != null) {
                        db.collection("tasks").document(currentTask.getId()).set(currentTask);
                        JOptionPane.showMessageDialog(panel, "Work Uploaded Successfully!");
                        loadTasksFromFirebase(); // Reload
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void openFile() {
            String b64 = isSubmission ? currentTask.getSubmissionFile() : currentTask.getInstructionFile();
            String name = isSubmission ? currentTask.getSubmissionFileName() : currentTask.getInstructionFileName();

            if (b64 == null || b64.isEmpty())
                return;

            try {
                byte[] fileBytes = Base64.getDecoder().decode(b64);
                String suffix = name.contains(".") ? name.substring(name.lastIndexOf(".")) : ".tmp";
                String prefix = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
                if (prefix.length() < 3)
                    prefix = "doc";

                File tempFile = File.createTempFile(prefix + "_", suffix);
                Files.write(tempFile.toPath(), fileBytes);
                Desktop.getDesktop().open(tempFile);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error opening file: " + ex.getMessage());
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            if (row < taskList.size())
                currentTask = taskList.get(row);

            String fileName = (String) value;

            // Set Button Text dynamically just like Renderer
            if (isSubmission && (fileName == null || fileName.isEmpty())) {
                if (currentTask.getAssignee() != null && currentTask.getAssignee().equalsIgnoreCase(userName)) {
                    btn.setText("📤 Upload Work");
                    btn.setEnabled(true);
                } else {
                    btn.setText("Pending");
                    btn.setEnabled(false);
                }
            } else if (fileName != null && !fileName.isEmpty()) {
                btn.setText("📄 Open File");
                btn.setEnabled(true);
            } else {
                btn.setText("No File");
                btn.setEnabled(false);
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    class ThreeDotButton extends JButton {
        public ThreeDotButton() {
            setContentAreaFilled(false);
            setBorder(null);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            int d = 5;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - (d * 3 + 4)) / 2;
            g2.fillOval(x, y, d, d);
            g2.fillOval(x, y + d + 2, d, d);
            g2.fillOval(x, y + (d + 2) * 2, d, d);
            g2.dispose();
        }
    }

    class ActionMenuRenderer extends JPanel implements TableCellRenderer {
        ThreeDotButton btn = new ThreeDotButton();

        public ActionMenuRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(Color.WHITE);
            add(btn, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? new Color(200, 220, 255) : Color.WHITE);
            return this;
        }
    }

    class ActionMenuEditor extends AbstractCellEditor implements TableCellEditor {
        JPanel panel = new JPanel(new BorderLayout());
        ThreeDotButton btn = new ThreeDotButton();
        Task currentTask;

        public ActionMenuEditor() {
            panel.setBackground(Color.WHITE);
            btn.addActionListener(e -> {
                // Role Check: Admin / PM ONLY
                boolean isAdmin = userRole.contains("admin") || userRole.equalsIgnoreCase("project manager")
                        || (userName != null && userName.equalsIgnoreCase("Project Manager"));
                if (!isAdmin) {
                    return; // Do not show menu if not admin or project manager
                }

                JPopupMenu menu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Edit Task");
                JMenuItem deleteItem = new JMenuItem("Delete Task");
                deleteItem.setForeground(Color.RED);

                editItem.addActionListener(ev -> {
                    fireEditingStopped();
                    Window win = SwingUtilities.getWindowAncestor(panel);
                    new AddTaskDialog(win, currentTask).setVisible(true);
                    loadTasksFromFirebase();
                });

                deleteItem.addActionListener(ev -> {
                    fireEditingStopped();
                    deleteTask(currentTask);
                });

                menu.add(editItem);
                menu.add(deleteItem);
                menu.show(btn, 0, btn.getHeight());
            });
            panel.add(btn, BorderLayout.CENTER);
        }

        private void deleteTask(Task t) {
            int confirm = JOptionPane.showConfirmDialog(panel, "Delete '" + t.getName() + "'?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Firestore db = FirebaseManager.getFirestore();
                if (db != null) {
                    db.collection("tasks").document(t.getId()).delete();
                    loadTasksFromFirebase();
                }
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object val, boolean sel, int row, int col) {
            if (row < taskList.size())
                currentTask = taskList.get(row);
            panel.setBackground(sel ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void setupHeaderRenderer() {
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                l.setBackground(Theme.PRIMARY_BLUE);
                l.setForeground(Color.WHITE);
                l.setFont(Theme.FONT_BOLD);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                return l;
            }
        });
    }

    // ---------------------------------------------------------
    // RENDERER: VIBRANT BADGE (PILL SHAPE)
    // ---------------------------------------------------------
    class BadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String text = (String) value;
            c.setText(text);
            c.setHorizontalAlignment(CENTER);
            c.setBorder(new EmptyBorder(5, 10, 5, 10)); // Internal padding

            if (isSelected) {
                c.setForeground(Color.WHITE);
                c.setBackground(table.getSelectionBackground());
                return c;
            }

            // Define Badge Colors based on Text
            Color bg = Color.WHITE;
            Color fg = Color.BLACK;

            if (text != null) {
                String t = text.toUpperCase();
                if (t.contains("HIGH")) {
                    bg = Theme.BG_RED;
                    fg = Theme.FG_RED;
                } else if (t.contains("MEDIUM")) {
                    bg = Theme.BG_ORANGE;
                    fg = Theme.FG_ORANGE;
                } else if (t.contains("LOW")) {
                    bg = Theme.BG_BLUE;
                    fg = Theme.FG_BLUE;
                } else if (t.contains("DONE")) {
                    bg = Theme.BG_GREEN;
                    fg = Theme.FG_GREEN;
                } else if (t.contains("IN PROG")) {
                    bg = Theme.BG_PURPLE;
                    fg = Theme.FG_PURPLE;
                } else if (t.contains("TO DO") || t.contains("PENDING")) {
                    bg = Theme.BG_ORANGE;
                    fg = Theme.FG_ORANGE;
                }
            }

            // We need a custom painting component to draw the pill
            // DefaultTableCellRenderer just fills rect.
            // So we return a specialized label that paints logic.

            BadgeLabel label = new BadgeLabel(bg, fg, text);
            if (isSelected) {
                label.bg = table.getSelectionBackground();
                label.fg = Color.WHITE;
            }
            return label;
        }
    }

    class BadgeLabel extends JLabel {
        Color bg, fg;

        public BadgeLabel(Color bg, Color fg, String text) {
            super(text, CENTER);
            this.bg = bg;
            this.fg = fg;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(fg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Pill
            int arc = 20; // Rounded corner radius
            int padX = 8; // Horizontal padding for the pill
            int padY = 10; // Vertical padding for the pill (to center it)

            g2.setColor(bg);
            g2.fillRoundRect(padX, padY, getWidth() - (padX * 2), getHeight() - (padY * 2), arc, arc);

            super.paintComponent(g);
        }
    }
}