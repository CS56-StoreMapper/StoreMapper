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
    /** The coordinates of the location. */
    private final Coordinates coordinates;
    private final Node osmNode; // Optional OSM node data


    public Location(long id, double latitude, double longitude, Node osmNode) {
        this(id, new Coordinates(latitude, longitude), osmNode);
    }

    public Location(long id, Node osmNode) {
        this(id, new Coordinates(osmNode.lat(), osmNode.lon()), osmNode);
    }

    public Location(long id, Coordinates coordinates, Node osmNode) {
        this.id = id;
        this.coordinates = coordinates;
        this.osmNode = osmNode;
    }

     // Getters (no setters to maintain immutability)
    public long getId() { return id; }    

    public double getLatitude() {
        return osmNode != null ? osmNode.lat() : coordinates.getLatitude();
    }

    public double getLongitude() {
        return osmNode != null ? osmNode.lon() : coordinates.getLongitude();
    }

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

    public boolean isRestaurant() {
        return "restaurant".equalsIgnoreCase(this.getAmenity()) || !this.getCuisine().isEmpty();
    }
    
    public boolean isStore() {
        return "shop".equalsIgnoreCase(this.getAmenity()) || !this.getShop().isEmpty();
    }

    public String getName() {
        return getOsmTag("name").orElse("");
    }

    public String getAmenity() {
        return getOsmTag("amenity").orElse("");
    }

    public String getShop() {
        return getOsmTag("shop").orElse("");
    }

    public String getBrand() {
        return getOsmTag("brand").orElse("");
    }

    public String getCuisine() {
        return getOsmTag("cuisine").orElse("");
    }

    public String getAddress() {
        return getOsmTag("addr:full").orElse(
               getOsmTag("addr:housenumber").orElse("") + " " +
               getOsmTag("addr:street").orElse("")).trim();
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
                id, getName(), coordinates, getType(), 
                osmNode != null ? osmNode.id() : "N/A");
    }
}
