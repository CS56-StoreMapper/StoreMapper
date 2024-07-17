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
        return switch (type) {
            case STORE -> new Store(id, name, latitude, longitude);
            case RESTAURANT -> new Restaurant(id, name, latitude, longitude);
            default -> throw new IllegalArgumentException("Unknown location type: " + type);
        };
    }

    public static Location createLocation(LocationType type, long id, String name, Node osmNode) {
        return switch (type) {
            case STORE -> new Store(id, name, osmNode);
            case RESTAURANT -> new Restaurant(id, name, osmNode);
            default -> throw new IllegalArgumentException("Unknown location type: " + type);
        };
    }

    public static Store createStore(long id, String name, Coordinates coordinates) {
        return new Store(id, name, coordinates);
    }

    public static Store createStore(long id, String name, double latitude, double longitude) {
        return new Store(id, name, latitude, longitude);
    }

    public static Store createStore(long id, String name, Node osmNode) {
        return new Store(id, name, osmNode);
    }

    public static Restaurant createRestaurant(long id, String name, Coordinates coordinates) {
        return new Restaurant(id, name, coordinates);
    }

    public static Restaurant createRestaurant(long id, String name, double latitude, double longitude) {
        return new Restaurant(id, name, latitude, longitude);
    }

    public static Restaurant createRestaurant(long id, String name, Node osmNode) {
        return new Restaurant(id, name, osmNode);
    }
}