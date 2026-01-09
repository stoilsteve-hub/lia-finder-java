package com.liafinder.service.observer;

import com.liafinder.config.AppConfig;
import com.liafinder.model.ScoredListing;
import com.liafinder.service.StorageService;

import java.util.List;

public class FileStorageObserver implements ListingObserver {
    @Override
    public void onListingsFound(List<ScoredListing> listings, AppConfig config) {
        if (!listings.isEmpty()) {
            StorageService.saveListings(listings, config.output().dataDir());
        }
    }
}
