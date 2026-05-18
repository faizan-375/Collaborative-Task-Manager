package com.mycompany.projectscd;

public class Task {
    private String id;
    private String name;
    private String priority;
    private String status;
    private String assignee;
    private String assignDate; // NEW FIELD
    private String deadline;

    private String category;

    // --- FILE 1: Admin Instructions ---
    private String instructionFileName;
    private String instructionFile; // Base64 String

    // --- FILE 2: User Submission ---
    private String submissionFileName;
    private String submissionFile; // Base64 String
    private boolean performanceDeducted = false; // NEW: Track if penalty applied

    public Task() {
    }

    public Task(String name, String priority, String status, String assignee,
            String assignDate, String deadline,
            String category,
            String instructionFileName, String instructionFile,
            String submissionFileName, String submissionFile) {
        this.name = name;
        this.priority = priority;
        this.status = status;
        this.assignee = assignee;
        this.assignDate = assignDate;
        this.deadline = deadline;
        this.category = category;
        this.instructionFileName = instructionFileName;
        this.instructionFile = instructionFile;
        this.submissionFileName = submissionFileName;
        this.submissionFile = submissionFile;
    }

    // --- GETTERS ---
    public String getName() {
        return name;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getAssignDate() {
        return assignDate;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getInstructionFileName() {
        return instructionFileName;
    }

    public String getInstructionFile() {
        return instructionFile;
    }

    public String getSubmissionFileName() {
        return submissionFileName;
    }

    public String getSubmissionFile() {
        return submissionFile;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    // --- SETTERS (REQUIRED FOR FIREBASE) ---
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setAssignDate(String assignDate) {
        this.assignDate = assignDate;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setInstructionFileName(String instructionFileName) {
        this.instructionFileName = instructionFileName;
    }

    public void setInstructionFile(String instructionFile) {
        this.instructionFile = instructionFile;
    }

    public void setSubmissionFileName(String submissionFileName) {
        this.submissionFileName = submissionFileName;
    }

    public void setSubmissionFile(String submissionFile) {
        this.submissionFile = submissionFile;
    }

    public boolean isPerformanceDeducted() {
        return performanceDeducted;
    }

    public void setPerformanceDeducted(boolean performanceDeducted) {
        this.performanceDeducted = performanceDeducted;
    }

    // Optional legacy field to silence warnings
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}