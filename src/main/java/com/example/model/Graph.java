package com.example.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class Graph {
    private static final Logger logger = Logger.getLogger(Graph.class.getName());

    private final Map<Long, Node> nodes;
    private final Map<Long, Set<Long>> adjacencyList;

    private static final Set<String> ALLOWED_HIGHWAY_TYPES = Set.of(
        "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
        "residential", "living_street", "motorway_link", "trunk_link",
        "primary_link", "secondary_link", "tertiary_link"
    );

    public Graph() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public Graph(List<Node> nodes, List<Way> ways) {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
            
        nodes.forEach(this::addNode);
        ways.forEach(this::addWay);

        // logger.info("Adding " + ways.size() + " ways to the graph");
        // logger.info("Graph now has " + getWayCount() + " ways");
    }

    public void addNode(Node node) {
        nodes.putIfAbsent(node.id(), node);
        adjacencyList.putIfAbsent(node.id(), new HashSet<>());
    }

    public void addWay(Way way) {
        String highwayType = way.getTags().get("highway");
        if (highwayType == null || !ALLOWED_HIGHWAY_TYPES.contains(highwayType)) {
            // logger.fine(() -> "Skipping way due to invalid highway type: " + highwayType);
            return;
        }

        List<Long> nodeIds = way.getNodeIds();
        for (int i = 0; i < nodeIds.size() - 1; i++) {
            Long startId = nodeIds.get(i);
            Long endId = nodeIds.get(i + 1);
            addNodeIfAbsent(startId);
            addNodeIfAbsent(endId);
            adjacencyList.computeIfAbsent(startId, k -> new HashSet<>()).add(endId);
            
            if (!way.isOneWay()) {
                adjacencyList.computeIfAbsent(endId, k -> new HashSet<>()).add(startId);
            }
        }
    }

    private void addNodeIfAbsent(Long id) {
        nodes.computeIfAbsent(id, k -> new Node(id, 0, 0));
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public Node getNode(long id) {
        return nodes.get(id);
    }

    public Set<Node> getNeighbors(Node node) {
        Set<Long> neighborIds = adjacencyList.get(node.id());
        if (neighborIds == null) {
            return Collections.emptySet();
        }
        return neighborIds.stream()
                .map(this::getNode)
                .collect(Collectors.toSet());
    }


    public Node findNearestRelevantNode(Coordinates coordinates) {
        Node nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<Long, Set<Long>> entry : adjacencyList.entrySet()) {
            Long nodeId = entry.getKey();
            Set<Long> neighbors = entry.getValue();
            if (!neighbors.isEmpty()) {
                Node node = nodes.get(nodeId);
                double distance = node.toCoordinates().distanceTo(coordinates);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = node;
                }
            }
        }
        return nearest;
    }

    public List<Node> findShortestPath(Node start, Node end) {
        return new DijkstraPathFinder(this).findShortestPath(start, end);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getWayCount() {
        Set<String> countedConnections = new HashSet<>();

        for (Map.Entry<Long, Set<Long>> entry : adjacencyList.entrySet()) {
            Long fromNodeId = entry.getKey();
            for (Long toNodeId : entry.getValue()) {
                String connection = fromNodeId < toNodeId ? fromNodeId + "-" + toNodeId : toNodeId + "-" + fromNodeId;
                countedConnections.add(connection);
            }
        }
        return countedConnections.size();
    }

    public void printGraphStructure() {
        logger.info(() -> "Graph Structure:");
        logger.info(() -> "Total nodes: " + nodes.size());
        logger.info(() -> "Total connections: " + getWayCount());
        adjacencyList.forEach((nodeId, neighbors) -> {
            logger.info(() -> "Node: " + nodes.get(nodeId));
            logger.info(() -> "  Neighbors: " + neighbors);
        });
    }

    public void printNodeAdjacencyList(long nodeId) {
        Set<Long> neighbors = adjacencyList.get(nodeId);
        if (neighbors == null) {
            logger.info(() -> "No adjacency list for node " + nodeId);
        } else {
            logger.info(() -> "Neighbors of node " + nodeId + ": " + neighbors);
        }
    }
}