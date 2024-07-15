package com.example.model;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a node in OpenStreetMap (OSM) data.
 *
 * A node is a specific point on the earth's surface defined by its latitude and longitude.
 * It can optionally have tags that describe the node's attributes or features.
 */
public record Node(long id, double lat, double lon, Map<String, String> tags) {

    /**
     * Constructs a new Node with the given ID, latitude, and longitude.
     *
     * @param id The unique identifier of the node
     * @param lat The latitude of the node
     * @param lon The longitude of the node
     */
    public Node(long id, double lat, double lon) {
        this(id, lat, lon, Map.of());
    }

    /**
     * Creates a Node object from a Map of OSM data.
     *
     * This method expects a Map with the following structure:
     * {
     *     "id": Long,
     *     "lat": Double,
     *     "lon": Double,
     *     "tags": Map<String, String> (optional)
     * }
     *
     * For example:
     * {
     *     "id": 1234567,
     *     "lat": 51.5074,
     *     "lon": -0.1278,
     *     "tags": {
     *         "amenity": "cafe",
     *         "name": "Central Perk",
     *         "opening_hours": "Mo-Fr 07:00-22:00; Sa-Su 08:00-23:00"
     *     }
     * }
     *
     * @param map A Map containing OSM Node data
     * @return A new Node object
     * @throws ClassCastException if the map contains invalid data types
     * @throws NullPointerException if required fields are missing
     */
    public static Node fromMap(Map<String, Object> map) {
        long id = ((Number) map.get("id")).longValue();
        double lat = ((Number) map.get("lat")).doubleValue();
        double lon = ((Number) map.get("lon")).doubleValue();
        @SuppressWarnings("unchecked")
        Map<String, String> tags = (Map<String, String>) map.getOrDefault("tags", Map.of());
        return new Node(id, lat, lon, tags);
    }

    /**
     * Creates a Node object from a Map of OSM data, with error handling.
     *
     * This method expects a Map with the same structure as described in the fromMap method.
     *
     * For example:
     * {
     *     "id": 1234567,
     *     "lat": 51.5074,
     *     "lon": -0.1278,
     *     "tags": {
     *         "amenity": "cafe",
     *         "name": "Central Perk",
     *         "opening_hours": "Mo-Fr 07:00-22:00; Sa-Su 08:00-23:00"
     *     }
     * }
     *
     * @param data A Map containing OSM Node data
     * @return An Optional containing a Node object if all required data is present and valid,
     *         or an empty Optional otherwise
     */
    public static Optional<Node> create(Map<String, Object> data) {
        try {
            return Optional.of(fromMap(data));
        } catch (ClassCastException | NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the value of a specific tag.
     * 
     * @param key The key of the tag to retrieve
     * @return The value of the tag, or null if the tag doesn't exist
     */
    public String getTag(String key) {
        return tags.get(key);
    }

    /**
     * Returns a Coordinates object representing the location of this Node.
     * 
     * @return A new Coordinates object
     */
    public Coordinates toCoordinates() {
        return new Coordinates(lat, lon);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", tags=" + tags +
                '}';
    }
}
