package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.model.Coordinates;
import com.example.model.Location;
import com.example.model.Store;
import com.example.model.Way;
import com.example.model.Graph;
import com.example.model.Route;
import com.example.model.Node;


class MapServiceTest {

    @Mock
    private LocationService locationService;

    private Graph graph;
    private MapService mapService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create a list of nodes with various tags
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1, 34.0522, -118.2437, Map.of("name", "Downtown Cafe", "amenity", "cafe", "cuisine", "coffee")));
        nodes.add(new Node(2, 34.0523, -118.2438, Map.of("name", "Central Library", "amenity", "library")));
        nodes.add(new Node(3, 34.0524, -118.2439, Map.of("name", "City Supermarket", "shop", "supermarket")));
        nodes.add(new Node(4, 34.0525, -118.2440, Map.of("name", "Pizza Place", "amenity", "restaurant", "cuisine", "pizza")));
        nodes.add(new Node(5, 34.0526, -118.2441, Map.of("name", "Main Street Pharmacy", "amenity", "pharmacy")));

        // Create ways connecting these nodes
        List<Way> ways = new ArrayList<>();
        ways.add(createWay(1, nodes.get(0), nodes.get(1)));
        ways.add(createWay(2, nodes.get(1), nodes.get(2)));
        ways.add(createWay(3, nodes.get(2), nodes.get(3)));
        ways.add(createWay(4, nodes.get(3), nodes.get(4)));

        // Create the graph
        graph = new Graph(nodes, ways);

        // Create locations based on the nodes
        List<Location> locations = nodes.stream()
            .map(node -> new Store(node.id(), node.lat(), node.lon(), node))
            .collect(Collectors.toList());

        // Set up the locationService mock to return our test locations
        when(locationService.getAllLocations()).thenReturn(locations);

        // Initialize the MapService
        mapService = new MapService(locationService, graph);

        System.out.println("Graph initialized with " + graph.getNodeCount() + " nodes and " + graph.getWayCount() + " ways");
    }

    private Way createWay(long id, Node start, Node end) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("nodes", Arrays.asList(start.id(), end.id()));
        data.put("tags", Map.of("highway", "residential"));
        return new Way(id, start, end, data);
    }

    @Nested
    class FindNearestLocationTests {
        @Test
        void testFindNearestLocationNoLocations() {
            Coordinates point = new Coordinates(34.0522, -118.2437);
            when(locationService.getAllLocations()).thenReturn(List.of());
            
            Optional<Location> nearest = mapService.findNearestLocation(point);
            assertTrue(nearest.isEmpty(), "Expected empty Optional when no locations are available");
        }

        @Test
        void testFindNearestLocationEquidistant() {
            Coordinates point = new Coordinates(34.0522, -118.2437);
            Coordinates dest1 = Coordinates.getDestinationPoint(point, 1, 0);
            Coordinates dest2 = Coordinates.getDestinationPoint(point, 1, 180);
            Node node1 = new Node(1, dest1.getLatitude(), dest1.getLongitude(), Map.of("name", "Store 1"));
            Node node2 = new Node(2, dest2.getLatitude(), dest2.getLongitude(), Map.of("name", "Store 2"));
            Location loc1 = new Store(1, dest1.getLatitude(), dest1.getLongitude(), node1);
            Location loc2 = new Store(2, dest2.getLatitude(), dest2.getLongitude(), node2);
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));
    
            Optional<Location> nearest = mapService.findNearestLocation(point);
            System.out.println("Nearest location: " + nearest);
    
            assertTrue(nearest.isPresent(), "Expected a location to be returned");
            assertTrue(nearest.get().getId() == 1 || nearest.get().getId() == 2, "Expected either location 1 or 2 to be returned");
        }

        @Test
        void testFindNearestLocationOneIsNearest() {
            Coordinates point = new Coordinates(34.0522, -118.2437);
            Node node1 = new Node(1, 34.0500, -118.2500, Map.of("name", "Store 1"));
            Node node2 = new Node(2, 34.0600, -118.2600, Map.of("name", "Store 2"));
            Location loc1 = new Store(1, 34.0500, -118.2500, node1);
            Location loc2 = new Store(2, 34.0600, -118.2600, node2);
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));

            Optional<Location> nearest = mapService.findNearestLocation(point);

            assertTrue(nearest.isPresent(), "Expected a location to be returned");
            assertEquals(1, nearest.get().getId());
            assertEquals("Store 1", nearest.get().getName());
        }
    }

    @Nested
    class FindLocationsWithinRadiusTests {
        @Test
        void testOneIsWithin() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 10.0;
            
            Coordinates dest1 = Coordinates.getDestinationPoint(center, radiusKm-1, 0);
            Coordinates dest2 = Coordinates.getDestinationPoint(center, radiusKm+1, 90);
            Node node1 = new Node(1, dest1.getLatitude(), dest1.getLongitude(), Map.of("name", "Store 1"));
            Node node2 = new Node(2, dest2.getLatitude(), dest2.getLongitude(), Map.of("name", "Store 2"));
            Location loc1 = new Store(1, dest1.getLatitude(), dest1.getLongitude(), node1);
            Location loc2 = new Store(2, dest2.getLatitude(), dest2.getLongitude(), node2);
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));
        
            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);
        
            System.out.println("Distance to loc1: " + center.distanceTo(loc1.getCoordinates()));
            System.out.println("Distance to loc2: " + center.distanceTo(loc2.getCoordinates()));
        
            assertEquals(1, locationsWithinRadius.size(), "Expected only one location within radius");
            assertEquals(1, locationsWithinRadius.get(0).getId(), "Expected the first location to be within radius");
        }

        @Test
        void testNoResults() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 1.0;
            
            Coordinates dest1 = Coordinates.getDestinationPoint(center, radiusKm+0.1, 0);
            Coordinates dest2 = Coordinates.getDestinationPoint(center, radiusKm+0.1, 90);
            Node node1 = new Node(1, dest1.getLatitude(), dest1.getLongitude(), Map.of("name", "Store 1"));
            Node node2 = new Node(2, dest2.getLatitude(), dest2.getLongitude(), Map.of("name", "Store 2"));
            Location loc1 = new Store(1, dest1.getLatitude(), dest1.getLongitude(), node1);
            Location loc2 = new Store(2, dest2.getLatitude(), dest2.getLongitude(), node2);
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));

            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);

            assertTrue(locationsWithinRadius.isEmpty(), "Expected no locations within radius");
        }

        @Test
        void testBoundary() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 10.0;
            
            Coordinates dest1 = Coordinates.getDestinationPoint(center, radiusKm-0.1, 0);
            Coordinates dest2 = Coordinates.getDestinationPoint(center, radiusKm, 90);
            Coordinates dest3 = Coordinates.getDestinationPoint(center, radiusKm+0.1, 180);
            Node node1 = new Node(1, dest1.getLatitude(), dest1.getLongitude(), Map.of("name", "Store 1"));
            Node node2 = new Node(2, dest2.getLatitude(), dest2.getLongitude(), Map.of("name", "Store 2"));
            Node node3 = new Node(3, dest3.getLatitude(), dest3.getLongitude(), Map.of("name", "Store 3"));
            Location loc1 = new Store(1, dest1.getLatitude(), dest1.getLongitude(), node1);
            Location loc2 = new Store(2, dest2.getLatitude(), dest2.getLongitude(), node2);
            Location loc3 = new Store(3, dest3.getLatitude(), dest3.getLongitude(), node3);
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2, loc3));

            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);

            System.out.println("Distance to loc1: " + center.distanceTo(loc1.getCoordinates()));
            System.out.println("Distance to loc2: " + center.distanceTo(loc2.getCoordinates()));
            System.out.println("Distance to loc3: " + center.distanceTo(loc3.getCoordinates()));

            assertEquals(2, locationsWithinRadius.size(), "Expected two locations within or on the radius");
            assertTrue(locationsWithinRadius.stream().anyMatch(loc -> loc.getId() == 1), "Expected location 1 to be within radius");
            assertTrue(locationsWithinRadius.stream().anyMatch(loc -> loc.getId() == 2), "Expected location 2 to be on the radius boundary");
            assertFalse(locationsWithinRadius.stream().anyMatch(loc -> loc.getId() == 3), "Expected location 3 to be outside radius");
        }

        @Test
        void testZeroRadius() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 0.0;
            
            Node node1 = new Node(1, center.getLatitude(), center.getLongitude(), Map.of("name", "Store 1")); // Exactly at center
            Coordinates dest2 = Coordinates.getDestinationPoint(center, 0.1, 180);
            Node node2 = new Node(2, dest2.getLatitude(), dest2.getLongitude(), Map.of("name", "Store 2"));
            Location loc1 = new Store(1, center.getLatitude(), center.getLongitude(), node1);
            Location loc2 = new Store(2, dest2.getLatitude(), dest2.getLongitude(), node2);
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));

            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);

            assertEquals(1, locationsWithinRadius.size(), "Expected only the location exactly at the center");
            assertEquals(1, locationsWithinRadius.get(0).getId(), "Expected only the location at the center to be returned");
        }
    }
    @Nested
    class CalculateRouteTests {
        @Test
        void testBasicRoute() {
            Coordinates start = new Coordinates(34.0522, -118.2437);
            Coordinates end = new Coordinates(34.0524, -118.2439);
            
            Route route = mapService.calculateShortestRoute(start, end);
            
            assertNotNull(route);
            assertEquals(3, route.getNodes().size());
            assertTrue(route.getTotalDistance() > 0);
        }

        @Test
        void testRouteWithSameStartAndEnd() {
            Coordinates start = new Coordinates(34.0522, -118.2437);
            Coordinates end = new Coordinates(34.0522, -118.2437);
            
            Route route = mapService.calculateShortestRoute(start, end);
            
            assertNull(route, "Expected null route when start and end are the same");
        }

        @Test
        void testRouteWithUnreachableEnd() {
            Coordinates start = new Coordinates(34.0522, -118.2437);
            Coordinates end = new Coordinates(40.7128, -74.0060); // New York City coordinates
            
            Route route = mapService.calculateShortestRoute(start, end);
            
            assertNull(route, "Expected null route when end is unreachable");
        }
    }

    @Test
    void testGetNodeCount() {
        assertEquals(5, mapService.getNodeCount(), "Expected 5 nodes in the graph");
    }

    @Test
    void testGetWayCount() {
        assertEquals(4, mapService.getWayCount(), "Expected 4 ways in the graph");
    }

}