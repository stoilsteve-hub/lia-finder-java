package com.liafinder.service.scoring;

import com.liafinder.config.AppConfig;
import com.liafinder.model.ScoredListing;

public class DateScoringStrategy implements ScoringStrategy {
    @Override
    public void score(ScoredListing listing, AppConfig config) {
        String combinedLower = (listing.title + " " + (listing.description != null ? listing.description : "")).toLowerCase();

        if (combinedLower.contains("2026") || combinedLower.contains("2027")) {
            listing.score += 5.0;

            if (combinedLower.contains("oktober") || combinedLower.contains("october") || combinedLower.contains("10")) {
                listing.score += 10.0;
                listing.reasons.add("Target start month match (October)");
            }

            if (combinedLower.contains("mars") || combinedLower.contains("march") || combinedLower.contains("03")) {
                listing.score += 5.0;
                listing.reasons.add("Target end month match (March)");
            }

            if (config.lia().extensionWeeks() > 0) {
                if (combinedLower.contains("förlängning") || combinedLower.contains("extension") ||
                        combinedLower.contains("sommarjobb") || combinedLower.contains("summer")) {
                    listing.score += 5.0;
                    listing.reasons.add("Potential extension/summer job mentioned");
                }
            }
        }
    }
}
