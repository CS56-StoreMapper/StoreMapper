package com.example.service;

import java.util.List;
import com.example.model.Coordinates;
import com.example.model.Location;
import com.example.model.Route;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Service class for map-related operations.
 * This class provides methods for finding locations, calculating routes,
 * and performing spatial queries on the set of locations.
 */
public final class MapService {

    private final LocationService locationService;
    private final RouteStrategy routeStrategy;

    /**
     * Constructs a new MapService.
     *
     * @param locationService The location service to use.
     * @param routeStrategy The route strategy to use.
     */
    public MapService(final LocationService locationService, final RouteStrategy routeStrategy) {
        this.locationService = locationService;
        this.routeStrategy = routeStrategy;
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

    /**
     * Calculates a route between two points.
     *
     * @param start The starting coordinates.
     * @param end The ending coordinates.
     * @return The calculated route.
     */
    public Route calculateRoute(final Coordinates start, final Coordinates end) {
        return routeStrategy.calculateRoute(start, end);
    }

    /**
     * Calculates a route between two locations.
     *
     * @param start The starting location.
     * @param end The ending location.
     * @return The calculated route.
     */
    public Route calculateRoute(final Location start, final Location end) {
        return calculateRoute(start.getCoordinates(), end.getCoordinates());
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