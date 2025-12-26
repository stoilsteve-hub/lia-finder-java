package com.liafinder.service;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;
import com.liafinder.model.ScoredListing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingService {

    public static List<ScoredListing> scoreListings(AppConfig cfg, List<Listing> listings) {
        List<ScoredListing> scored = new ArrayList<>();

        for (Listing l : listings) {
            ScoredListing sl = new ScoredListing(l);
            double score = 1.0; // Base score
            String titleLower = l.title.toLowerCase();
            String descriptionLower = (l.description != null ? l.description : "").toLowerCase();
            String combinedLower = (titleLower + " " + descriptionLower);

            // 1. LIA Term Bonus (Higher weights for LIA relevance)
            if (cfg.search().liaTerms() != null) {
                for (String term : cfg.search().liaTerms()) {
                    String t = term.toLowerCase();
                    if (titleLower.contains(t)) {
                        score += 15.0; // Large bonus for LIA in title
                        sl.reasons.add("LIA term in title: " + term);
                    } else if (descriptionLower.contains(t)) {
                        score += 5.0;
                        sl.reasons.add("LIA term in description: " + term);
                    }
                }
            }

            // 2. Java Term Bonus
            if (cfg.search().javaTerms() != null) {
                for (String term : cfg.search().javaTerms()) {
                    String t = term.toLowerCase();
                    if (titleLower.contains(t)) {
                        score += 5.0;
                    } else if (descriptionLower.contains(t)) {
                        score += 2.0;
                    }
                }
            }

            // 3. Exclusion Penalties (Secondary check)
            List<String> notLia = cfg.search().notLiaTerms();
            if (notLia == null || notLia.isEmpty()) {
                notLia = (cfg.linkedin() != null) ? cfg.linkedin().notLiaTerms() : null;
            }
            if (notLia != null) {
                for (String term : notLia) {
                    if (combinedLower.contains(term.toLowerCase())) {
                        score -= 50.0; // Heavy penalty
                        sl.reasons.add("Excluded term found: " + term);
                    }
                }
            }

            // 4. Specific Date Bonus (Oct 2026 - March 2027)
            if (combinedLower.contains("2026") || combinedLower.contains("2027")) {
                score += 5.0;
                if (combinedLower.contains("oktober") || combinedLower.contains("october")
                        || combinedLower.contains("10")) {
                    score += 10.0;
                    sl.reasons.add("Target month match (October)");
                }
                if (combinedLower.contains("mars") || combinedLower.contains("march") || combinedLower.contains("03")) {
                    score += 5.0;
                    sl.reasons.add("Target month match (March)");
                }
            }

            // 5. Remote / location bonuses
            if (cfg.search().remoteOk() && combinedLower.contains("remote")) {
                score += 2.0;
            }
            if (cfg.search().locations() != null) {
                for (String loc : cfg.search().locations()) {
                    if (combinedLower.contains(loc.toLowerCase())) {
                        score += 1.0;
                    }
                }
            }

            sl.score = score;
            scored.add(sl);
        }

        // Sort descending by score
        scored.sort(Comparator.comparingDouble((ScoredListing sl) -> sl.score).reversed());
        return scored;
    }
}
