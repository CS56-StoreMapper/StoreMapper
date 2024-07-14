package com.example.util;

import com.example.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class TestDataGenerator {
    private static final Random random = new Random();

    public static Map<Long, Location> generateTestLocations(int count) {
        Map<Long, Location> locations = new HashMap<>();
        for (int i = 1; i <= count; i++) {
            Location location = generateRandomLocation(i);
            locations.put(location.getId(), location);
        }
        return locations;
    }

    private static Location generateRandomLocation(long id) {
        String name = generateRandomName();
        Coordinates coordinates = generateRandomCoordinates();
        
        if (random.nextBoolean()) {
            return LocationFactory.createStore(id, name, coordinates);
        } else {
            return LocationFactory.createRestaurant(id, name, coordinates);
        }
    }

    private static String generateRandomName() {
        String[] prefixes = {"North", "South", "East", "West", "Central", "Downtown", "Uptown"};
        String[] types = {"Grocery", "Cafe", "Bookstore", "Restaurant", "Supermarket", "Bakery", "Pharmacy"};
        return prefixes[random.nextInt(prefixes.length)] + " " + types[random.nextInt(types.length)];
    }

    private static Coordinates generateRandomCoordinates() {
        // Generate coordinates within a reasonable range (e.g., Los Angeles area)
        double delta = 0.1;
        double lat = 33.99 + (random.nextDouble() * 2 * delta - delta); 
        double lon = -118.331 + (random.nextDouble() * 2 * delta - delta); 
        return new Coordinates(lat, lon);
    }
}