package com.example.util;

import com.example.model.*;

public final class LocationFactory {
    private LocationFactory() {}
    
    public static Location createLocation(LocationType type, long id, String name, Coordinates coordinates) {
        return switch (type) {
            case STORE -> new Store(id, name, coordinates);
            case RESTAURANT -> new Restaurant(id, name, coordinates);
            default -> throw new IllegalArgumentException("Unknown location type: " + type);
        };
    }

    public static Location createLocation(LocationType type, long id, String name, double latitude, double longitude) {
        return createLocation(type, id, name, new Coordinates(latitude, longitude));
    }

    public static Store createStore(long id, String name, Coordinates coordinates) {
        return (Store) createLocation(LocationType.STORE, id, name, coordinates);
    }

    public static Restaurant createRestaurant(long id, String name, Coordinates coordinates) {
        return (Restaurant) createLocation(LocationType.RESTAURANT, id, name, coordinates);
    }
}