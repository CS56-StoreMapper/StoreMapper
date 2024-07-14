package com.example.util;

import com.example.model.*;

public class LocationFactory {
    public static Location createLocation(LocationType type, long id, String name, Coordinates coordinates) {
        switch (type) {
            case STORE:
                return new Store(id, name, coordinates);
            case RESTAURANT:
                return new Restaurant(id, name, coordinates);
            default:
                throw new IllegalArgumentException("Unknown location type: " + type);
        }
    }

    // Overloaded method for creating a location with latitude and longitude instead of Coordinates object
    public static Location createLocation(LocationType type, long id, String name, double latitude, double longitude) {
        Coordinates coordinates = new Coordinates(latitude, longitude);
        return createLocation(type, id, name, coordinates);
    }

    // You can add more factory methods here if needed, for example:
    public static Store createStore(long id, String name, Coordinates coordinates) {
        return (Store) createLocation(LocationType.STORE, id, name, coordinates);
    }

    public static Restaurant createRestaurant(long id, String name, Coordinates coordinates) {
        return (Restaurant) createLocation(LocationType.RESTAURANT, id, name, coordinates);
    }
}
