package com.liafinder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Profile(
        Map<String, Object> person,
        Map<String, Object> education,
        Map<String, Object> profile) {
}
