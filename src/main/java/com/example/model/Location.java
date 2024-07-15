package com.example.model;

import com.example.util.DistanceUtil;

import java.util.Objects;

/**
 * Represents a location with an ID, name, and coordinates.
 */
public abstract sealed class Location implements Comparable<Location> permits Store, Restaurant {
    /** The unique identifier for the location. */
    private final long id;
    /** The name of the location. */
    private String name;
    /** The coordinates of the location. */
    private Coordinates coordinates;

    /**
     * Constructs a new Location object.
     *
     * @param id          The unique identifier for the location.
     * @param name        The name of the location.
     * @param coordinates The coordinates of the location.
     */
    public Location(long id, String name, Coordinates coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public Location(long id, String name, double latitude, double longitude) {
        this(id, name, new Coordinates(latitude, longitude));
    }

     // Getters (no setters to maintain immutability)
    public long getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }

    public abstract LocationType getType();

    @Override
    public int compareTo(Location other) {
        return Long.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location otherLocation)) return false;
        return id == otherLocation.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    /**
     * Calculates the distance to another location in kilometers.
     *
     * @param other The other location to calculate the distance to.
     * @return The distance in kilometers.
     */
    public double distanceTo(Location other) {
        return this.coordinates.distanceTo(other.getCoordinates());
    }

    /**
     * Calculates the distance to another location in miles.
     *
     * @param other The other location to calculate the distance to.
     * @return The distance in miles.
     */
    public double distanceToInMiles(Location other) {
        return DistanceUtil.kmToMiles(this.distanceTo(other));
    }

    /**
     * Returns a string representation of the location.
     *
     * @return A string representation of the location.
     */
    @Override
    public String toString() {
        return String.format("Location{id=%d, name='%s', coordinates=%s, type=%s}",
                id, name, coordinates, getType());
    }
}
