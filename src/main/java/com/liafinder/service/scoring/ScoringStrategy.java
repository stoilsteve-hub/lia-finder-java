package com.liafinder.service.scoring;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;
import com.liafinder.model.ScoredListing;

public interface ScoringStrategy {
    void score(ScoredListing listing, AppConfig config);
}
