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

    @Override
    public LocationType getType() {
        return LocationType.STORE;
    }

    @Override
    public String toString() {
        return "Store{" + super.toString() + "}";
    }
}