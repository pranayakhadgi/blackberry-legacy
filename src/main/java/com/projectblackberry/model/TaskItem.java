package com.projectblackberry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskItem {
    private String id;
    private String description;
    private boolean completed;
    private String estimatedTime; // e.g., "90m", "120m"
    private String priority; // HIGH, CRITICAL, MEDIUM, LOW

    @JsonCreator
    public TaskItem(
            @JsonProperty("id") String id,
            @JsonProperty("description") String description,
            @JsonProperty("completed") Boolean completed,
            @JsonProperty("estimatedTime") String estimatedTime,
            @JsonProperty("priority") String priority) {
        this.id = id;
        this.description = description;
        this.completed = completed != null ? completed : false;
        this.estimatedTime = estimatedTime != null ? estimatedTime : "";
        this.priority = priority != null ? priority : "MEDIUM";
    }

    // Getters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public String getEstimatedTime() { return estimatedTime; }
    public String getPriority() { return priority; }

    // Setters
    public void setCompleted(boolean completed) { this.completed = completed; }
}

