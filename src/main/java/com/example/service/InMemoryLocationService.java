package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.model.Location;

public class InMemoryLocationService implements LocationService {
    private Map<Long, Location> locations;

    public InMemoryLocationService() {
        locations = new HashMap<>();
    }

    @Override
    public List<Location> getAllLocations() {
        return new ArrayList<>(locations.values());
    }

    @Override
    public List<Location> searchLocations(String query) {
        // To be implemented
        return null;
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
        // To be implemented
    }

    @Override
    public void deleteLocation(long id) {
        // To be implemented
    }
}
