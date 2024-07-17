package com.example.model;

public final class Store extends Location {
    public Store(long id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    public Store(long id, String name, double latitude, double longitude) {
        super(id, name, latitude, longitude);
    }

    public Store(long id, String name, Node osmNode) {
        super(id, name, osmNode);
    }

    public Store(long id, double latitude, double longitude, Node osmNode) {
        super(id, "Store " + id, new Coordinates(latitude, longitude), osmNode);
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