package com.liafinder.service;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;
import com.liafinder.model.ScoredListing;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DaemonService {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void start(AppConfig config) {
        System.out.println("Starting Daemon Mode...");
        System.out.println("Will run every 24 hours.");

        Runnable task = () -> {
            try {
                System.out.println("\n[Daemon] Starting search cycle at " + new java.util.Date());
                
                String apiKey = System.getenv("JOBTECH_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    System.err.println("[Daemon] Error: JOBTECH_API_KEY is missing.");
                    return;
                }

                List<Listing> listings = JobSearchService.fetchListings(config);
                System.out.println("[Daemon] Found " + listings.size() + " listings.");

                List<ScoredListing> scored = RankingService.scoreListings(config, listings);
                
                if (!scored.isEmpty()) {
                    StorageService.saveListings(scored, config.output().dataDir());
                    System.out.println("[Daemon] Saved " + scored.size() + " scored listings.");
                } else {
                    System.out.println("[Daemon] No relevant listings found this cycle.");
                }

            } catch (Exception e) {
                System.err.println("[Daemon] Error in search cycle: " + e.getMessage());
                e.printStackTrace();
            }
        };

        // Run immediately, then every 24 hours
        scheduler.scheduleAtFixedRate(task, 0, 24, TimeUnit.HOURS);
        
        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Daemon interrupted.");
        }
    }
}
