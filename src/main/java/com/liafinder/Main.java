package com.liafinder;

import com.liafinder.config.AppConfig;
import com.liafinder.config.ConfigLoader;
import com.liafinder.model.Company;
import com.liafinder.model.Profile;
import com.liafinder.service.OutreachService;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("LIA Finder AI Assistant - Java Version");

        String mode;
        if (args.length > 0) {
            mode = args[0].toLowerCase();
        } else {
            mode = chooseMode();
        }

        try {
            // Assume we run from project root, adjacent to yaml files
            File configParams = new File("config.yaml");
            if (!configParams.exists()) {
                System.err.println("Error: config.yaml not found in current directory.");
                return;
            }

            AppConfig config = ConfigLoader.loadConfig("config.yaml");
            System.out.println("Loaded Config. Location: " + config.search().locations());

            if ("monitor".equals(mode) || "1".equals(mode)) {
                System.out.println("Mode: Monitor");

                String apiKey = System.getenv("JOBTECH_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    System.out.println("\n[!] JOBTECH_API_KEY is missing!");
                    System.out.println("    Please set it in your Run Configuration -> Environment variables.");
                    System.out.println("    Example: JOBTECH_API_KEY=your_key_here");
                }

                List<com.liafinder.model.Listing> listings = com.liafinder.service.JobSearchService
                        .fetchListings(config);
                System.out.println("Found " + listings.size() + " listings.");

                List<com.liafinder.model.ScoredListing> scored = com.liafinder.service.RankingService
                        .scoreListings(config, listings);

                System.out.println("\nTop Matches:");
                for (int i = 0; i < Math.min(10, scored.size()); i++) {
                    com.liafinder.model.ScoredListing sl = scored.get(i);
                    System.out.printf("[%d] %s - %s (Score: %.1f)\n    URL: %s\n", i + 1, sl.title, sl.company,
                            sl.score, sl.url);
                }

            } else if ("outreach".equals(mode) || "2".equals(mode)) {
                System.out.println("Mode: Outreach");
                List<Company> companies = ConfigLoader.loadCompanies("companies.yaml");
                Profile profile = ConfigLoader.loadProfile("profile.yaml");
                System.out.println("Loaded " + companies.size() + " companies.");
                System.out.println("Loaded profile for: " + profile.person().get("full_name"));

                // Actual outreach generation call
                for (Company c : companies) {
                    OutreachService.generateOutreach(config, c, profile);
                }

            } else if ("daemon".equals(mode) || "3".equals(mode)) {
                System.out.println("Mode: Daemon (Not implemented yet)");

            } else {
                System.out.println("Unknown mode: " + mode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String chooseMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nChoose what to run:");
        System.out.println("  1) Monitor LIA (run once)");
        System.out.println("  2) Outreach Builder (generate emails/letters)");
        System.out.println("  3) Monitor daemon (run continuously)");
        System.out.print("Enter 1, 2 or 3: ");

        String choice = scanner.nextLine().trim();
        if ("2".equals(choice))
            return "outreach";
        if ("3".equals(choice))
            return "daemon";
        return "monitor";
    }
}
