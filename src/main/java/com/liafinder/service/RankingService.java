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
            double score = 0.0;
            String text = (l.title + " " + l.location + " " + (l.description != null ? l.description : ""))
                    .toLowerCase();

            // Check Java Terms
            if (cfg.search().javaTerms() != null) {
                for (String term : cfg.search().javaTerms()) {
                    if (text.contains(term.toLowerCase())) {
                        score += 10;
                        sl.reasons.add("Matched keyword: " + term);
                    }
                }
            }

            // Check Remote
            if (cfg.search().remoteOk() && text.contains("remote")) {
                score += 5;
                sl.reasons.add("Remote mention");
            }

            // Check Location
            if (cfg.search().locations() != null) {
                for (String loc : cfg.search().locations()) {
                    if (text.contains(loc.toLowerCase())) {
                        score += 3;
                        sl.reasons.add("Location match");
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
