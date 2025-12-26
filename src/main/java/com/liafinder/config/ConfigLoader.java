package com.liafinder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.liafinder.model.Company;
import com.liafinder.model.Profile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static AppConfig loadConfig(String path) throws IOException {
        return yamlMapper.readValue(new File(path), AppConfig.class);
    }

    public static List<Company> loadCompanies(String path) throws IOException {
        // companies.yaml structure: "companies: [...]"
        Map<String, List<Company>> wrapper = yamlMapper.readValue(new File(path),
                yamlMapper.getTypeFactory().constructMapType(Map.class, String.class,
                        yamlMapper.getTypeFactory().constructCollectionType(List.class, Company.class)));
        return wrapper.get("companies");
    }

    public static Profile loadProfile(String path) throws IOException {
        return yamlMapper.readValue(new File(path), Profile.class);
    }
}
