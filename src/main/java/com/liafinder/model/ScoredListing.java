package com.liafinder.model;

import java.util.ArrayList;
import java.util.List;

public class ScoredListing extends Listing {
    public double score;
    public List<String> reasons = new ArrayList<>();

    public ScoredListing() {
        super();
    }
    
    public ScoredListing(Listing l) {
        super(l.title, l.company, l.location, l.url, l.description, l.source);
    }
}
