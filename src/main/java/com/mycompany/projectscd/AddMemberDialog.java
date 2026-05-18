package com.mycompany.projectscd;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class AddMemberDialog extends JDialog {

    private final Member memberToEdit; // If null, we are adding new
    private final String docId;

    // Constructor for ADDING
    public AddMemberDialog(Window parent) {
        this(parent, null, null);
    }

    // Constructor for EDITING
    public AddMemberDialog(Window parent, Member member, String id) {
        super(parent, member == null ? "Add New Team Member" : "Edit Team Member", ModalityType.APPLICATION_MODAL);
        this.memberToEdit = member;
        this.docId = id;

        setSize(450, 600);
        setLocationRelativeTo(parent);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        Border fieldBorder = new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 8, 5, 8));

        // Name
        p.add(createLabel("Full Name:"), gbc);
        gbc.gridy++;
        JTextField txtName = new JTextField();
        txtName.setBorder(fieldBorder);
        p.add(txtName, gbc);

        // Contact
        gbc.gridy++;
        p.add(createLabel("Contact Number:"), gbc);
        gbc.gridy++;
        JTextField txtContact = new JTextField();
        txtContact.setBorder(fieldBorder);
        p.add(txtContact, gbc);

        // Email
        gbc.gridy++;
        p.add(createLabel("Email Address:"), gbc);
        gbc.gridy++;
        JTextField txtEmail = new JTextField();
        txtEmail.setBorder(fieldBorder);
        p.add(txtEmail, gbc);

        // Experience Level
        gbc.gridy++;
        p.add(createLabel("Experience Level:"), gbc);
        gbc.gridy++;
        String[] levels = { "Beginner", "Intermediate", "Professional" };
        JComboBox<String> cbLevel = new JComboBox<>(levels);
        cbLevel.setBackground(Color.WHITE);
        p.add(cbLevel, gbc);

        // SDLC Category
        gbc.gridy++;
        p.add(createLabel("SDLC Category:"), gbc);
        gbc.gridy++;
        String[] categories = { "Planning", "Analysis", "Design", "Implementation", "Testing",
                "Maintenance" };
        JComboBox<String> cbCategory = new JComboBox<>(categories);
        cbCategory.setBackground(Color.WHITE);
        p.add(cbCategory, gbc);

        // Working Experience
        gbc.gridy++;
        p.add(createLabel("Working Experience (Years):"), gbc);
        gbc.gridy++;
        JTextField txtExp = new JTextField();
        txtExp.setBorder(fieldBorder);
        p.add(txtExp, gbc);

        // PRE-FILL IF EDITING
        if (memberToEdit != null) {
            txtName.setText(memberToEdit.getName());
            txtContact.setText(memberToEdit.getContact());
            txtEmail.setText(memberToEdit.getEmail());
            cbLevel.setSelectedItem(memberToEdit.getExperienceLevel());
            cbCategory.setSelectedItem(memberToEdit.getSdlcCategory());
            txtExp.setText(String.valueOf(memberToEdit.getWorkingExperience()));
            txtEmail.setEditable(false); // Can't change email usually as it's login ID
        }

        gbc.gridy++;
        p.add(Box.createVerticalStrut(20), gbc);

        JButton save = Theme.createPrimaryButton(memberToEdit == null ? "Save Member" : "Update Member");
        save.setPreferredSize(new Dimension(100, 40));

        save.addActionListener(e -> {
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            String email = txtEmail.getText().trim();
            String level = (String) cbLevel.getSelectedItem();
            String category = (String) cbCategory.getSelectedItem();
            String expStr = txtExp.getText().trim();

            if (name.isEmpty() || contact.isEmpty() || email.isEmpty() || expStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            double experience = 0.0;
            try {
                experience = Double.parseDouble(expStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid experience format. Please enter a number.");
                return;
            }

            Member newMember = new Member(name, category, email, contact, level, category, experience);

            // If updating, preserve old logic like password or rating if needed,
            // but for now simple overwrite is fine or re-set them.
            if (memberToEdit != null) {
                newMember.setPassword(memberToEdit.getPassword());
                newMember.setRating(memberToEdit.getRating());
            }

            Firestore db = FirebaseManager.getFirestore();
            if (db != null) {
                save.setText("Saving...");
                save.setEnabled(false);

                // DECIDE: Add new or Update existing
                ApiFuture<?> future;
                if (memberToEdit != null && docId != null) {
                    future = db.collection("members").document(docId).set(newMember);
                } else {
                    future = db.collection("members").add(newMember);
                }

                try {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            future.get();
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                JOptionPane.showMessageDialog(AddMemberDialog.this,
                                        memberToEdit == null ? "Member Saved Successfully!\nPassword sent to: 12345"
                                                : "Member Updated Successfully!");
                                dispose();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(AddMemberDialog.this,
                                        "Error saving member: " + ex.getMessage());
                                save.setText("Save Member");
                                save.setEnabled(true);
                            }
                        }
                    }.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        gbc.gridy++;
        p.add(save, gbc);
        add(p);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }
}
