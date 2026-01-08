package com.liafinder.service;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Listing;
import com.liafinder.model.ScoredListing;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class RankingServiceTest {

    @Test
    public void testScoreListings_LiaTermInTitle_ShouldHaveHighScore() {
        // Mock Config
        AppConfig.SearchConfig searchConfig = new AppConfig.SearchConfig(
                List.of("Stockholm"),
                true,
                List.of("LIA", "praktik"), // liaTerms
                List.of("Java"),           // javaTerms
                Collections.emptyList(),   // notLiaTerms
                null,                      // strict
                null                       // query
        );
        
        AppConfig config = new AppConfig(searchConfig, null, null, null);

        // Mock Listing
        Listing listing = new Listing(
                "LIA Java Developer",
                "Test Company",
                "Stockholm",
                "http://example.com",
                "We are looking for a LIA student.",
                "JobTech"
        );

        List<ScoredListing> results = RankingService.scoreListings(config, List.of(listing));

        Assert.assertEquals(1, results.size());
        ScoredListing scored = results.get(0);

        // Base score 1.0
        // +15.0 for "LIA" in title
        // +5.0 for "Java" in title
        // +5.0 for "LIA" in description (from "LIA student"?) -> actually "LIA" is in title, loop checks all terms.
        // Let's trace carefully:
        // titleLower = "lia java developer"
        // descriptionLower = "we are looking for a lia student."
        //
        // 1. LIA Term Bonus:
        //    - term "LIA": title contains it -> score += 15.0.
        //    - term "praktik": neither.
        //
        // 2. Java Term Bonus:
        //    - term "Java": title contains it -> score += 5.0.
        //
        // 3. Exclusion: None.
        // 4. Date: None.
        // 5. Remote/Location:
        //    - Remote: false.
        //    - Location "Stockholm": combined contains it -> score += 1.0.
        //
        // Total expected: 1.0 + 15.0 + 5.0 + 1.0 = 22.0
        
        Assert.assertTrue("Score should be significantly boosted by LIA term", scored.score > 20.0);
        Assert.assertTrue(scored.reasons.toString().contains("LIA term in title"));
    }

    @Test
    public void testScoreListings_ExcludedTerm_ShouldHaveNegativeScore() {
        // Mock Config with exclusion
        AppConfig.SearchConfig searchConfig = new AppConfig.SearchConfig(
                List.of("Stockholm"),
                false,
                List.of("LIA"),
                List.of("Java"),
                List.of("Senior"), // notLiaTerms
                null,
                null
        );
        AppConfig config = new AppConfig(searchConfig, null, null, null);

        Listing listing = new Listing(
                "Senior Java Developer",
                "Test Company",
                "Stockholm",
                "http://example.com",
                "We need a Senior dev.",
                "JobTech"
        );

        List<ScoredListing> results = RankingService.scoreListings(config, List.of(listing));
        
        // Base 1.0
        // +5.0 (Java in title)
        // +1.0 (Location)
        // -50.0 (Excluded "Senior")
        // Total approx -43.0

        Assert.assertTrue("Score should be negative due to exclusion", results.get(0).score < 0);
    }
}
