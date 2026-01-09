package com.liafinder.service;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;
import com.liafinder.model.ScoredListing;
import com.liafinder.service.scoring.DateScoringStrategy;
import com.liafinder.service.scoring.KeywordScoringStrategy;
import com.liafinder.service.scoring.LocationScoringStrategy;
import com.liafinder.service.scoring.ScoringStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankingService {
    private final List<ScoringStrategy> strategies;

    public RankingService() {
        this.strategies = new ArrayList<>();
        this.strategies.add(new KeywordScoringStrategy());
        this.strategies.add(new DateScoringStrategy());
        this.strategies.add(new LocationScoringStrategy());
    }

    public List<ScoredListing> scoreListings(AppConfig cfg, List<Listing> listings) {
        List<ScoredListing> scored = new ArrayList<>();

        for (Listing l : listings) {
            ScoredListing sl = new ScoredListing(l);
            sl.score = 1.0; // Base score

            for (ScoringStrategy strategy : strategies) {
                strategy.score(sl, cfg);
            }

            scored.add(sl);
        }

        scored.sort(Comparator.comparingDouble((ScoredListing sl) -> sl.score).reversed());
        return scored;
    }
}
