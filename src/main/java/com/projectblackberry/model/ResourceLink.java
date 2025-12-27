package com.projectblackberry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceLink {
    private String title;
    private String url;
    private String category;

    @JsonCreator
    public ResourceLink(
            @JsonProperty("title") String title,
            @JsonProperty("url") String url,
            @JsonProperty("category") String category) {
        this.title = title;
        this.url = url;
        this.category = category != null ? category : "General";
    }

    // Getters
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getCategory() { return category; }
}

