package com.liafinder.service.scoring;

import com.liafinder.config.AppConfig;
import com.liafinder.model.ScoredListing;

import java.util.List;

public class KeywordScoringStrategy implements ScoringStrategy {
    @Override
    public void score(ScoredListing listing, AppConfig config) {
        String titleLower = listing.title.toLowerCase();
        String descriptionLower = (listing.description != null ? listing.description : "").toLowerCase();
        String combinedLower = titleLower + " " + descriptionLower;

        // LIA Term Bonus
        if (config.search().liaTerms() != null) {
            for (String term : config.search().liaTerms()) {
                String t = term.toLowerCase();
                if (titleLower.contains(t)) {
                    listing.score += 15.0;
                    listing.reasons.add("LIA term in title: " + term);
                } else if (descriptionLower.contains(t)) {
                    listing.score += 5.0;
                    listing.reasons.add("LIA term in description: " + term);
                }
            }
        }

        // Java Term Bonus
        if (config.search().javaTerms() != null) {
            for (String term : config.search().javaTerms()) {
                String t = term.toLowerCase();
                if (titleLower.contains(t)) {
                    listing.score += 5.0;
                } else if (descriptionLower.contains(t)) {
                    listing.score += 2.0;
                }
            }
        }

        // Exclusion Penalties
        List<String> notLia = config.search().notLiaTerms();
        if (notLia == null || notLia.isEmpty()) {
            notLia = (config.linkedin() != null) ? config.linkedin().notLiaTerms() : null;
        }
        if (notLia != null) {
            for (String term : notLia) {
                if (combinedLower.contains(term.toLowerCase())) {
                    listing.score -= 50.0;
                    listing.reasons.add("Excluded term found: " + term);
                }
            }
        }
    }
}
