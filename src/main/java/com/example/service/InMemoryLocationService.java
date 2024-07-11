package com.example.service;

import java.util.ArrayList;
import java.util.List;
import com.example.model.Location;

public class InMemoryLocationService implements LocationService {
    private List<Location> locations;

    public InMemoryLocationService() {
        locations = new ArrayList<>();
    }

    @Override
    public List<Location> getAllLocations() {
        return locations;
    }

    @Override
    public List<Location> searchLocations(String query) {
        // To be implemented
        return null;
    }

    @Override
    public void addLocation(Location location) {
        locations.add(location);
    }

    @Override
    public Location getLocationById(long id) {
        // To be implemented
        return null;
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
