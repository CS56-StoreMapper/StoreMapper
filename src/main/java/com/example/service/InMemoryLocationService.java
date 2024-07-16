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
    private static final long MEMORY_THRESHOLD = Runtime.getRuntime().maxMemory() / 4; // 25% of max heap
    private static final boolean USE_CHUNKING = Boolean.getBoolean("app.use.chunking");
    
    
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
        long startTime = System.currentTimeMillis();
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        logger.info("Maximum heap size before initializing locations: " + (maxHeapSize / (1024 * 1024)) + " MB");
        
        logger.info("Initializing locations from graph");
        List<Node> allNodes = graph.getNodes();
        int totalNodes = allNodes.size();
        logger.info("Total nodes to process: " + totalNodes);
 
        if (USE_CHUNKING) {
            logger.info("Processing nodes in chunks");
            processInChunks(allNodes, totalNodes);
        } else {
            logger.info("Processing all nodes at once");
            processAllAtOnce(allNodes, totalNodes);
        }
 
        long endTime = System.currentTimeMillis();
        logger.info("Initialized " + locations.size() + " locations in " + (endTime - startTime) + " ms");
    }

    private void processInChunks(List<Node> allNodes, int totalNodes) {
        int processedNodes = 0;
        while (processedNodes < totalNodes && shouldContinueProcessing()) {
            int chunkSize = calculateChunkSize();
            int end = Math.min(processedNodes + chunkSize, totalNodes);
            List<Node> chunk = allNodes.subList(processedNodes, end);
            processNodeChunk(chunk);
            processedNodes = end;
            logger.info("Processed " + processedNodes + " out of " + totalNodes + " nodes");
            logMemoryUsage();
            
            if (isLowMemory()) {
                logger.info("Low memory detected. Triggering garbage collection.");
                compactLocationMap();
                System.gc();
                try {
                    Thread.sleep(1000); // Give GC some time to work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void compactLocationMap() {
        Map<Long, Location> compactMap = new HashMap<>(locations);
        locations.clear();
        locations.putAll(compactMap);
        logger.info("Compacted location map. New size: " + locations.size());
    }

    private int calculateChunkSize() {
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (int) Math.max(10000, Math.min(freeMemory / 100, 100000));
    }

    private boolean shouldContinueProcessing() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return usedMemory < runtime.maxMemory() * 0.75; // Continue if less than 75% memory used
    }

    private void processAllAtOnce(List<Node> allNodes, int totalNodes) {
        processNodeChunk(allNodes);
        logger.info("Processed all " + totalNodes + " nodes at once");
    }

    private void processNodeChunk(List<Node> nodes) {
        long memoryPerNode = estimateMemoryPerNode();
        for (Node node : nodes) {
            if (Runtime.getRuntime().freeMemory() < memoryPerNode * 2) {
                clearCacheIfNeeded();
            }
            try {
                Location location;
                if ("store".equals(node.tags().get("type"))) {
                    location = new Store(node.id(), node.lat(), node.lon(), node);
                } else {
                    location = new Restaurant(node.id(), node.lat(), node.lon(), node);
                }
                locations.put(node.id(), location);
            } catch (Exception e) {
                logger.warning("Error processing node " + node.id() + ": " + e.getMessage());
            }
        }
    }

    private long estimateMemoryPerNode() {
        if (locations.isEmpty()) return 0;
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return usedMemory / locations.size();
    }

    private void clearCacheIfNeeded() {
        if (isLowMemory()) {
            logger.info("Low memory detected. Clearing location cache.");
            locations.clear();
            System.gc();
        }
    }    

    private void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        logger.info("Memory Usage: " +
                    "Total: " + (totalMemory / 1048576) + " MB, " +
                    "Free: " + (freeMemory / 1048576) + " MB, " +
                    "Max: " + (maxMemory / 1048576) + " MB, " +
                    "Used: " + ((totalMemory - freeMemory) / 1048576) + " MB");
    }

    private boolean isLowMemory() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long availableMemory = maxMemory - usedMemory;
        
        logger.info("Maximum heap size: " + (maxMemory / (1024 * 1024)) + " MB");
        logger.info("Total memory: " + (totalMemory / (1024 * 1024)) + " MB");
        logger.info("Free memory: " + (freeMemory / (1024 * 1024)) + " MB");
        logger.info("Used memory: " + (usedMemory / (1024 * 1024)) + " MB");
        logger.info("Available memory: " + (availableMemory / (1024 * 1024)) + " MB");
        
        return availableMemory < MEMORY_THRESHOLD;
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
