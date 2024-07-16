package com.example.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a segment of an OpenStreetMap (OSM) way.
 *
 * In OSM, a way is an ordered list of nodes which normally also has at least one tag
 * or is included within a Relation. A way can have between 2 and 2,000 nodes.
 *
 * This class represents a segment of such a way, defined by its start and end nodes.
 */
public record Way(Node startNode, Node endNode, Map<String, Object> data) {

    public Way {
        data = data == null ? Map.of() : data;
    }

    /**
     * Retrieves the list of all node IDs that make up this way.
     *
     * @return A List of Long values representing the IDs of all nodes in this way
     */
    @SuppressWarnings("unchecked")
    public List<Long> getNodeIds() {
        List<?> nodeIds = (List<?>) data.getOrDefault("nodes", Collections.emptyList());
        return nodeIds.stream()
                .map(Way::toLong)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the tags associated with this way.
     *
     * Tags in OSM are key-value pairs that describe features of map elements.
     * Common tags for ways include "highway" (e.g., "residential", "motorway"),
     * "name", "oneway", "maxspeed", etc.
     *
     * @return A Map of String key-value pairs representing the tags
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getTags() {
        return (Map<String, String>) data.getOrDefault("tags", Collections.emptyMap());
    }

    /**
     * Creates a Way object from a Map of OSM data.
     *
     * This method expects a Map with the following structure:
     * {
     *     "id": Long,
     *     "nodes": List<Long>,
     *     "tags": Map<String, String>
     * }
     *
     * The 'data' Map structure:
     * - "id": A Long value representing the unique identifier of the way in OSM.
     * - "nodes": A List of Long values, each representing a node ID. These nodes
     *            form the geometry of the way in order.
     * - "tags": A Map of String key-value pairs describing attributes of the way.
     *           Common tags include:
     *           - "highway": Describes the type of road (e.g., "residential", "motorway")
     *           - "name": The name of the way (e.g., "Main Street")
     *           - "oneway": Indicates if the way is one-way ("yes" or "no")
     *           - "maxspeed": Speed limit on the way
     *           - Other tags may be present depending on the specific features of the way
     *
     * Example:
     * {
     *     "id": 12345,
     *     "nodes": [100, 101, 102, 103],
     *     "tags": {
     *         "highway": "residential",
     *         "name": "Main Street",
     *         "oneway": "yes",
     *         "maxspeed": "30 mph",
     *         "maxspeed_mph": "30"
     *     }
     * }
     *
     * The method creates a Way object representing a segment of the OSM way,
     * with startNodeId and endNodeId representing the endpoints of this segment.
     *
     * @param startNodeId The ID of the start node of this way segment
     * @param endNodeId The ID of the end node of this way segment
     * @param data A Map containing OSM Way data
     * @return A new Way object
     */
    public static Way create(Node startNode, Node endNode, Map<String, Object> data) {
        return new Way(startNode, endNode, data);
    }

    /**
     * Creates a Way object from a Map of OSM data.
     *
     * This method is used by the OSMDataLoader to create Way objects from the serialized data.
     * It expects a Map with the following structure:
     * {
     *     "id": Long,
     *     "nodes": List<Long>,
     *     "tags": Map<String, String>
     * }
     *
     * @param map A Map containing OSM Way data
     * @return A new Way object
     */
    public static Way fromMap(Map<String, Object> map) {
        try {
            @SuppressWarnings("unchecked")
            List<?> nodeIds = (List<?>) map.get("nodes");
            if (nodeIds == null || nodeIds.size() < 2) {
                throw new IllegalArgumentException("Way must have at least two nodes");
            }
    
            List<Long> longNodeIds = nodeIds.stream()
                    .map(Way::toLong)
                    .collect(Collectors.toList());
            Node startNode = new Node(longNodeIds.get(0), 0, 0);
            Node endNode = new Node(longNodeIds.get(longNodeIds.size() - 1), 0, 0);
    
            return new Way(startNode, endNode, map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid map data for Way: " + map, e);
        }
    }

    private static long toLong(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof Long) {
            return (Long) obj;
        } else {
            throw new IllegalArgumentException("Expected Integer or Long, but got " + obj.getClass().getName());
        }
    }

    public Way withNodes(Node newStartNode, Node newEndNode) {
        return new Way(newStartNode, newEndNode, this.data);
    }

    /**
     * Checks if this way is a one-way street.
     *
     * @return true if the way is one-way, false otherwise
     */
    public boolean isOneWay() {
        return "yes".equals(getTags().get("oneway"));
    }

    /**
     * Retrieves the highway type of this way.
     *
     * @return The highway type as a String, or null if not specified
     */
    public String getHighwayType() {
        return getTags().get("highway");
    }

    @Override
    public String toString() {
        return "Way{" +
                "startNode=" + startNode +
                ", endNode=" + endNode +
                ", tags=" + getTags() +
                '}';
    }
}