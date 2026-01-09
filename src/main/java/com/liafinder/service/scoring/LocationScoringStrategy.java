package com.liafinder.service.scoring;

import com.liafinder.config.AppConfig;
import com.liafinder.model.ScoredListing;

public class LocationScoringStrategy implements ScoringStrategy {
    @Override
    public void score(ScoredListing listing, AppConfig config) {
        String combinedLower = (listing.title + " " + (listing.description != null ? listing.description : "")).toLowerCase();

        if (config.search().remoteOk() && combinedLower.contains("remote")) {
            listing.score += 2.0;
        }
        if (config.search().locations() != null) {
            for (String loc : config.search().locations()) {
                if (combinedLower.contains(loc.toLowerCase())) {
                    listing.score += 1.0;
                }
            }
        }
    }
}
