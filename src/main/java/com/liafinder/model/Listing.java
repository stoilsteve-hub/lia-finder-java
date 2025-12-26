package com.liafinder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Listing {
    public String title;
    public String company;
    public String location;
    public String url;
    public String description;
    public String source;

    // Make it a class instead of record to allow mutability for scoring or easy
    // JSON deserialization if fields missing
    // or just stick to Record. Let's stick to Record if we can, but Score is added
    // later.
    // Actually, ScoredListing extends Listing in Python.
    // In Java, records can't extend. Composition or just adding fields.
    // Let's use a class for Listing to be safe.

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
