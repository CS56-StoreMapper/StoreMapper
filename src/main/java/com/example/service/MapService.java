package com.example.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.example.model.Coordinates;
import com.example.model.Location;
import com.example.model.Route;
import com.example.web.LocationServlet;
import com.example.model.Node;
import com.example.model.Graph;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;


/**
 * Service class for map-related operations.
 * This class provides methods for finding locations, calculating routes,
 * and performing spatial queries on the set of locations.
 */
public final class MapService {

    private final LocationService locationService;
    private final Graph graph;
    private final RouteStrategy routeStrategy;

    private static final double MAX_DISTANCE_KM = 5.0; // Maximum distance to consider a point reachable

    private static final Logger logger = Logger.getLogger(LocationServlet.class.getName());

    /**
     * Constructs a new MapService with a specified RouteStrategy.
     *G
     * @param locationService The location service to use for retrieving location data.
     * @param graph The graph representing the road network.
     * @param routeStrategy The route strategy to use for calculating routes.
     */
    public MapService(final LocationService locationService, final Graph graph, final RouteStrategy routeStrategy) {
        this.locationService = Objects.requireNonNull(locationService, "LocationService must not be null");
        this.graph = Objects.requireNonNull(graph, "Graph must not be null");
        this.routeStrategy = Objects.requireNonNull(routeStrategy, "RouteStrategy must not be null");
    }

    /**
     * Constructs a new MapService with a default DijkstraRouteStrategy.
     *
     * @param locationService The location service to use for retrieving location data.
     * @param graph The graph representing the road network.
     */
    public MapService(final LocationService locationService, final Graph graph) {
        this(locationService, graph, new DijkstraRouteStrategy(graph));
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
        return calculateRoute(start.getCoordinates(), end.getCoordinates());
    }

    public Route calculateRoute(final Coordinates start, final Coordinates end) {
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

        Route route = routeStrategy.calculateRoute(startNode, endNode);
        if (route == null) {
            System.out.println("No route found between " + start + " and " + end);
            return null;
        }

        System.out.println("Route found: " + route.getNodes().size() + " nodes, start: " + route.getNodes().get(0) + ", end: " + route.getNodes().get(route.getNodes().size() - 1));
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
     * @param center The center point of the search area.
     * @param radiusKm The radius to search within, in kilometers.
     * @return A list of locations that match the criteria.
     * @throws IllegalArgumentException if radiusKm is negative.
     */
    public List<Location> searchLocationsWithinRadiusAndKeyword(String query, Coordinates center, double radiusKm) throws IllegalArgumentException{
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        List<Location> locationsWithinRadius = this.findLocationsWithinRadius(center, radiusKm);
        logger.info("Found " + locationsWithinRadius.size() + " locations within radius");
        List<Location> matchingLocations = locationsWithinRadius.stream()
                .filter(location -> {
                    boolean nameContains = location.getName().toLowerCase().contains(query.toLowerCase());
                    logger.info("Location " + location.getName() + " contains keyword " + query + ": " + nameContains);
                    boolean osmTagContains = location.getOsmTag("name").map(tag -> tag.toLowerCase().contains(query.toLowerCase())).orElse(false);
                    logger.info("Location " + location.getName() + " contains keyword " + query + ": " + osmTagContains);
                    return nameContains || osmTagContains;
                })
                .toList();

        logger.info("Found " + matchingLocations.size() + " locations matching query: " + query);
        return matchingLocations;
    }

    public static class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }
}