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

    private class NodeWrapper {
        Node node;
        double priority;
    
        NodeWrapper(Node node, double priority) {
            this.node = node;
            this.priority = priority;
        }
    
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeWrapper that = (NodeWrapper) o;
            return Objects.equals(node, that.node);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(node);
        }
    }

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public Graph(List<Node> nodes, List<Way> ways) {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
            
        for (Node node : nodes) {
            addNode(node);
        }

        for (Way way : ways) {
            addWay(way);
        }
    }

    public void addNode(Node node) {
        nodes.putIfAbsent(node.id(), node);
        adjacencyList.putIfAbsent(node.id(), new HashSet<>());
    }

    public void addWay(Way way) {
        Map<String, String> tags = way.getTags();
        String highwayType = tags.get("highway");
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
        if (!nodes.containsKey(id)) {
            Node node = new Node(id, 0, 0); // Placeholder coordinates
            addNode(node);
        }
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

    // public Node findNearestNode(double lat, double lon) {
    //     return nodes.values().stream()
    //         .min(Comparator.comparingDouble(node -> 
    //             Math.pow(node.lat() - lat, 2) + Math.pow(node.lon() - lon, 2)))
    //         .orElseThrow(() -> new IllegalStateException("No nodes in the graph"));
    // }

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
        // Step 1: Initialize data structures
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
    
        // Step 2: Initialize distances to infinity for all nodes
        for (Node node : nodes.values()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.offer(start);
    
        // Step 3: Main Loop
        while (!queue.isEmpty()) {
            // Step 3.1: Get the node with the smallest distance
            Node current = queue.poll();
    
            // Step 3.2: If the current node is the end node, return the path
            if (current.equals(end)) {
                return reconstructPath(previousNodes, end);
            }
    
            // Step 3.3: Get the neighbors of the current node
            Set<Node> neighbors = getNeighbors(current);
    
            // Step 3.4: For each neighbor, calculate the new distance and update if it's shorter
            for (Node neighbor : neighbors) {                
                double newDist = distances.get(current) + calculateDistance(current, neighbor);
    
                // Step 3.4.1: If the new distance is shorter, update the distance and previous node
                if (newDist < distances.get(neighbor)) {
                    queue.remove(neighbor);
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
    
        // Step 4: If no path is found, return an empty list
        return null;
    }

    private List<Node> reconstructPath(Map<Node, Node> previousNodes, Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }
        return path;
    }

    private double calculateDistance(Node node1, Node node2) {
        return node1.toCoordinates().distanceTo(node2.toCoordinates());
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

    private void printPath(List<Node> path) {
        for (int i = 0; i < path.size(); i++) {
            System.out.println(i + ": " + path.get(i));
        }
    }

    public void printGraphStructure() {
        System.out.println("Graph Structure:");
        for (Map.Entry<Long, Set<Long>> entry : adjacencyList.entrySet()) {
            Node node = nodes.get(entry.getKey());
            System.out.println("Node: " + node);
            System.out.println("  Neighbors:");
            for (Long neighborId : entry.getValue()) {
                System.out.println("    " + nodes.get(neighborId));
            }
        }
    }
}