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

    private static final String[] AMENITIES = {"restaurant", "cafe", "bar", "fast_food", "pub", "pharmacy", "bank", "atm", "school", "library", "hospital", "police", "post_office"};
    private static final String[] SHOPS = {"supermarket", "convenience", "bakery", "butcher", "clothes", "shoes", "electronics", "hardware", "books", "jewelry", "gift", "hairdresser", "car"};
    private static final String[] CUISINES = {"pizza", "burger", "sushi", "chinese", "italian", "mexican", "indian", "thai", "vegetarian", "seafood"};
    private static final String[] BRANDS = {"McDonald's", "Starbucks", "Subway", "KFC", "Burger King", "Walmart", "Target", "CVS", "Walgreens", "Best Buy"};


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
        Coordinates coordinates = generateRandomCoordinates();
        Map<String, String> tags = new HashMap<>();

        if (random.nextBoolean()) {
            // Generate an amenity
            String amenity = AMENITIES[random.nextInt(AMENITIES.length)];
            tags.put("amenity", amenity);
            if (amenity.equals("restaurant") || amenity.equals("cafe") || amenity.equals("fast_food")) {
                tags.put("cuisine", CUISINES[random.nextInt(CUISINES.length)]);
            }
        } else {
            // Generate a shop
            tags.put("shop", SHOPS[random.nextInt(SHOPS.length)]);
        }

        // Add a name
        tags.put("name", generateRandomName(tags));

        // Randomly add a brand
        if (random.nextDouble() < 0.3) {
            tags.put("brand", BRANDS[random.nextInt(BRANDS.length)]);
        }

        // Add an address
        tags.put("addr:street", "Test Street " + random.nextInt(100));
        tags.put("addr:housenumber", String.valueOf(random.nextInt(1000)));

        return new Node(id, coordinates.getLatitude(), coordinates.getLongitude(), tags);
    }

    private static String generateRandomName(Map<String, String> tags) {
        String prefix = tags.containsKey("brand") ? tags.get("brand") : 
                        (tags.containsKey("amenity") ? capitalize(tags.get("amenity")) : 
                        (tags.containsKey("shop") ? capitalize(tags.get("shop")) : ""));
        return prefix + " " + (random.nextInt(1000) + 1);
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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

        Long id = random.nextLong(10000);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("nodes", nodeIds);
        data.put("tags", tags);

        Way way = new Way(id, nodes.get(0), nodes.get(nodes.size() - 1), data);
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
    // private static NameComponents generateRandomNameComponents() {
    //     String[] prefixes = {"North", "South", "East", "West", "Central", "Downtown", "Uptown"};
    //     String[] types = {"Grocery", "Cafe", "Bookstore", "Restaurant", "Supermarket", "Bakery", "Pharmacy"};
    //     return new NameComponents(
    //         prefixes[random.nextInt(prefixes.length)],
    //         types[random.nextInt(types.length)]
    //     );
    // }

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