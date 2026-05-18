package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class AddTaskDialog extends JDialog {

    private final long MAX_FILE_SIZE = 1024 * 1024; // 1MB

    private JComboBox<String> cbSdlc;
    private JComboBox<String> cbAssignee;
    private JTextField txtTitle;
    private JComboBox<String> cbPriority;
    // private JComboBox<String> cbStatus; // REMOVED per user request
    private JTextField txtAssignDate; // NEW
    private JTextField txtDeadline;

    // File Data
    private String instructionFileName = "";
    private String instructionFileBase64 = "";
    private JLabel fileLbl;

    private Task taskToEdit;
    private boolean isEditMode;

    public AddTaskDialog(Window owner) {
        this(owner, null);
    }

    public AddTaskDialog(Window owner, Task task) {
        super(owner, task == null ? "Assign New Task" : "Edit Task", ModalityType.APPLICATION_MODAL);
        this.taskToEdit = task;
        this.isEditMode = (task != null);

        setSize(450, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(25, 25, 25, 25));

        // UTILS
        Border fieldBorder = new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(8, 10, 8, 10));

        // 1. SDLC FILTER (Acts as Category)
        container.add(createLabel("Select Phase (Category):"));
        cbSdlc = new JComboBox<>(
                new String[] { "Planning", "Analysis", "Design", "Implementation", "Testing", "Maintenance" });
        cbSdlc.setBackground(Color.WHITE);
        cbSdlc.addActionListener(e -> loadMembers((String) cbSdlc.getSelectedItem()));
        container.add(cbSdlc);
        container.add(Box.createVerticalStrut(15));

        // 2. ASSIGNEE
        container.add(createLabel("Assign To:"));
        cbAssignee = new JComboBox<>();
        cbAssignee.setBackground(Color.WHITE);
        container.add(cbAssignee);
        container.add(Box.createVerticalStrut(15));

        // Initial Load
        loadMembers("Planning");

        // 3. TASK NAME
        container.add(createLabel("Task Name:"));
        txtTitle = new JTextField(isEditMode ? taskToEdit.getName() : "");
        txtTitle.setBorder(fieldBorder);
        container.add(txtTitle);
        container.add(Box.createVerticalStrut(15));

        // 4. PRIORITY & STATUS
        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setBackground(Color.WHITE);

        JPanel pPrio = new JPanel(new BorderLayout());
        pPrio.setBackground(Color.WHITE);
        pPrio.add(createLabel("Priority:"), BorderLayout.NORTH);
        cbPriority = new JComboBox<>(new String[] { "High", "Medium", "Low" });
        cbPriority.setBackground(Color.WHITE);
        // Default Logic
        if (isEditMode)
            cbPriority.setSelectedItem(taskToEdit.getPriority());
        pPrio.add(cbPriority, BorderLayout.CENTER);

        if (isEditMode)
            cbPriority.setSelectedItem(taskToEdit.getPriority());
        pPrio.add(cbPriority, BorderLayout.CENTER);

        // REMOVED STATUS SELECTION - Auto Lifecycle
        // JPanel pStat = new JPanel(new BorderLayout());...

        row.add(pPrio);
        // row.add(pStat); // Only Priority in this row now
        container.add(row);
        container.add(Box.createVerticalStrut(15));

        // 5. DATES (Assign Date & Deadline)
        container.add(createLabel("Assign Date (yyyy-MM-dd HH:mm):"));
        String assignVal = isEditMode && taskToEdit.getAssignDate() != null ? taskToEdit.getAssignDate()
                : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        txtAssignDate = new JTextField(assignVal);
        txtAssignDate.setBorder(fieldBorder);
        container.add(txtAssignDate);
        container.add(Box.createVerticalStrut(10));

        container.add(createLabel("Deadline (yyyy-MM-dd HH:mm):"));
        String dateVal = isEditMode ? taskToEdit.getDeadline()
                : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(System.currentTimeMillis() + 86400000));
        txtDeadline = new JTextField(dateVal);
        txtDeadline.setBorder(fieldBorder);
        container.add(txtDeadline);
        container.add(Box.createVerticalStrut(15));

        // 6. FILE ATTACHMENT
        container.add(createLabel("Instruction File (Optional):"));

        // Pre-fill file data if editing
        if (isEditMode && taskToEdit.getInstructionFileName() != null) {
            instructionFileName = taskToEdit.getInstructionFileName();
            instructionFileBase64 = taskToEdit.getInstructionFile();
        }

        JButton uploadBtn = new JButton("Attach File (Max 1MB)");
        uploadBtn.setBackground(new Color(240, 245, 255));
        uploadBtn.setForeground(Theme.PRIMARY_BLUE);
        uploadBtn.setFocusPainted(false);

        fileLbl = new JLabel(instructionFileName.isEmpty() ? "No file selected" : instructionFileName);
        fileLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fileLbl.setForeground(Color.GRAY);

        uploadBtn.addActionListener(e -> selectFile());

        container.add(uploadBtn);
        container.add(fileLbl);
        container.add(Box.createVerticalStrut(25));

        // 7. SAVE BUTTON
        JButton saveBtn = Theme.createPrimaryButton(isEditMode ? "Update Task" : "Assign Task");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(200, 40));

        saveBtn.addActionListener(e -> saveTask(saveBtn));

        container.add(saveBtn);
        add(container);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f.length() > MAX_FILE_SIZE) {
                JOptionPane.showMessageDialog(this, "File too large! Max 1MB.");
                return;
            }
            try {
                byte[] bytes = Files.readAllBytes(f.toPath());
                instructionFileName = f.getName();
                instructionFileBase64 = Base64.getEncoder().encodeToString(bytes);
                fileLbl.setText(instructionFileName);
                fileLbl.setForeground(Theme.FG_GREEN);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadMembers(String sdlcFilter) {
        cbAssignee.removeAllItems();
        cbAssignee.addItem("Loading...");

        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                List<String> names = new ArrayList<>();
                ApiFuture<QuerySnapshot> future = db.collection("members").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();

                for (QueryDocumentSnapshot doc : docs) {
                    String name = doc.getString("name");
                    String role = doc.getString("sdlcCategory");

                    if (name != null) {
                        boolean match = false;
                        if (role != null && role.equalsIgnoreCase(sdlcFilter)) {
                            match = true;
                        }

                        if (match)
                            names.add(name);
                    }
                }
                return names;
            }

            @Override
            protected void done() {
                try {
                    List<String> result = get();
                    cbAssignee.removeAllItems();
                    if (result.isEmpty()) {
                        cbAssignee.addItem("No members found");
                    } else {
                        for (String n : result)
                            cbAssignee.addItem(n);
                    }

                    // Restore Selection if Edit Mode
                    if (isEditMode && taskToEdit.getAssignee() != null) {
                        // Check if still valid in this filter
                        for (int i = 0; i < cbAssignee.getItemCount(); i++) {
                            if (cbAssignee.getItemAt(i).equals(taskToEdit.getAssignee())) {
                                cbAssignee.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cbAssignee.removeAllItems();
                    cbAssignee.addItem("Error");
                }
            }
        }.execute();
    }

    private void saveTask(JButton btn) {
        String name = txtTitle.getText().trim();
        String assignee = (String) cbAssignee.getSelectedItem();

        if (name.isEmpty() || assignee == null || assignee.contains("Loading") || assignee.contains("No members")) {
            JOptionPane.showMessageDialog(this, "Please check fields.");
            return;
        }

        btn.setText("Saving...");
        btn.setEnabled(false);

        // Preserve Submission Data if Editing
        String subName = (isEditMode) ? taskToEdit.getSubmissionFileName() : "";
        String subFile = (isEditMode) ? taskToEdit.getSubmissionFile() : "";

        String selectedPhase = (String) cbSdlc.getSelectedItem();
        if (selectedPhase == null || selectedPhase.isEmpty()) {
            selectedPhase = "Planning";
        }

        Task t = new Task(
                name,
                (String) cbPriority.getSelectedItem(),
                (isEditMode ? taskToEdit.getStatus() : "To Do"), // Default to To Do, or keep existing if edit
                assignee,
                txtAssignDate.getText().trim(), // NEW
                txtDeadline.getText().trim(),
                selectedPhase, // Used SDLC filter as category
                instructionFileName,
                instructionFileBase64,
                subName,
                subFile);

        // Ensure ID is set if editing, so we don't create a new doc if we want to
        // update (though below checks isEditMode)
        if (isEditMode) {
            t.setId(taskToEdit.getId());
        }

        Firestore db = FirebaseManager.getFirestore();
        if (db != null) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (isEditMode && taskToEdit.getId() != null) {
                        db.collection("tasks").document(taskToEdit.getId()).set(t).get();
                    } else {
                        db.collection("tasks").add(t).get();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(AddTaskDialog.this, "Task Saved!");
                    dispose();
                }
            }.execute();
        }
    }
}
