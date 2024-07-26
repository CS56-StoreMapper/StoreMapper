package com.example.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.example.model.Bounds;
import com.example.model.Coordinates;
import com.example.model.Location;
import com.example.model.Route;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.model.Node;
import com.example.model.Graph;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import com.example.util.MemoryUtil;

/**
 * Service class for map-related operations.
 * This class provides methods for finding locations, calculating routes,
 * and performing spatial queries on the set of locations.
 */
public final class MapService {
    private static final Logger logger = Logger.getLogger(MapService.class.getName());
    private static final boolean IS_TEST_ENVIRONMENT = System.getProperty("maven.test") != null;
    private static final boolean BOUNDS_CHECKING_ENABLED = !IS_TEST_ENVIRONMENT;
    private static final Bounds WEST_LA_BOUNDS = loadBounds();

    private final LocationService locationService;
    private final Graph graph;
    private final RouteStrategy shortestRouteStrategy;
    private final RouteStrategy fastestRouteStrategy;

    private static final double MAX_DISTANCE_KM = 5.0; // Maximum distance to consider a point reachable

    private static Bounds loadBounds() {
        if (IS_TEST_ENVIRONMENT) {
            logger.info("Test environment detected. Using default bounds.");
            return new Bounds(-90, -180, 90, 180); // Whole world bounds for testing
        }

        try {
            logger.info("Attempting to read bounds from file...");
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = MapService.class.getResourceAsStream("/prod_data/west-la.bounds.json");
            if (is == null) {
                throw new IOException("Bounds file not found in classpath");
            }
            List<Bounds> boundsList = mapper.readValue(is, new TypeReference<List<Bounds>>() {});
            logger.info("Bounds loaded successfully: " + boundsList.get(0));
            return boundsList.get(0);
        } catch (IOException e) {
            logger.severe("Failed to load bounds: " + e.getMessage());
            logger.info("Using default West LA bounds");
            return new Bounds(33.965, -118.5129999, 34.07, -118.3849999); // Default West LA bounds
        }
    }

    /**
     * Constructs a new MapService with specified RouteStrategies.
     *
     * @param locationService The location service to use for retrieving location data.
     * @param graph The graph representing the road network.
     * @param shortestRouteStrategy The route strategy to use for calculating shortest routes.
     * @param fastestRouteStrategy The route strategy to use for calculating fastest routes.
     */
    public MapService(final LocationService locationService, 
                      final Graph graph, 
                      final RouteStrategy shortestRouteStrategy,
                      final RouteStrategy fastestRouteStrategy) {
        this.locationService = Objects.requireNonNull(locationService, "LocationService must not be null");
        this.graph = Objects.requireNonNull(graph, "Graph must not be null");
        this.shortestRouteStrategy = Objects.requireNonNull(shortestRouteStrategy, "ShortestRouteStrategy must not be null");
        this.fastestRouteStrategy = Objects.requireNonNull(fastestRouteStrategy, "FastestRouteStrategy must not be null");
    }

    /**
     * Constructs a new MapService with default DijkstraRouteStrategy for shortest paths
     * and FastestRouteStrategy for fastest paths.
     *
     * @param locationService The location service to use for retrieving location data.
     * @param graph The graph representing the road network.
     */
    public MapService(final LocationService locationService, final Graph graph) {
        this(locationService, 
             graph, 
             new DijkstraRouteStrategy(graph),
             new FastestRouteStrategy(graph));
    }

    /**
     * Finds the nearest location to a given point.
     *
     * @param point The coordinates to search from.
     * @return An Optional containing the nearest location, or empty if no locations are available.
     */
    public Optional<Location> findNearestLocation(final Coordinates point) {
        return findNearestLocation(point, location -> true);
    }

    /**
     * Finds the nearest location to a given point that satisfies a specified condition.
     *
     * @param point The coordinates to search from.
     * @param filter A predicate to filter locations.
     * @return An Optional containing the nearest location that satisfies the filter, or empty if none found.
     */
    public Optional<Location> findNearestLocation(Coordinates point, Predicate<Location> filter) {
        List<Location> locations = locationService.getAllLocations();
        if (locations == null) {
            return Optional.empty();
        }
        return locations.stream()
                .filter(Objects::nonNull)
                .filter(filter != null ? filter : location -> true)
                .min((loc1, loc2) -> Double.compare(
                    loc1.getCoordinates().distanceTo(point),
                    loc2.getCoordinates().distanceTo(point)
                ));
    }


    /**
     * Finds locations with a specific OSM tag key-value pair.
     *
     * @param key The OSM tag key to search for.
     * @param value The OSM tag value to match.
     * @return A list of locations that have the specified OSM tag.
     */
    public List<Location> findLocationsByOsmTag(String key, String value) {
        return locationService.getAllLocations().stream()
                .filter(location -> location.getOsmTag(key)
                        .map(tag -> tag.equals(value))
                        .orElse(false))
                        .toList();
    }

    /**
     * Finds locations within a specified radius of a point.
     *
     * @param point The center point to search from.
     * @param radiusKm The radius to search within, in kilometers.
     * @return A list of locations within the specified radius.
     * @throws IllegalArgumentException if radiusKm is negative.
     */
    public List<Location> findLocationsWithinRadius(final Coordinates point, final double radiusKm) {
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        return locationService.getAllLocations().stream()
                .filter(location -> location.getCoordinates().distanceTo(point) <= radiusKm)
                .toList();
    }

    public Route calculateRoute(final Location start, final Location end) {
        return calculateShortestRoute(start.getCoordinates(), end.getCoordinates());
    }

    public Route calculateShortestRoute(final Coordinates start, final Coordinates end) {
        Node startNode = graph.findNearestRelevantNode(start);
        Node endNode = graph.findNearestRelevantNode(end);

        System.out.println("Nearest relevant node to " + start + " is " + startNode);
        System.out.println("Nearest relevant node to " + end + " is " + endNode);

        if (startNode == null || endNode == null) {
            System.out.println("No route possible: start or end nodes not found");
            return null;
        }


        if (startNode.equals(endNode)) {
            System.out.println("Start and end nodes are the same");
            return null;
        }

        // Check if end point is too far from the nearest node
        if (end.distanceTo(new Coordinates(endNode.lat(), endNode.lon())) > MAX_DISTANCE_KM) {
            System.out.println("End point is too far from the nearest node");
            return null;
        }

        Route route = shortestRouteStrategy.calculateRoute(startNode, endNode);
        if (route == null) {
            System.out.println("No route found between " + start + " and " + end);
            return null;
        }

        System.out.println("Route found: " + route.getNodes().size() + " nodes, start: " + route.getNodes().get(0) + ", end: " + route.getNodes().get(route.getNodes().size() - 1));
        return route;

    }

    public Route calculateFastestRoute(final Coordinates start, final Coordinates end) {
        Node startNode = graph.findNearestRelevantNode(start);
        Node endNode = graph.findNearestRelevantNode(end);

        if (startNode == null || endNode == null) {
            logger.warning("No route possible: start or end nodes not found");
            return null;
        }

        if (startNode.equals(endNode)) {
            logger.info("Start and end nodes are the same");
            return null;
        }

        // Check if end point is too far from the nearest node
        double endNodeDistance = end.distanceTo(new Coordinates(endNode.lat(), endNode.lon()));
        if (endNodeDistance > MAX_DISTANCE_KM) {
            logger.warning("End point is too far from the nearest node: " + endNodeDistance + " km");
            return null;
        }

        Route route = fastestRouteStrategy.calculateRoute(startNode, endNode);
        if (route == null || route.getNodes().isEmpty()) {
            logger.warning("No route found between " + start + " and " + end);
            return null;
        }

        logger.info("Fastest route found: " + route.getNodes().size() + " nodes, start: " + route.getNodes().get(0) + ", end: " + route.getNodes().get(route.getNodes().size() - 1));
        logger.info("Memory usage after calculating fastest route: " + MemoryUtil.getMemoryUsage());
        return route;
    }

    private Node findNearestGraphNode(Coordinates coordinates) {
        Node nearest = graph.getNodes().stream()
                .min(Comparator.comparingDouble(node -> 
                    coordinates.distanceTo(new Coordinates(node.lat(), node.lon()))))
                .orElseThrow(() -> new IllegalStateException("No nodes in graph"));
        System.out.println("Nearest node to " + coordinates + " is " + nearest);
        return nearest;
    }

    private Node locationToNode(Location location) {
        if (location.getOsmNode().isPresent()) {
            return location.getOsmNode().get();
        }
        return findNearestGraphNode(location.getCoordinates());
    }

    public Location nodeToLocation(Node node) {
        // This method assumes that you want to create a generic Location from a Node, which we can't
        // So this is a placeholder
        return null;
    }


    public int getNodeCount() {
        return graph.getNodeCount();
    }

    /**
     * Gets the count of ways in the graph
     * This count includes only original ways, not reverse ways created for bidirectional streets
     */
    public int getWayCount() {
        return graph.getWayCount();
    }

    /**
     * Searches for locations within a specified radius of a point that match a given keyword.
     *
     * @param query The search keyword.
     * @param category The category to search within (empty string for all categories).
     * @param type The type to search within (empty string for all types).
     * @param center The center point of the search area.
     * @param radiusKm The radius to search within, in kilometers.
     * @return A list of locations that match the criteria.
     * @throws IllegalArgumentException if radiusKm is negative.
     */
    public List<Location> searchLocationsWithinRadiusAndKeyword(String query, String category, String type, Coordinates center, double radiusKm) throws IllegalArgumentException {
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }

        if (BOUNDS_CHECKING_ENABLED && !WEST_LA_BOUNDS.contains(center)) {
            logger.warning("Search center is outside of available data bounds. Adjusting to nearest point within bounds.");
            center = adjustToBounds(center);
        }

        List<Location> locationsWithinRadius = this.findLocationsWithinRadius(center, radiusKm);
        logger.info("Found " + locationsWithinRadius.size() + " locations within radius");
        
        String lowercaseQuery = query.toLowerCase();
        String lowercaseType = type.toLowerCase();
        
        List<Location> matchingLocations = locationsWithinRadius.stream()
                .filter(location -> {
                    // Category filter
                    if (!category.isEmpty()) {
                        if ("restaurant".equals(category) && !location.isRestaurant()) {
                            return false;
                        }
                        if ("store".equals(category) && !location.isStore()) {
                            return false;
                        }
                    }
                    
                    // Type filter
                    if (!type.isEmpty()) {
                        if ("restaurant".equals(category) && !location.getCuisine().toLowerCase().contains(lowercaseType)) {
                            return false;
                        }
                        if ("store".equals(category) && !location.getShop().toLowerCase().contains(lowercaseType)) {
                            return false;
                        }
                    }
                    
                    // Keyword filter (only if query is not empty)
                    if (!query.isEmpty()) {
                        boolean nameMatches = location.getName().toLowerCase().contains(lowercaseQuery);
                        boolean amenityMatches = location.getAmenity().toLowerCase().contains(lowercaseQuery);
                        boolean brandMatches = location.getBrand().toLowerCase().contains(lowercaseQuery);
                        boolean addressMatches = location.getAddress().toLowerCase().contains(lowercaseQuery);
                        
                        return nameMatches || amenityMatches || brandMatches || addressMatches;
                    }
                    
                    // If we've made it this far and the query is empty, include this location
                    return true;
                })
                .toList();

        logger.info("Found " + matchingLocations.size() + " locations matching criteria");
        return matchingLocations;
    }

    public Coordinates adjustToBounds(Coordinates point) {
        if (!BOUNDS_CHECKING_ENABLED) {
            return point;
        }
        double lat = Math.max(WEST_LA_BOUNDS.minlat(), Math.min(WEST_LA_BOUNDS.maxlat(), point.getLatitude()));
        double lon = Math.max(WEST_LA_BOUNDS.minlon(), Math.min(WEST_LA_BOUNDS.maxlon(), point.getLongitude()));
        return new Coordinates(lat, lon);
    }

    public static class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }
}