package com.liafinder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobSearchService {
    private static final String API_ENDPOINT = "https://jobsearch.api.jobtechdev.se/search";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Listing> fetchListings(AppConfig cfg) {
        String apiKey = System.getenv("JOBTECH_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Warning: JOBTECH_API_KEY environment variable not set. Cannot fetch listings.");
            return List.of();
        }

        List<Listing> allListings = new ArrayList<>();
        List<String> queries = buildQueries(cfg);
        int limit = cfg.search().query().maxPerQuery();

        System.out.println("Fetching listings for " + queries.size() + " queries...");

        for (String q : queries) {
            try {
                String encodedQ = URLEncoder.encode(q, StandardCharsets.UTF_8);
                String uri = API_ENDPOINT + "?q=" + encodedQ + "&limit=" + limit;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .header("accept", "application/json")
                        .header("api-key", apiKey)
                        .header("User-Agent", "LIA_FINDER_AI_ASSISTANT_JAVA/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    allListings.addAll(parseResponse(response.body(), cfg));
                } else {
                    System.err.println("Failed to fetch for query '" + q + "': " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Error fetching query '" + q + "': " + e.getMessage());
            }
        }

        return removeDuplicates(allListings);
    }

    private static List<String> buildQueries(AppConfig cfg) {
        List<String> locations = cfg.search().locations() != null ? cfg.search().locations() : List.of("Stockholm");
        String loc = String.join(" ", locations);

        List<String> base = new ArrayList<>(List.of(
                "LIA Java " + loc,
                "praktik Java " + loc,
                "\"lärande i arbete\" Java " + loc,
                "yrkeshögskola Java " + loc,
                "internship Java " + loc,
                "LIA Spring Boot " + loc,
                "praktik Spring Boot " + loc,
                "LIA backend Java " + loc,
                "praktik backend Java " + loc));

        if (cfg.search().remoteOk() && cfg.search().query().addRemoteQueries()) {
            base.addAll(List.of(
                    "LIA Java distans",
                    "praktik Java distans",
                    "internship Java remote",
                    "LIA backend Java remote"));
        }

        // De-duplicate
        return new ArrayList<>(new HashSet<>(base));
    }

    private static List<Listing> parseResponse(String jsonBody, AppConfig cfg) {
        List<Listing> listings = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(jsonBody);
            JsonNode hits = root.path("hits");
            if (hits.isArray()) {
                for (JsonNode hit : hits) {
                    String title = hit.path("headline").asText("");
                    if (title.isEmpty())
                        title = hit.path("title").asText("");

                    String employer = hit.path("employer").path("name").asText("");

                    JsonNode wp = hit.path("workplace_address");
                    String location = wp.path("municipality").asText("");
                    if (location.isEmpty())
                        location = wp.path("city").asText("");

                    String adId = hit.path("id").asText("");
                    String url = hit.path("webpage_url").asText("");
                    if (url.isEmpty() && !adId.isEmpty()) {
                        url = "https://platsbanken.se/annons/" + adId;
                    }

                    String description = "";
                    JsonNode descNode = hit.path("description");
                    if (descNode.isObject()) {
                        description = descNode.path("text").asText("");
                    } else if (descNode.isTextual()) {
                        description = descNode.asText();
                    }

                    // Simple basic filter to ensure it's not totally irrelevant
                    // (Real logic should have the strict checks from Python)

                    listings.add(new Listing(title, employer, location, url, description, "JobTech"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listings;
    }

    private static List<Listing> removeDuplicates(List<Listing> raw) {
        List<Listing> uniq = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();
        for (Listing l : raw) {
            if (l.url != null && !seenUrls.contains(l.url)) {
                seenUrls.add(l.url);
                uniq.add(l);
            }
        }
        return uniq;
    }
}
