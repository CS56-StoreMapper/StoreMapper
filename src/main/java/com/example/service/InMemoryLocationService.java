package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.model.Location;
import com.example.util.TestDataGenerator;

public class InMemoryLocationService implements LocationService {
    private final Map<Long, Location> locations;

    public InMemoryLocationService() {
        this.locations = new HashMap<>();
    }

    // Constructor with number of locations to generate
    public InMemoryLocationService(int numberOfLocations) {
        this.locations = TestDataGenerator.generateTestLocations(numberOfLocations);
    }

    public InMemoryLocationService(Map<Long, Location> initialLocations) {
        this.locations = new HashMap<>(initialLocations);
    }

    @Override
    public List<Location> getAllLocations() {
        return new ArrayList<>(locations.values());
    }

    @Override
    public List<Location> searchLocations(String query) {
        var lowercaseQuery = query.toLowerCase();
        return locations.values().stream()
                .filter(location -> location.getName().toLowerCase().contains(lowercaseQuery))
                .toList();
    }

    @Override
    public List<Location> searchLocationsByOsmTag(String key, String value) {
        return locations.values().stream()
                .filter(location -> location.getOsmTag(key)
                        .map(tag -> tag.equals(value))
                        .orElse(false))
                .toList();
    }

    @Override
    public void addLocation(Location location) {
        locations.put(location.getId(), location);
    }

    @Override
    public Location getLocationById(long id) {
        return locations.get(id);
    }

    @Override
    public void updateLocation(Location location) {
        locations.put(location.getId(), location);
    }

    @Override
    public void deleteLocation(long id) {
        locations.remove(id);
    }
}
