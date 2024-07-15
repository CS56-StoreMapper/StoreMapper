package com.example.util;

import com.example.model.*;
import java.util.Random;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.function.Function;

public final class TestDataGenerator {
    private static final Random random = new Random();

    private TestDataGenerator() {} // Prevents instantiation

    private record NameComponents(String prefix, String type) {}

    public static Map<Long, Location> generateTestLocations(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(TestDataGenerator::generateRandomLocation)
            .collect(Collectors.toMap(Location::getId, Function.identity()));
    }

    private static Location generateRandomLocation(long id) {
        NameComponents nameComponents = generateRandomNameComponents();
        if (nameComponents instanceof NameComponents(String prefix, String type)) {
            String name = nameComponents.prefix + " " + nameComponents.type;
            Coordinates coordinates = generateRandomCoordinates();
        
            return random.nextBoolean()
                ? LocationFactory.createStore(id, name, coordinates)
                : LocationFactory.createRestaurant(id, name, coordinates);
        }
        throw new IllegalStateException("Invalid name components");
    }

    private static NameComponents generateRandomNameComponents() {
        String[] prefixes = {"North", "South", "East", "West", "Central", "Downtown", "Uptown"};
        String[] types = {"Grocery", "Cafe", "Bookstore", "Restaurant", "Supermarket", "Bakery", "Pharmacy"};
        return new NameComponents(
            prefixes[random.nextInt(prefixes.length)],
            types[random.nextInt(types.length)]
        );
    }

    private static Coordinates generateRandomCoordinates() {
        // Generate coordinates within a reasonable range (e.g., Los Angeles area)
        double delta = 0.1;
        double lat = 33.99 + (random.nextDouble() * 2 * delta - delta); 
        double lon = -118.331 + (random.nextDouble() * 2 * delta - delta); 
        return new Coordinates(lat, lon);
    }
}