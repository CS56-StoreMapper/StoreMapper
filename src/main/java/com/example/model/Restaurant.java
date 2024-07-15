package com.example.model;

public final class Restaurant extends Location {

    public Restaurant(long id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    @Override
    public LocationType getType() {
        return LocationType.RESTAURANT;
    }
}
