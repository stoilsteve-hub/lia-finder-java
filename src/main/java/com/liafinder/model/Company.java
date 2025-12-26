package com.liafinder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Company(
    String name,
    String location,
    String website,
    String careers,
    String contact_email,
    List<String> stack_hints,
    String domain,
    String why,
    String notes
) {}
