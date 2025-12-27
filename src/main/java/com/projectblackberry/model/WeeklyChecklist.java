package com.projectblackberry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyChecklist {
    private String weekId;
    private Map<String, DayChecklist> days;
    private List<ResourceLink> resources;

    @JsonCreator
    public WeeklyChecklist(
            @JsonProperty("weekId") String weekId,
            @JsonProperty("days") Map<String, DayChecklist> days,
            @JsonProperty("resources") List<ResourceLink> resources) {
        this.weekId = weekId;
        this.days = days != null ? days : new HashMap<>();
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    // Getters
    public String getWeekId() { return weekId; }
    public Map<String, DayChecklist> getDays() { return days; }
    public List<ResourceLink> getResources() { return resources; }
}

