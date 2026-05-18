package com.mycompany.projectscd;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PerformanceManager {

    /**
     * Checks all tasks assigned to the member.
     * If a task is overdue and not completed, and hasn't triggered a deduction yet:
     * 1. Deduct 10% from member's performance.
     * 2. Mark task as deducted.
     * 3. Send a warning notification.
     */
    public static void checkAndDeductPerformance(Member member) {
        if (member == null || member.getName() == null)
            return;

        Firestore db = FirebaseManager.getFirestore();
        if (db == null)
            return;

        try {
            // Fetch tasks for this member
            // Note: This blocks the thread. Caller should ensure it's in background
            // (SwingWorker).
            List<QueryDocumentSnapshot> tasks = db.collection("tasks")
                    .whereEqualTo("assignee", member.getName())
                    .get().get().getDocuments();

            boolean performanceUpdated = false;
            double currentPerf = member.getPerformance();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat notifTime = new SimpleDateFormat("hh:mm a");
            Date now = new Date();

            for (QueryDocumentSnapshot doc : tasks) {
                Task task = doc.toObject(Task.class);
                task.setId(doc.getId());

                if (task.isPerformanceDeducted())
                    continue; // Already penalized

                // Check if submitted (File needs to be present)
                boolean hasSubmitted = task.getSubmissionFile() != null && !task.getSubmissionFile().isEmpty();

                if (hasSubmitted)
                    continue; // User did their work

                // Check Deadline
                if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
                    try {
                        Date deadline = sdf.parse(task.getDeadline());

                        // If current time is after deadline
                        if (now.after(deadline)) {
                            // DEDUCT
                            currentPerf -= 10.0;
                            if (currentPerf < 0)
                                currentPerf = 0;

                            task.setPerformanceDeducted(true);

                            // Update Task Logic
                            db.collection("tasks").document(task.getId()).set(task);

                            // Send Notification
                            String msg = "Task '" + task.getName() + "' assigned to " + member.getName()
                                    + " is delayed. Performance -10%.";
                            Notification n = new Notification(
                                    "Performance Warning",
                                    msg,
                                    notifTime.format(now),
                                    "WARNING");

                            db.collection("notifications").add(n);

                            performanceUpdated = true;
                        }
                    } catch (Exception e) {
                        System.err.println("Date parse error for task " + task.getName() + ": " + e.getMessage());
                    }
                }
            }

            if (performanceUpdated) {
                member.setPerformance(currentPerf);
                // Update Member in DB
                db.collection("members").document(member.getId()).set(member);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
