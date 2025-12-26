package com.liafinder;

import com.liafinder.config.AppConfig;
import com.liafinder.config.ConfigLoader;
import com.liafinder.model.Company;
import com.liafinder.model.Profile;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("LIA Finder AI Assistant - Java Version");

        // Basic argument parsing
        String mode = "monitor"; // Default
        if (args.length > 0) {
            mode = args[0].toLowerCase();
        }

        try {
            // Assume we run from project root, adjacent to yaml files
            // For dev execution from IDE, paths might need adjustment or config passed as
            // arg.
            // We'll look in current dir.

            File configParams = new File("config.yaml");
            if (!configParams.exists()) {
                System.err.println("Error: config.yaml not found in current directory.");
                return;
            }

            AppConfig config = ConfigLoader.loadConfig("config.yaml");
            System.out.println("Loaded Config. Location: " + config.search().locations());

            if ("monitor".equals(mode)) {
                System.out.println("Mode: Monitor (Not implemented yet)");
            } else if ("outreach".equals(mode)) {
                System.out.println("Mode: Outreach");
                List<Company> companies = ConfigLoader.loadCompanies("companies.yaml");
                Profile profile = ConfigLoader.loadProfile("profile.yaml");
                System.out.println("Loaded " + companies.size() + " companies.");
                System.out.println("Loaded profile for: " + profile.person().get("full_name"));
            } else {
                System.out.println("Unknown mode: " + mode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
