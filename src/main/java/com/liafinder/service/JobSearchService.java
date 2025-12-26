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
        int limit = (cfg.search().query() != null) ? cfg.search().query().maxPerQuery() : 50;

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

        // Note: For Oct 2026, most current ads are irrelevant.
        // We will filter heavily in parseResponse.
        return new ArrayList<>(new HashSet<>(base));
    }

    private static List<Listing> parseResponse(String jsonBody, AppConfig cfg) {
        List<Listing> listings = new ArrayList<>();
        int droppedExclusion = 0;
        int droppedNoLia = 0;
        int droppedWrongTitle = 0;

        try {
            JsonNode root = mapper.readTree(jsonBody);
            JsonNode hits = root.path("hits");
            if (hits.isArray()) {
                for (JsonNode hit : hits) {
                    String title = hit.path("headline").asText("");
                    if (title.isEmpty())
                        title = hit.path("title").asText("");
                    String employer = hit.path("employer").path("name").asText("");

                    String description = "";
                    JsonNode descNode = hit.path("description");
                    if (descNode.isObject()) {
                        description = descNode.path("text").asText("");
                    } else if (descNode.isTextual()) {
                        description = descNode.asText();
                    }

                    String titleL = title.toLowerCase();
                    String combinedL = (titleL + "\n" + description.toLowerCase());

                    // --- STAGE 1: EXCLUSION TERMS (Strict) ---
                    List<String> badTerms = new ArrayList<>(List.of(
                            "chef", "manager", "ledare", "senior", "principal", "specialist",
                            "erfaren", "tillsvidare", "fast anställning", "hel-tid", "fullstack-utvecklare till",
                            "vi söker en", "working at", "apply now", "provanställning", "omgående"));
                    if (cfg.search().notLiaTerms() != null)
                        badTerms.addAll(cfg.search().notLiaTerms());
                    if (cfg.linkedin() != null && cfg.linkedin().notLiaTerms() != null)
                        badTerms.addAll(cfg.linkedin().notLiaTerms());

                    if (containsAny(combinedL, badTerms)) {
                        // Exception: If it's a LIA ad it might still have "apply now" or "omgående" in
                        // some cases,
                        // but for "Senior" or "Chef" it's a hard drop.
                        if (titleL.contains("chef") || titleL.contains("manager") || titleL.contains("senior")) {
                            droppedExclusion++;
                            continue;
                        }
                        if (containsAny(combinedL, List.of("tillsvidare", "fast anställning"))) {
                            droppedExclusion++;
                            continue;
                        }
                    }

                    // --- STAGE 2: TITLE RELEVANCE (Very Strict) ---
                    // Actual LIA ads ALMOST ALWAYS put LIA/Praktik/Intern in the title.
                    // If the title is just "Javautvecklare", it's 99% a permanent job.
                    List<String> liaKeywords = cfg.search().liaTerms() != null ? cfg.search().liaTerms()
                            : List.of("LIA", "praktik", "intern", "yh-");
                    boolean titleHasLia = containsAny(titleL, liaKeywords);

                    if (!titleHasLia) {
                        // If title doesn't have LIA term, check if it's broad like "Developer"
                        // but then it MUST have a LIA term very early in description or be special.
                        // To be safe for the user, we'll drop it if title is totally generic.
                        droppedWrongTitle++;
                        continue;
                    }

                    // --- STAGE 3: MANDATORY CONTEXT ---
                    if (!containsAny(combinedL, liaKeywords)) {
                        droppedNoLia++;
                        continue;
                    }

                    // --- STAGE 4: DATE ANALYSIS ---
                    // If the ad mentions "2025" and NOT "2026", it's likely too early.
                    if (combinedL.contains("2025") && !combinedL.contains("2026")) {
                        // continue; // Temporarily disabled to not be TOO aggressive, but likely
                        // correct.
                    }

                    JsonNode wp = hit.path("workplace_address");
                    String location = wp.path("municipality").asText("");
                    if (location.isEmpty())
                        location = wp.path("city").asText("");

                    String adId = hit.path("id").asText("");
                    String url = hit.path("webpage_url").asText("");
                    if (url.isEmpty() && !adId.isEmpty()) {
                        url = "https://platsbanken.se/annons/" + adId;
                    }

                    listings.add(new Listing(title, employer, location, url, description, "JobTech"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("  Filtered: " + droppedNoLia + " no LIA terms, "
                + droppedExclusion + " non-LIA roles, " + droppedWrongTitle + " generic titles (non-LIA).");

        return listings;
    }

    private static boolean containsAny(String text, List<String> terms) {
        if (terms == null || text == null)
            return false;
        String lowerText = text.toLowerCase();
        for (String term : terms) {
            if (lowerText.contains(term.toLowerCase()))
                return true;
        }
        return false;
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
