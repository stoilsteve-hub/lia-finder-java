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

        if (cfg.search().remoteOk() && cfg.search().query() != null && cfg.search().query().addRemoteQueries()) {
            base.addAll(List.of(
                    "LIA Java distans",
                    "praktik Java distans",
                    "internship Java remote",
                    "LIA backend Java remote"));
        }

        return new ArrayList<>(new HashSet<>(base));
    }

    private static List<Listing> parseResponse(String jsonBody, AppConfig cfg) {
        List<Listing> listings = new ArrayList<>();
        int droppedExclusion = 0;
        int droppedNoLia = 0;
        int droppedWrongDate = 0;

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

                    String combinedL = (title + "\n" + description).toLowerCase();
                    String titleL = title.toLowerCase();

                    // --- 1. Aggressive Filtering GATES ---

                    // A. Exclusion terms (Permanent jobs, Senior roles, etc.)
                    List<String> notLia = new ArrayList<>();
                    if (cfg.search().notLiaTerms() != null)
                        notLia.addAll(cfg.search().notLiaTerms());
                    if (cfg.linkedin() != null && cfg.linkedin().notLiaTerms() != null) {
                        for (String term : cfg.linkedin().notLiaTerms()) {
                            if (!notLia.contains(term))
                                notLia.add(term);
                        }
                    }
                    // Additional hardcoded safety terms
                    notLia.addAll(List.of("provanställning", "6 månader", "erfarenhet av minst", "senior developer",
                            "principal"));

                    if (containsAny(combinedL, notLia)) {
                        droppedExclusion++;
                        continue;
                    }

                    // B. LIA Mandatory Term
                    boolean hasLiaTerm = containsAny(combinedL, cfg.search().liaTerms());
                    if (cfg.search().strict() != null && cfg.search().strict().titleMustContainLia()) {
                        if (!containsAny(titleL, cfg.search().liaTerms())) {
                            droppedNoLia++;
                            continue;
                        }
                    } else if (!hasLiaTerm) {
                        droppedNoLia++;
                        continue;
                    }

                    // C. Date Heuristic (Specific to user requirement: Oct 2026 - March 2027)
                    // If the ad contains a date like "2025" or "januari 2026", but NOT "oktober
                    // 2026",
                    // and it's for any kind of "start", we might be suspicious.
                    // For now, let's just look for "2026" or "2027".
                    if (!combinedL.contains("2026") && !combinedL.contains("2027")) {
                        // Some LIA ads don't mention the year if it's "next term",
                        // so we won't DROP them hard, but we'll be careful.
                    }

                    // If it specifically mentions "omgående" (immediately), it's probably NOT for
                    // Oct 2026.
                    if (combinedL.contains("omgående") && !combinedL.contains("oktober")) {
                        droppedWrongDate++;
                        continue;
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

        if (droppedExclusion > 0 || droppedNoLia > 0 || droppedWrongDate > 0) {
            System.out.println("  Filtered: " + droppedNoLia + " missing LIA terms, "
                    + droppedExclusion + " permanent/senior jobs, " + droppedWrongDate + " wrong start dates.");
        }

        return listings;
    }

    private static boolean containsAny(String text, List<String> terms) {
        if (terms == null || text == null)
            return false;
        String lowerText = text.toLowerCase();
        for (String term : terms) {
            if (lowerText.contains(term.toLowerCase())) {
                return true;
            }
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
