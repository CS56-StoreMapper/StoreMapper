package com.example.model;

/**
 * Represents a location with an ID, name, and coordinates.
 */
public class Location {
    /** The unique identifier for the location. */
    private long id;
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

    /**
     * Gets the ID of the location.
     *
     * @return The location's ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the name of the location.
     *
     * @return The location's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the coordinates of the location.
     *
     * @return The location's coordinates.
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Returns a string representation of the location.
     *
     * @return A string representation of the location.
     */
    @Override
    public String toString() {
        return String.format("Location{id=%d, name='%s', coordinates=%s}",
                id, name, coordinates);
    }
}
