package com.example.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Logger;

public class GeoJsonLoader {
    private static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(GeoJsonLoader.class.getName());

    public static String loadGeoJson(String filename) {
        String resourcePath = "/prod_data/" + filename;
        logger.info("Attempting to load resource: " + resourcePath);

        // Print out the classpath
        String classpath = System.getProperty("java.class.path");
        logger.info("Classpath: " + classpath);

        // List all resources in the prod_data directory
        try {
            Enumeration<URL> resources = GeoJsonLoader.class.getClassLoader().getResources("prod_data");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                logger.info("Found resource: " + resource);
            }
        } catch (Exception e) {
            logger.warning("Error listing resources: " + e.getMessage());
        }

        URL resourceUrl = GeoJsonLoader.class.getResource(resourcePath);
        if (resourceUrl != null) {
            logger.info("Resource URL: " + resourceUrl.toString());
        } else {
            logger.warning("Resource URL is null for: " + resourcePath);
        }

        try (InputStream is = GeoJsonLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.severe("Resource not found: " + resourcePath);
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            try (Reader reader = new InputStreamReader(is)) {
                JsonObject geoJson = gson.fromJson(reader, JsonObject.class);
                return gson.toJson(geoJson);
            }
        } catch (Exception e) {
            logger.severe("Failed to load GeoJSON from " + filename + ": " + e.getMessage());
            throw new RuntimeException("Failed to load GeoJSON from " + filename + ": " + e.getMessage(), e);
        }
    }
}