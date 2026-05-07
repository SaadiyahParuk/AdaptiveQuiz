package com.example.adaptivegamifiedlearningsystem;

public class QuizSet {
    private long id;
    private String ownerUserId;   // Firebase UID
    private String title;
    private String description;
    private long updatedAt;

    public QuizSet() {}
    public QuizSet(long id, String ownerUserId, String title, String description, long updatedAt) {
        this.id = id; this.ownerUserId = ownerUserId; this.title = title;
        this.description = description; this.updatedAt = updatedAt;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String v) { this.ownerUserId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long v) { this.updatedAt = v; }
}