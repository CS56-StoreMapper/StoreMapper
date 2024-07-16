package com.example.model;

public final class Restaurant extends Location {

    public Restaurant(long id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    public Restaurant(long id, String name, double latitude, double longitude) {
        super(id, name, latitude, longitude);
    }

    public Restaurant(long id, String name, Node osmNode) {
        super(id, name, osmNode);
    }

    public Restaurant(long id, double latitude, double longitude, Node osmNode) {
        super(id, "Restaurant " + id, new Coordinates(latitude, longitude), osmNode);
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