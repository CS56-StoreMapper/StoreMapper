package com.example.util;

import com.example.model.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

/**
 * TestDataGenerator is a utility class for generating test data for the graph-based map system.
 * It creates a random graph with nodes representing locations and ways representing connections between them.
 * The generated data structure mimics real-world OpenStreetMap (OSM) data format.
 */
public final class TestDataGenerator {
    private static final Random random = new Random();

    private TestDataGenerator() {} // Prevent instantiation

    private record NameComponents(String prefix, String type) {}

    /**
     * Generates a test graph with the specified number of nodes and corresponding ways.
     * 
     * @param nodeCount The number of nodes to generate in the graph.
     * @return A Graph object containing randomly generated nodes and ways.
     */
    public static Graph generateTestGraph(int nodeCount) {
        List<Node> nodes = IntStream.rangeClosed(1, nodeCount)
            .mapToObj(TestDataGenerator::generateRandomNode)
            .collect(Collectors.toList());

        List<Way> ways = generateRandomWays(nodes);
        System.out.println("Generated " + ways.size() + " ways");

        Graph graph = new Graph(nodes, ways);
        
        System.out.println("Graph created with " + graph.getNodeCount() + " nodes and " + graph.getWayCount() + " ways");

        return graph;
    }

    /**
     * Generates a random node with a unique ID, random coordinates, and tags.
     * 
     * @param id The unique identifier for the node.
     * @return A Node object with random attributes.
     */
    private static Node generateRandomNode(long id) {
        NameComponents nameComponents = generateRandomNameComponents();
        String name = nameComponents.prefix() + " " + nameComponents.type();
        Coordinates coordinates = generateRandomCoordinates();

        Map<String, String> tags = new HashMap<>();
        tags.put("name", name);
        tags.put("type", random.nextBoolean() ? "store" : "restaurant");

        return new Node(id, coordinates.getLatitude(), coordinates.getLongitude(), tags);
    }

    /**
     * Creates a Way object connecting a list of nodes.
     * The way includes tags similar to those found in OSM data, such as 'highway' and 'name'.
     * 
     * @param nodes A list of Node objects to be connected by this way.
     * @return A Way object representing a connection between the given nodes.
     */
    private static Way createWay(List<Node> nodes) {
        Map<String, String> tags = new HashMap<>();
        tags.put("highway", "residential");
        tags.put("name", "Test Way " + random.nextInt(1000));
        if (random.nextBoolean()) {
            tags.put("oneway", random.nextBoolean() ? "yes" : "no");
        }

        List<Long> nodeIds = nodes.stream().map(Node::id).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("id", random.nextLong(10000));
        data.put("nodes", nodeIds);
        data.put("tags", tags);

        Way way = new Way(nodes.get(0), nodes.get(nodes.size() - 1), data);
        // System.out.println("Created way: " + way);
        return way;
    }

    /**
     * Generates a list of Way objects to connect the given nodes.
     * It creates a circular connection of all nodes and adds random additional connections.
     * 
     * @param nodes The list of Node objects to be connected.
     * @return A List of Way objects representing connections between nodes.
     */
    private static List<Way> generateRandomWays(List<Node> nodes) {
        List<Way> ways = new ArrayList<>();
        
        // Connect all nodes in a circular fashion
        for (int i = 0; i < nodes.size(); i++) {
            List<Node> wayNodes = new ArrayList<>();
            wayNodes.add(nodes.get(i));
            wayNodes.add(nodes.get((i + 1) % nodes.size()));
            ways.add(createWay(wayNodes));
        }
        
        // Add some random additional connections
        int additionalConnections = nodes.size() / 2;
        for (int i = 0; i < additionalConnections; i++) {
            List<Node> wayNodes = new ArrayList<>();
            wayNodes.add(nodes.get(random.nextInt(nodes.size())));
            wayNodes.add(nodes.get(random.nextInt(nodes.size())));
            if (!wayNodes.get(0).equals(wayNodes.get(1))) {
                ways.add(createWay(wayNodes));
            }
        }
        
        return ways;
    }

    /**
     * Generates random name components for a location.
     * 
     * @return A NameComponents object containing a random prefix and type.
     */
    private static NameComponents generateRandomNameComponents() {
        String[] prefixes = {"North", "South", "East", "West", "Central", "Downtown", "Uptown"};
        String[] types = {"Grocery", "Cafe", "Bookstore", "Restaurant", "Supermarket", "Bakery", "Pharmacy"};
        return new NameComponents(
            prefixes[random.nextInt(prefixes.length)],
            types[random.nextInt(types.length)]
        );
    }

    /**
     * Generates random coordinates within a predefined area.
     * 
     * @return A Coordinates object with random latitude and longitude.
     */
    private static Coordinates generateRandomCoordinates() {
        double delta = 0.1;
        double lat = 33.99 + (random.nextDouble() * 2 * delta - delta); 
        double lon = -118.331 + (random.nextDouble() * 2 * delta - delta); 
        return new Coordinates(lat, lon);
    }
}