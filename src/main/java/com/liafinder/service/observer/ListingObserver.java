package com.liafinder.service.observer;

import com.liafinder.config.AppConfig;
import com.liafinder.model.ScoredListing;

import java.util.List;

public interface ListingObserver {
    void onListingsFound(List<ScoredListing> listings, AppConfig config);
}
