package com.example.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.example.model.Location;
import com.example.model.Coordinates;
import com.example.model.Graph;

public interface LocationService {

    /**
     * Retrieves all locations.
     *
     * @return A list of all locations.
     */
    List<Location> getAllLocations();

    /**
     * Searches for locations based on a query string.
     *
     * @param query The search query.
     * @return A list of locations matching the query.
     */
    List<Location> searchLocations(String query);

    /**
     * Searches for locations based on an OSM tag.
     *
     * @param key The OSM tag key.
     * @param value The OSM tag value.
     * @return A list of locations with the specified OSM tag.
     */
    List<Location> searchLocationsByOsmTag(String key, String value);

    /**
     * Finds the nearest location to the given coordinates.
     *
     * @param coordinates The coordinates to search from.
     * @param filter An optional filter predicate for locations.
     * @return The nearest location, if any.
     */
    Optional<Location> findNearestLocation(Coordinates coordinates, Predicate<Location> filter);

    /**
     * Finds locations within a specified radius of the given coordinates.
     *
     * @param coordinates The center coordinates.
     * @param radiusKm The radius in kilometers.
     * @param filter An optional filter predicate for locations.
     * @return A list of locations within the specified radius.
     */
    List<Location> findLocationsWithinRadius(Coordinates coordinates, double radiusKm, Predicate<Location> filter);

    /**
     * Adds a new location.
     *
     * @param location The location to be added.
     */
    void addLocation(Location location);

    /**
     * Retrieves a location by its ID.
     *
     * @param id The ID of the location.
     * @return The location with the specified ID, or null if not found.
     */
    Location getLocationById(long id);

    /**
     * Updates an existing location.
     *
     * @param location The location to be updated.
     */
    void updateLocation(Location location);

    /**
     * Deletes a location by its ID.
     *
     * @param id The ID of the location to be deleted.
     */
    void deleteLocation(long id);

    /**
     * Gets the underlying graph.
     *
     * @return The graph used by this service.
     */
    Graph getGraph();
}