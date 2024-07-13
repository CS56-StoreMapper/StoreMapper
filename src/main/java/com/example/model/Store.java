package com.example.model;

public class Store extends Location {
    public Store(long id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    @Override
    public LocationType getType() {
        return LocationType.STORE;
    }
}
