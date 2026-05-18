package com.mycompany.projectscd;

public class Category {
    private String name;
    private String description;
    private String colorTag;
    private int progress;
    private String activeTasks;

    // Empty constructor required for Firebase
    public Category() {}

    public Category(String name, String description, String colorTag, int progress, String activeTasks) {
        this.name = name;
        this.description = description;
        this.colorTag = colorTag;
        this.progress = progress;
        this.activeTasks = activeTasks;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getColorTag() { return colorTag; }
    public int getProgress() { return progress; }
    public String getActiveTasks() { return activeTasks; }
}