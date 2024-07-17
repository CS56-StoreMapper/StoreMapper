package com.example.model;

import java.util.Map;

public final class Store extends Location {
    public Store(long id, Coordinates coordinates, Node osmNode) {
        super(id, coordinates, osmNode);
    }

    public Store(long id, double latitude, double longitude, Node osmNode) {
        super(id, latitude, longitude, osmNode);
    }

    public Store(long id, Coordinates coordinates) {
        this(id, coordinates, createDefaultNode(id, coordinates, "Store"));
    }

    public Store(long id, double latitude, double longitude) {
        this(id, new Coordinates(latitude, longitude));
    }

    private static Node createDefaultNode(long id, Coordinates coordinates, String type) {
        return new Node(id, coordinates.getLatitude(), coordinates.getLongitude(), 
                        Map.of("name", type + " " + id, "type", type.toLowerCase()));
    }

    @Override
    public LocationType getType() {
        return LocationType.STORE;
    }

    @Override
    public String toString() {
        return "Store{" + super.toString() + "}";
    }
}