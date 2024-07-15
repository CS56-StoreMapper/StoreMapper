package com.example.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;


import com.example.model.Node;
import com.example.model.Way;
import com.example.util.OSMDataLoader;

/**
 * Service class for processing OpenStreetMap (OSM) data.
 * This class is responsible for loading, filtering, and transforming OSM nodes and ways.
 */
public class OSMDataService {
    private final OSMDataLoader dataLoader;
    private Map<Long, Node> loadedNodes;

    /**
     * Set of allowed highway types for filtering ways.
     * Only ways with these highway types will be processed.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = Set.of(
            "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
            "residential", "living_street", "motorway_link", "trunk_link",
            "primary_link", "secondary_link", "tertiary_link"
    );

    /**
     * Constructor initializing the OSMDataLoader.
     */
    public OSMDataService() {
        this.dataLoader = new OSMDataLoader();
    }

    /**
     * Loads and processes OSM nodes from the given files.
     * This method performs the following steps:
     * 1. Loads all nodes from the nodes file.
     * 2. Loads and processes ways from the ways file.
     * 3. Filters nodes to include only those that are part of the processed ways.
     *
     * @param nodesFile Path to the file containing node data.
     * @param waysFile Path to the file containing way data.
     * @return A list of Node objects that are part of the processed ways.
     * @throws IOException If there's an error reading the files.
     * @throws ClassNotFoundException If there's an error deserializing the data.
     */
    public List<Node> loadNodes(String nodesFile, String waysFile) throws IOException, ClassNotFoundException {
        Map<Long, Node> allNodes = dataLoader.loadData(nodesFile, Node::fromMap).stream()
                .collect(Collectors.toMap(Node::id, node -> node));

        this.loadedNodes = allNodes; // Store all nodes before filtering

        List<Way> ways = loadWays(waysFile);

        Set<Long> activeNodeIds = ways.stream()
                .flatMap(way -> List.of(way.startNode().id(), way.endNode().id()).stream())
                .collect(Collectors.toSet());

        return allNodes.values().stream()
                .filter(node -> activeNodeIds.contains(node.id()))
                .toList();
    }

    /**
     * Loads and processes OSM ways from the given file.
     * This method filters ways based on allowed highway types and creates Way objects.
     *
     * @param filename Path to the file containing way data.
     * @return A list of Way objects representing the processed ways.
     * @throws IOException If there's an error reading the file.
     * @throws ClassNotFoundException If there's an error deserializing the data.
     * @throws IllegalStateException If nodes have not been loaded before calling this method.
     */
    public List<Way> loadWays(String filename) throws IOException, ClassNotFoundException {
        if (this.loadedNodes == null) {
            throw new IllegalStateException("Nodes must be loaded before loading ways");
        }
        return dataLoader.loadData(filename, map -> mapToWays(map, this.loadedNodes))
                .stream()
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Maps a raw data map to a list of Way objects.
     * This method performs the following steps:
     * 1. Checks if the way's highway type is allowed.
     * 2. Extracts node IDs for the way.
     * 3. Creates Way objects for each segment of the way using the provided nodes.
     * 4. Handles one-way and two-way streets appropriately.
     *
     * @param map Raw data map representing a way.
     * @param nodes Map of node IDs to Node objects.
     * @return A list of Way objects created from the raw data.
     */
    private List<Way> mapToWays(Map<String, Object> map, Map<Long, Node> nodes) {
        List<Way> ways = new ArrayList<>();

        @SuppressWarnings("unchecked")
        String highwayType = ((Map<String, String>) map.get("tags")).get("highway");

        if (!ALLOWED_HIGHWAY_TYPES.contains(highwayType)) {
            return ways;
        }

        @SuppressWarnings("unchecked")
        List<Long> nodeIds = ((List<Number>) map.get("nodes")).stream()
                .map(Number::longValue)
                .toList();

        for (int i = 0; i < nodeIds.size() - 1; i++) {
            long startId = nodeIds.get(i);
            long endId = nodeIds.get(i + 1);
            Node startNode = nodes.get(startId);
            Node endNode = nodes.get(endId);

            if (startNode != null && endNode != null) {
                Way way = Way.create(startNode, endNode, map);
                ways.add(way);

                if (!way.isOneWay()) {
                    ways.add(Way.create(endNode, startNode, map));
                }
            }
        }

        return ways;
    }
}