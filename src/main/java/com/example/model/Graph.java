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
    }

    public void addNode(Node node) {
        nodes.putIfAbsent(node.id(), node);
        adjacencyList.putIfAbsent(node.id(), new HashSet<>());
    }

    public void addWay(Way way) {
        String highwayType = way.getTags().get("highway");
        if (highwayType == null || !ALLOWED_HIGHWAY_TYPES.contains(highwayType)) {
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
            // A node is considered relevant if it has neighbors
            if (!neighbors.isEmpty()) {
                Node node = nodes.get(nodeId);
                double distance = node.toCoordinates().distanceTo(coordinates);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = node;
                    // Early termination if we find an exact match
                    if (distance == 0) {
                        break;
                    }
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
        Set<Connection> countedConnections = new HashSet<>();

        for (Map.Entry<Long, Set<Long>> entry : adjacencyList.entrySet()) {
            Long fromNodeId = entry.getKey();
            for (Long toNodeId : entry.getValue()) {
                countedConnections.add(new Connection(fromNodeId, toNodeId));
            }
        }
        return countedConnections.size();
    }

    /**
     * Represents a unique, undirected connection between two nodes in the graph.
     * 
     * This record ensures that connections are treated as undirected edges,
     * regardless of the order in which the node IDs are provided. It maintains
     * a consistent representation by always storing the smaller node ID first.
     * 
     * Key features:
     * 1. Undirected: Connection(A, B) is equivalent to Connection(B, A).
     * 2. Unique representation: Ensures each connection is represented uniquely,
     *    which is crucial for correct counting and set operations.
     * 3. Immutable: Once created, a Connection cannot be modified.
     * 
     * @param node1 The ID of the first node (always the smaller of the two IDs)
     * @param node2 The ID of the second node (always the larger of the two IDs)
     */
    private record Connection(long node1, long node2) {
        /**
         * Compact constructor for Connection.
         * Ensures that node1 is always the smaller ID and node2 is the larger ID.
         * This guarantees a consistent, direction-independent representation of the connection.
         */
        Connection {
            if (node1 > node2) {
                long temp = node1;
                node1 = node2;
                node2 = temp;
            }
        }
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