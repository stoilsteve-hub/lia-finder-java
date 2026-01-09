package com.liafinder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Listing {
    public String title;
    public String company;
    public String location;
    public String url;
    public String description;
    public String source;

    public Listing() {
    }

    public Listing(String title, String company, String location, String url, String description, String source) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.url = url;
        this.description = description;
        this.source = source;
    }
}
