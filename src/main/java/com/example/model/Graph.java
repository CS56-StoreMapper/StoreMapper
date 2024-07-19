package com.example.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Represents a graph of the road network, storing nodes and ways from OpenStreetMap data.
 * This class provides methods for building and querying the graph structure.
 */
public class Graph {
    private static final Logger logger = Logger.getLogger(Graph.class.getName());

    /** Maps node IDs to Node objects for quick lookup. */
    private final Map<Long, Node> nodesById;
    /**
     * Stores the graph structure. For each node, it maps to another map
     * where the keys are neighboring nodes and the values are the Ways connecting them.
     */
    private Map<Node, Map<Node, Way>> adjacencyList;

    private static final Set<String> ALLOWED_HIGHWAY_TYPES = Set.of(
        "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
        "residential", "living_street", "motorway_link", "trunk_link",
        "primary_link", "secondary_link", "tertiary_link"
    );

    /**
     * Constructs an empty graph.
     */
    public Graph() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Constructs a graph from the given lists of nodes and ways.
     * 
     * @param nodes List of nodes to add to the graph
     * @param ways List of ways to add to the graph
     */
    public Graph(List<Node> nodes, List<Way> ways) {
        this.nodesById = new HashMap<>();
        this.adjacencyList = new HashMap<>();
            
        nodes.forEach(this::addNode);
        ways.forEach(this::addWay);
    }

    /**
     * Adds a node to the graph if it doesn't already exist.
     * 
     * @param node The node to add
     */
    public void addNode(Node node) {
        nodesById.putIfAbsent(node.id(), node);
        adjacencyList.putIfAbsent(node, new HashMap<>());
    }

    /**
     * Adds a way to the graph, creating edges between its nodes.
     * Only ways with allowed highway types are added.
     * For non-oneway roads, edges are added in both directions.
     * 
     * @param way The way to add
     */
    public void addWay(Way way) {
        String highwayType = way.getTags().get("highway");
        if (highwayType == null || !ALLOWED_HIGHWAY_TYPES.contains(highwayType)) {
            return;
        }

        List<Long> nodeIds = way.getNodeIds();
        for (int i = 0; i < nodeIds.size() - 1; i++) {
            Long startId = nodeIds.get(i);
            Long endId = nodeIds.get(i + 1);
            Node startNode = addNodeIfAbsent(startId);
            Node endNode = addNodeIfAbsent(endId);
            addEdge(startNode, endNode, way);
            
            if (!way.isOneWay()) {
                addEdge(endNode, startNode, way);
            }
        }
    }

    /**
     * Adds a node to the graph if it doesn't exist, or returns the existing node.
     * Note: This method creates a new Node with default coordinates (0, 0) if not found.
     * 
     * @param id The ID of the node
     * @return The existing or newly created Node
     */
    private Node addNodeIfAbsent(Long id) {
        return nodesById.computeIfAbsent(id, k -> new Node(id, 0, 0));
    }

    
    /**
     * Adds an edge between two nodes in the graph.
     * If multiple ways connect the same nodes, the one with the highest speed limit is kept.
     * 
     * @param start The starting node of the edge
     * @param end The ending node of the edge
     * @param way The way representing this edge
     */
    private void addEdge(Node start, Node end, Way way) {
        adjacencyList.computeIfAbsent(start, k -> new HashMap<>())
            .merge(end, way, (existingWay, newWay) -> 
                existingWay.getSpeedLimitMph() > newWay.getSpeedLimitMph() ? existingWay : newWay);
    }

     /**
     * Returns a list of all nodes in the graph.
     * 
     * @return List of all Node objects in the graph
     */
    public List<Node> getNodes() {
        return new ArrayList<>(nodesById.values());
    }

    /**
     * Retrieves a node by its ID.
     * 
     * @param id The ID of the node
     * @return The Node object with the given ID, or null if not found
     */
    public Node getNode(long id) {
        return nodesById.get(id);
    }

    /**
     * Retrieves all neighboring nodes for a given node.
     * 
     * This method returns a Set of all nodes that are directly connected to the given node.
     * If the node has no neighbors or doesn't exist in the graph, an empty Set is returned.
     * 
     * @param node The node whose neighbors are to be retrieved.
     * @return A Set of Node objects representing the neighbors of the given node.
     *         Returns an empty Set if the node has no neighbors or doesn't exist in the graph.
     */
    public Set<Node> getNeighbors(Node node) {
        return adjacencyList.getOrDefault(node, Collections.emptyMap()).keySet();
    }

    /**
     * Retrieves the Way object connecting two nodes.
     * 
     * This method returns the Way object that represents the connection between the start and end nodes.
     * If there is no direct connection between the nodes, or if either node doesn't exist in the graph,
     * null is returned.
     * 
     * @param start The starting node of the connection.
     * @param end The ending node of the connection.
     * @return The Way object representing the connection between start and end nodes,
     *         or null if no such connection exists.
     */
    public Way getWay(Node start, Node end) {
        return adjacencyList.getOrDefault(start, Collections.emptyMap()).get(end);
    }


    
    /**
     * Finds the nearest relevant node in the graph to the given coordinates.
     * A node is considered relevant if it has at least one neighbor, meaning
     * it's connected to other nodes in the graph.
     *
     * @param coordinates The coordinates to which the nearest node should be found.
     * @return The nearest relevant node to the given coordinates, or null if no
     *         relevant nodes are found in the graph.
     */
    public Node findNearestRelevantNode(Coordinates coordinates) {
        Node nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<Node, Map<Node, Way>> entry : adjacencyList.entrySet()) {
            Node node = entry.getKey();
            Map<Node, Way> neighbors = entry.getValue();
            // A node is considered relevant if it has neighbors
            if (!neighbors.isEmpty()) {
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

    /**
     * Finds the shortest path between two nodes using Dijkstra's algorithm.
     * This method delegates the pathfinding to a DijkstraPathFinder instance.
     *
     * @param start The starting node of the path.
     * @param end The ending node of the path.
     * @return A List of Nodes representing the shortest path from start to end,
     *         or null if no path is found.
     */
    public List<Node> findShortestPath(Node start, Node end) {
        return new DijkstraPathFinder(this).findShortestPath(start, end);
    }

    /**
     * Returns the total number of nodes in the graph.
     *
     * @return The number of nodes in the graph.
     */
    public int getNodeCount() {
        return nodesById.size();
    }

    /**
     * Counts the total number of unique connections (ways) in the graph.
     * 
     * This method counts each connection only once, regardless of whether it's
     * a one-way or two-way connection. For example, if nodes A and B are connected,
     * it's counted as one connection, not two.
     *
     * @return The total number of unique connections in the graph.
     */
    public int getWayCount() {
        Set<Connection> countedConnections = new HashSet<>();

        for (Map.Entry<Node, Map<Node, Way>> entry : adjacencyList.entrySet()) {
            Node fromNode = entry.getKey();
            for (Node toNode : entry.getValue().keySet()) {
                countedConnections.add(new Connection(fromNode.id(), toNode.id()));
            }
        }
        return countedConnections.size();
    }

    /**
     * Returns a set of all node IDs in the graph.
     * 
     * @return Set of all node IDs in the graph
     */
    public Set<Long> getNodeIds() {
        return nodesById.keySet();
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

    /**
     * Prints the structure of the graph for debugging purposes.
     * This method logs the total number of nodes, connections, and
     * the neighbors of each node in the graph.
     */
    public void printGraphStructure() {
        logger.info(() -> "Graph Structure:");
        logger.info(() -> "Total nodes: " + nodesById.size());
        logger.info(() -> "Total connections: " + getWayCount());
        adjacencyList.forEach((node, neighbors) -> {
            logger.info(() -> "Node: " + node);
            logger.info(() -> "  Neighbors: " + neighbors.keySet());
        });
    }

    /**
     * Prints the adjacency list for a specific node for debugging purposes.
     * This method logs the neighbors of the specified node and the ways
     * connecting them.
     *
     * @param nodeId The ID of the node whose adjacency list should be printed.
     */
    public void printNodeAdjacencyList(long nodeId) {
        Node node = nodesById.get(nodeId);
        if (node == null) {
            logger.info(() -> "Node with ID " + nodeId + " not found in the graph.");
            return;
        } else {
            Map<Node, Way> neighbors = adjacencyList.get(node);
            if (neighbors == null || neighbors.isEmpty()) {
                logger.info(() -> "Node with ID " + nodeId + " has no neighbors.");
            } else {
                logger.info(() -> "Node with ID " + nodeId + " has the following neighbors:");
                neighbors.forEach((neighbor, way) -> {
                    logger.info(() -> "  Neighbor: " + neighbor + " via way: " + way);
                });
            }
        }
        
    }
}