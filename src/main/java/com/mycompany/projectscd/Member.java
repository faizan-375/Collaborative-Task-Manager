package com.mycompany.projectscd;

public class Member {
    private String name;
    private String role;
    private String email;
    private String contact;
    private String experienceLevel;
    private String sdlcCategory;
    private double workingExperience;
    private String password; // Default login password

    private double rating;
    private double performance = 100.0; // NEW: Performance Score (Starts at 100%)

    public Member() {
    }

    public Member(String name, String role, String email, String contact, String experienceLevel, String sdlcCategory,
            double workingExperience) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.contact = contact;
        this.experienceLevel = experienceLevel;
        this.sdlcCategory = sdlcCategory;
        this.workingExperience = workingExperience;
        this.password = "12345"; // Default Password
        this.rating = 5.0; // Default start rating
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getSdlcCategory() {
        return sdlcCategory;
    }

    public void setSdlcCategory(String sdlcCategory) {
        this.sdlcCategory = sdlcCategory;
    }

    public double getWorkingExperience() {
        return workingExperience;
    }

    public void setWorkingExperience(double workingExperience) {
        this.workingExperience = workingExperience;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    // ID Field (Excluded from Firestore data usually, or handled manually)
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}