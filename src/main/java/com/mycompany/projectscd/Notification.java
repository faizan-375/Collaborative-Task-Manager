package com.mycompany.projectscd;

public class Notification {
    private String title;
    private String message;
    private String time;
    private String type; // SUCCESS, DANGER, INFO, WARNING

    public Notification() {}

    public Notification(String title, String message, String time, String type) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getType() { return type; }
}