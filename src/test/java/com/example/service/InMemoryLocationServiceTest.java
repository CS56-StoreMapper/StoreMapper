package com.example.service;

import com.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryLocationServiceTest {

    private InMemoryLocationService service;
    private Graph testGraph;

    @BeforeEach
    public void setUp() {
        testGraph = createTestGraph();
        service = new InMemoryLocationService(testGraph);
    }

    private Graph createTestGraph() {
        List<Node> nodes = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        
        // Add some test nodes
        nodes.add(new Node(1L, 40.7128, -74.0060, Map.of("type", "store")));
        nodes.add(new Node(2L, 34.0522, -118.2437, Map.of("type", "restaurant")));
        nodes.add(new Node(3L, 40.7300, -74.0100, Map.of("type", "store")));
        
        // Create a simple graph with these nodes
        Graph graph = new Graph(nodes, ways);
        System.out.println("Created test graph with " + nodes.size() + " nodes");
        
        // Print out details of each node
        for (Node node : nodes) {
            System.out.println("Node: " + node.id() + ", Type: " + node.tags().getOrDefault("type", "unknown"));
        }
        
        return graph;
    }

    @Test
    public void testInitialization() {
        assertEquals(3, service.getAllLocations().size(), "Should have initialized 3 locations");
        assertNotNull(service.getLocationById(1L), "Location with ID 1 should exist");
        assertNotNull(service.getLocationById(2L), "Location with ID 2 should exist");
        assertNotNull(service.getLocationById(3L), "Location with ID 3 should exist");
    }

    @Test
    public void testGraphCreation() {
        Graph graph = createTestGraph();
        assertNotNull(graph, "Graph should not be null");
        assertEquals(3, graph.getNodes().size(), "Graph should have 3 nodes");
        assertTrue(graph.getNodes().stream().anyMatch(node -> node.id() == 1L), "Graph should contain node with ID 1");
        assertTrue(graph.getNodes().stream().anyMatch(node -> node.id() == 2L), "Graph should contain node with ID 2");
        assertTrue(graph.getNodes().stream().anyMatch(node -> node.id() == 3L), "Graph should contain node with ID 3");
    }

    @Test
    public void testGetAllLocations() {
        List<Location> allLocations = service.getAllLocations();
        assertEquals(3, allLocations.size());
        assertTrue(allLocations.stream().anyMatch(loc -> loc.getId() == 1L && loc instanceof Store));
        assertTrue(allLocations.stream().anyMatch(loc -> loc.getId() == 2L && loc instanceof Restaurant));
        assertTrue(allLocations.stream().anyMatch(loc -> loc.getId() == 3L && loc instanceof Store));
    }

    @Test
    public void testGetLocationById() {
        Location location = service.getLocationById(1L);
        assertNotNull(location);
        assertTrue(location instanceof Store);
        assertEquals(40.7128, location.getCoordinates().getLatitude(), 0.0001);
        assertEquals(-74.0060, location.getCoordinates().getLongitude(), 0.0001);
    }

    @Test
    public void testFindNearestLocation() {
        Coordinates testCoords = new Coordinates(40.7, -74.0);
        Predicate<Location> filter = location -> location instanceof Store;
        Optional<Location> nearest = service.findNearestLocation(testCoords, filter);
        assertTrue(nearest.isPresent());
        assertEquals(1L, nearest.get().getId());
        assertTrue(nearest.get() instanceof Store);
    }

    @Test
    public void testFindNearestLocationWithDifferentFilter() {
        Coordinates testCoords = new Coordinates(40.7, -74.0);
        Predicate<Location> filter = location -> location instanceof Restaurant;
        Optional<Location> nearest = service.findNearestLocation(testCoords, filter);
        assertTrue(nearest.isPresent());
        assertEquals(2L, nearest.get().getId());
        assertTrue(nearest.get() instanceof Restaurant);
    }

    // You can keep the helper methods if they're still useful for other tests
    private Store createTestStore(long id, String name, double lat, double lon) {
        return new Store(id, name, new Coordinates(lat, lon));
    }

    private void createAndAddTestStores(String testData) {
        Stream.of(testData.split("\n"))
            .map(line -> line.split(","))
            .map(parts -> createTestStore(
                Long.parseLong(parts[0]),
                parts[1],
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
            ))
            .forEach(service::addLocation);
    }
}