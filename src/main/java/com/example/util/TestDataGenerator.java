package com.example.util;

import com.example.model.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public final class TestDataGenerator {
    private static final Random random = new Random();

    private TestDataGenerator() {} // Prevents instantiation

    private record NameComponents(String prefix, String type) {}

    public static Graph generateTestGraph(int nodeCount) {
        List<Node> nodes = IntStream.rangeClosed(1, nodeCount)
            .mapToObj(TestDataGenerator::generateRandomNode)
            .collect(Collectors.toList());

        List<Way> ways = generateRandomWays(nodes);

        return new Graph(nodes, ways);
    }

    private static Node generateRandomNode(long id) {
        NameComponents nameComponents = generateRandomNameComponents();
        String name = nameComponents.prefix() + " " + nameComponents.type();
        Coordinates coordinates = generateRandomCoordinates();

        Map<String, String> tags = new HashMap<>();
        tags.put("name", name);
        tags.put("type", random.nextBoolean() ? "store" : "restaurant");

        return new Node(id, coordinates.getLatitude(), coordinates.getLongitude(), tags);
    }

    private static List<Way> generateRandomWays(List<Node> nodes) {
        List<Way> ways = new ArrayList<>();
        Set<Node> connectedNodes = new HashSet<>();
        
        // Ensure all nodes are connected
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node start = nodes.get(i);
            Node end = nodes.get(i + 1);
            ways.add(new Way(start, end, Map.of("highway", "residential", "oneway", "no")));
            connectedNodes.add(start);
            connectedNodes.add(end);
        }
        
        // Add some random additional connections
        int additionalConnections = nodes.size() / 2;
        for (int i = 0; i < additionalConnections; i++) {
            Node start = nodes.get(random.nextInt(nodes.size()));
            Node end = nodes.get(random.nextInt(nodes.size()));
            if (start != end && !areDirectlyConnected(ways, start, end)) {
                ways.add(new Way(start, end, Map.of("highway", "residential", "oneway", "no")));
            }
        }
        
        return ways;
    }

    private static boolean areDirectlyConnected(List<Way> ways, Node start, Node end) {
        return ways.stream().anyMatch(way -> 
            (way.startNode().equals(start) && way.endNode().equals(end)) ||
            (way.startNode().equals(end) && way.endNode().equals(start))
        );
    }

    private static NameComponents generateRandomNameComponents() {
        String[] prefixes = {"North", "South", "East", "West", "Central", "Downtown", "Uptown"};
        String[] types = {"Grocery", "Cafe", "Bookstore", "Restaurant", "Supermarket", "Bakery", "Pharmacy"};
        return new NameComponents(
            prefixes[random.nextInt(prefixes.length)],
            types[random.nextInt(types.length)]
        );
    }

    private static Coordinates generateRandomCoordinates() {
        // Generate coordinates within a reasonable range (e.g., Los Angeles area)
        double delta = 0.1;
        double lat = 33.99 + (random.nextDouble() * 2 * delta - delta); 
        double lon = -118.331 + (random.nextDouble() * 2 * delta - delta); 
        return new Coordinates(lat, lon);
    }
}