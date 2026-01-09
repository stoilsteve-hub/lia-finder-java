package com.liafinder.service;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;
import com.liafinder.model.ScoredListing;
import com.liafinder.service.observer.ConsoleLoggerObserver;
import com.liafinder.service.observer.FileStorageObserver;
import com.liafinder.service.observer.ListingObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DaemonService {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final List<ListingObserver> observers = new ArrayList<>();

    static {
        // Register default observers
        observers.add(new ConsoleLoggerObserver());
        observers.add(new FileStorageObserver());
    }

    public static void addObserver(ListingObserver observer) {
        observers.add(observer);
    }

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

                RankingService rankingService = new RankingService();
                List<ScoredListing> scored = rankingService.scoreListings(config, listings);
                
                notifyObservers(scored, config);

            } catch (Exception e) {
                System.err.println("[Daemon] Error in search cycle: " + e.getMessage());
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 24, TimeUnit.HOURS);
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Daemon interrupted.");
        }
    }

    private static void notifyObservers(List<ScoredListing> listings, AppConfig config) {
        for (ListingObserver observer : observers) {
            observer.onListingsFound(listings, config);
        }
    }
}
