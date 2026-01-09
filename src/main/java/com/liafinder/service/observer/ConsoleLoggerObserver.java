package com.liafinder.service.observer;

import com.liafinder.config.AppConfig;
import com.liafinder.model.ScoredListing;

import java.util.List;

public class ConsoleLoggerObserver implements ListingObserver {
    @Override
    public void onListingsFound(List<ScoredListing> listings, AppConfig config) {
        System.out.println("[Observer] New listings processed: " + listings.size());
        if (!listings.isEmpty()) {
            System.out.println("[Observer] Top match: " + listings.get(0).title + " at " + listings.get(0).company);
        }
    }
}
