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
        String name = nameComponents.prefix() + " " + nameComponents.type();
        
        return switch (random.nextInt(3)) {
            case 0 -> generateWithCoordinates(id, name);
            case 1 -> generateWithLatLon(id, name);
            case 2 -> generateWithOsmNode(id, name);
            default -> throw new IllegalStateException("Unexpected value");
        };
    }

    private static Location generateWithCoordinates(long id, String name) {
        Coordinates coordinates = generateRandomCoordinates();
        return random.nextBoolean()
            ? LocationFactory.createStore(id, name, coordinates)
            : LocationFactory.createRestaurant(id, name, coordinates);
    }

    private static Location generateWithLatLon(long id, String name) {
        Coordinates coordinates = generateRandomCoordinates();
        return random.nextBoolean()
            ? LocationFactory.createStore(id, name, coordinates.getLatitude(), coordinates.getLongitude())
            : LocationFactory.createRestaurant(id, name, coordinates.getLatitude(), coordinates.getLongitude());
    }

    private static Location generateWithOsmNode(long id, String name) {
        Coordinates coordinates = generateRandomCoordinates();
        Node osmNode = new Node(id, coordinates.getLatitude(), coordinates.getLongitude(), Map.of("name", name));
        return random.nextBoolean()
            ? LocationFactory.createStore(id, name, osmNode)
            : LocationFactory.createRestaurant(id, name, osmNode);
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