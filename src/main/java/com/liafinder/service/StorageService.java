package com.liafinder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.liafinder.model.ScoredListing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StorageService {
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void saveListings(List<ScoredListing> listings, String dataDir) {
        try {
            Path dir = Paths.get(dataDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "listings_" + timestamp + ".json";
            File file = dir.resolve(filename).toFile();

            mapper.writeValue(file, listings);
            System.out.println("Saved " + listings.size() + " listings to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save listings: " + e.getMessage());
        }
    }
}
