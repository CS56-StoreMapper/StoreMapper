package com.example.service;

import java.util.Comparator;
import java.util.List;
import com.example.model.Coordinates;
import com.example.model.Location;
import com.example.model.Route;
import com.example.model.Node;
import com.example.model.Graph;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Service class for map-related operations.
 * This class provides methods for finding locations, calculating routes,
 * and performing spatial queries on the set of locations.
 */
public final class MapService {

    private final LocationService locationService;
    private final Graph graph;

    /**
     * Constructs a new MapService.
     *
     * @param locationService The location service to use for retrieving location data.
     * @param routeStrategy The route strategy to use for calculating routes.
     */
    public MapService(final LocationService locationService, final Graph graph) {
        this.locationService = locationService;
        this.graph = graph;
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
        return locationService.getAllLocations().stream()
                .filter(filter)
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
        Node startNode = findNearestGraphNode(start.getCoordinates());
        Node endNode = findNearestGraphNode(end.getCoordinates());
        List<Node> path = graph.findShortestPath(startNode, endNode);
        return new Route(path);
    }

    public Route calculateRoute(final Coordinates start, final Coordinates end) {
        Node startNode = findNearestGraphNode(start);
        Node endNode = findNearestGraphNode(end);
        List<Node> path = graph.findShortestPath(startNode, endNode);
        return new Route(path);
    }

    private Node findNearestGraphNode(Coordinates coordinates) {
        return graph.getNodes().stream()
                .min(Comparator.comparingDouble(node -> 
                    coordinates.distanceTo(new Coordinates(node.lat(), node.lon()))))
                .orElseThrow(() -> new IllegalStateException("No nodes in graph"));
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
        return this.findLocationsWithinRadius(center, radiusKm).stream()
                .filter(location -> location.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    
}