package com.liafinder.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppConfig(
        SearchConfig search,
        LiaConfig lia,
        OutputConfig output,
        LinkedInConfig linkedin) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchConfig(
            List<String> locations,
            @JsonProperty("remote_ok") boolean remoteOk,
            @JsonProperty("lia_terms") List<String> liaTerms,
            @JsonProperty("java_terms") List<String> javaTerms,
            @JsonProperty("not_lia_terms") List<String> notLiaTerms,
            StrictConfig strict,
            QueryConfig query) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StrictConfig(
            @JsonProperty("title_must_contain_lia") boolean titleMustContainLia,
            @JsonProperty("must_contain_java") boolean mustContainJava) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QueryConfig(
            @JsonProperty("max_per_query") int maxPerQuery,
            @JsonProperty("add_remote_queries") boolean addRemoteQueries) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LiaConfig(
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate,
            TargetConfig target) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TargetConfig(
            @JsonProperty("desired_start") String desiredStart) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutputConfig(
            @JsonProperty("data_dir") String dataDir,
            @JsonProperty("applications_dir") String applicationsDir) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LinkedInConfig(
            boolean enabled,
            List<String> queries,
            StrictConfig strict,
            @JsonProperty("not_lia_terms") List<String> notLiaTerms) {
    }
}
