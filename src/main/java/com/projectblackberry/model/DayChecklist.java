package com.projectblackberry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class DayChecklist {
    private String date;
    private List<TaskItem> tasks;
    private String plannedTime; // e.g., "3h"

    @JsonCreator
    public DayChecklist(
            @JsonProperty("date") String date,
            @JsonProperty("tasks") List<TaskItem> tasks,
            @JsonProperty("plannedTime") String plannedTime) {
        this.date = date;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.plannedTime = plannedTime != null ? plannedTime : "";
    }

    // Getters
    public String getDate() { return date; }
    public List<TaskItem> getTasks() { return tasks; }
    public String getPlannedTime() { return plannedTime; }
}

