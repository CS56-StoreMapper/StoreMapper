package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Predicate;

import com.example.model.Coordinates;
import com.example.model.Graph;
import com.example.model.Location;
import com.example.model.Restaurant;
import com.example.model.Store;
import com.example.util.TestDataGenerator;
import com.example.model.Node;
import java.util.logging.Logger;

public class InMemoryLocationService implements LocationService {
    private static final Logger logger = Logger.getLogger(InMemoryLocationService.class.getName());
    
    private final Map<Long, Location> locations;
    private final Graph graph;

    public InMemoryLocationService(int nodeCount) {
        this.graph = TestDataGenerator.generateTestGraph(nodeCount);
        this.locations = new HashMap<>();
        initializeLocationsFromGraph();
    }

    public InMemoryLocationService(Graph graph) {
        this.graph = graph;
        this.locations = new HashMap<>();
        initializeLocationsFromGraph();
    }

    private void initializeLocationsFromGraph() {
        logger.info("Initializing locations from graph");
        for (Node node : graph.getNodes()) {
            Location location;
            if ("store".equals(node.tags().get("type"))) {
                location = new Store(node.id(), node.lat(), node.lon(), node);
            } else {
                location = new Restaurant(node.id(), node.lat(), node.lon(), node);
            }
            locations.put(node.id(), location);
        }
        logger.info("Initialized " + locations.size() + " locations");
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
    public Optional<Location> findNearestLocation(Coordinates coordinates, Predicate<Location> filter) {
        return locations.values().stream()
            .filter(filter)
            .min((loc1, loc2) -> Double.compare(
                coordinates.distanceTo(loc1.getCoordinates()),
                coordinates.distanceTo(loc2.getCoordinates())
            ));
    }

    @Override
    public List<Location> findLocationsWithinRadius(Coordinates coordinates, double radiusKm, Predicate<Location> filter) {
        return locations.values().stream()
                .filter(location -> coordinates.distanceTo(location.getCoordinates()) <= radiusKm)
                .filter(filter)
                .collect(Collectors.toList());
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
    @Override
    public Graph getGraph() {
        return this.graph;
    }
}
