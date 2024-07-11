package com.example.service;

import java.util.List;
import com.example.model.Location;

/**
 * Service interface for managing location data.
 */
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
}