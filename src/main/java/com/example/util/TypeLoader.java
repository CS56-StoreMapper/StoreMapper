package com.example.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class TypeLoader {
    private static final Gson gson = new Gson();

    public static List<String> loadTypes(String filename) {
        try (InputStream is = TypeLoader.class.getResourceAsStream("/types/" + filename);
             InputStreamReader reader = new InputStreamReader(is)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load types from " + filename, e);
        }
    }
}