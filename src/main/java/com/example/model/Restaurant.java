package com.example.model;

import java.util.Map;

public final class Restaurant extends Location {
    public Restaurant(long id, Coordinates coordinates, Node osmNode) {
        super(id, coordinates, osmNode);
    }

    public Restaurant(long id, double latitude, double longitude, Node osmNode) {
        super(id, latitude, longitude, osmNode);
    }

    public Restaurant(long id, Coordinates coordinates) {
        this(id, coordinates, createDefaultNode(id, coordinates, "Restaurant"));
    }

    public Restaurant(long id, double latitude, double longitude) {
        this(id, new Coordinates(latitude, longitude));
    }

    private static Node createDefaultNode(long id, Coordinates coordinates, String type) {
        return new Node(id, coordinates.getLatitude(), coordinates.getLongitude(), 
                        Map.of("name", type + " " + id, "type", type.toLowerCase()));
    }

    @Override
    public LocationType getType() {
        return LocationType.RESTAURANT;
    }

    @Override
    public String toString() {
        return "Restaurant{" + super.toString() + "}";
    }
}