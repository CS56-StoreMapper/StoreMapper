package com.example.model;

import com.example.util.DistanceUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a location with an ID, name, and coordinates.
 */
public abstract sealed class Location implements Comparable<Location> permits Store, Restaurant {
    /** The unique identifier for the location. */
    private final long id;
    /** The name of the location. */
    private final String name;
    /** The coordinates of the location. */
    private final Coordinates coordinates;
    private final Node osmNode; // Optional OSM node data

    public Location(long id, String name, Coordinates coordinates) {
        this(id, name, coordinates, null);
    }

    public Location(long id, String name, double latitude, double longitude) {
        this(id, name, new Coordinates(latitude, longitude), null);
    }

    public Location(long id, String name, Node osmNode) {
        this(id, name, new Coordinates(osmNode.lat(), osmNode.lon()), osmNode);
    }

    private Location(long id, String name, Coordinates coordinates, Node osmNode) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.osmNode = osmNode;
    }

     // Getters (no setters to maintain immutability)
    public long getId() { return id; }
    public String getName() { return name; }
    
    public Coordinates getCoordinates() {
        return osmNode != null ? new Coordinates(osmNode.lat(), osmNode.lon()) : coordinates;
    }
    
    public Optional<Node> getOsmNode() {
        return Optional.ofNullable(osmNode);
    }

    public Optional<String> getOsmTag(String key) {
        return Optional.ofNullable(osmNode).flatMap(node -> Optional.ofNullable(node.tags().get(key)));
    }

    public Map<String, String> getOsmTags() {
        return osmNode != null ? osmNode.tags() : Map.of();
    }

    public abstract LocationType getType();

    @Override
    public int compareTo(Location other) {
        return Long.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location that)) return false;
        return id == that.id && 
            Objects.equals(osmNode, that.osmNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, osmNode);
    }

    public boolean isOsmBased() {
        return osmNode != null;
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
        return String.format("Location{id=%d, name='%s', coordinates=%s, type=%s, osmId=%s}",
                id, name, coordinates, getType(), 
                osmNode != null ? osmNode.id() : "N/A");
    }
}
