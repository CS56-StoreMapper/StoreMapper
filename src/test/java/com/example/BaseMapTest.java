package com.example;

import com.example.util.DataFileManager;
import com.example.util.OSMDataLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.model.Node;
import com.example.model.Route;
import com.example.model.Way;
import com.example.model.Coordinates;
import com.example.model.Graph;
import com.example.model.Store;
import com.example.model.Location;
import com.example.service.MapService;
import com.example.service.LocationService;
import com.example.service.InMemoryLocationService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseMapTest {
    private static final Logger logger = Logger.getLogger(BaseMapTest.class.getName());
    protected Graph graph;
    protected MapService mapService;
    protected LocationService locationService;
    private final OSMDataLoader osmDataLoader = new OSMDataLoader();

    protected enum RouteType {
        SHORTEST,
        FASTEST
    }

    @BeforeAll
    public void setUp() throws IOException, ClassNotFoundException {
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        logger.info("Maximum heap size at test setup: " + (maxHeapSize / (1024 * 1024)) + " MB");
        String datasetName = getDatasetName();
        String nodesFile = datasetName + ".nodes.json";
        String waysFile = datasetName + ".ways.json";
        
        logger.info("Loading nodes from: " + nodesFile);
        List<Node> nodes = loadNodesFromFile(nodesFile);
        logger.info("Loaded " + nodes.size() + " nodes");
        
        logger.info("Loading ways from: " + waysFile);
        List<Way> ways = loadWaysFromFile(waysFile);
        logger.info("Loaded " + ways.size() + " ways");
        

        graph = new Graph(nodes, ways);
        logger.info("Graph created with " + graph.getNodeCount() + " nodes and " + graph.getWayCount() + " ways");
        
        locationService = new InMemoryLocationService(graph);
        logger.info("LocationService created");
        
        mapService = new MapService(locationService, graph);
        logger.info("MapService created");
        
        // Add test for findNearestLocation
        Coordinates testPoint = getTestPoint();
        logger.info("Calling findNearestLocation with coordinates: " + testPoint);
        Optional<Location> nearestLocation = mapService.findNearestLocation(testPoint, null);
        logger.info("Nearest location: " + nearestLocation);
        // logger.info("setUp method completed");

    }

    

    private List<Node> loadNodesFromFile(String filename) throws IOException {
        return osmDataLoader.loadData(filename, Node::fromMap);
    }
    
    private List<Way> loadWaysFromFile(String filename) throws IOException {
        List<Way> ways = osmDataLoader.loadData(filename, Way::fromMap);
        Map<String, Integer> highwayTypeCounts = new HashMap<>();
        for (Way way : ways) {
            String highwayType = way.getTags().get("highway");
            highwayTypeCounts.merge(highwayType, 1, Integer::sum);
        }
        logger.info("Highway type counts: " + highwayTypeCounts);
        return ways;
    }

    // protected void compareOutput(Coordinates start, Coordinates end, String testName, String type) {
    //     List<Coordinates> expectedPath = readExpectedPath(testName);
    //     Route actualRoute = mapService.calculateRoute(start, end);
    //     compareResultExpected(actualRoute, expectedPath, type);
    // }

    protected void compareResultExpected(Route actualRoute, List<Coordinates> expectedPath, String type) {
        assertNotNull(actualRoute, "Expected a route to be found, but no route was returned.");
        List<Node> actualNodes = actualRoute.getNodes();
        
        assertEquals(expectedPath.size(), actualNodes.size(), "Path lengths differ");
        
        for (int i = 0; i < expectedPath.size(); i++) {
            Coordinates expected = expectedPath.get(i);
            Node actual = actualNodes.get(i);
            assertTrue(coordinatesClose(expected, actual.toCoordinates()),
                       String.format("Paths differ at position %d. Expected %s, got %s.", i, expected, actual.toCoordinates()));
        }
        
        double routeLength = actualRoute.getTotalDistance();
        double directDistance = expectedPath.get(0).distanceTo(expectedPath.get(expectedPath.size() - 1));
        double ratio = routeLength / directDistance;
        
        System.out.println("Route length: " + routeLength + " units");
        System.out.println("Direct distance: " + directDistance + " units");
        System.out.println("Route/Direct ratio: " + ratio);
        
        assertTrue(routeLength >= directDistance, "Route shorter than direct distance");
        if (ratio > 2.0) {
            System.out.println("WARNING: Route is more than twice the direct distance (ratio: " + ratio + ")");
        }
        
        System.out.println("Route verified with " + actualNodes.size() + " nodes and total distance of " + routeLength + " units");
    }

    protected void testRoute(String testName, RouteType routeType) {
        List<List<Double>> expectedPath = readExpectedPath(testName);
        
        Coordinates start = new Coordinates(expectedPath.get(0).get(0), expectedPath.get(0).get(1));
        Coordinates end = new Coordinates(expectedPath.get(expectedPath.size() - 1).get(0), expectedPath.get(expectedPath.size() - 1).get(1));
        
        Route actualRoute;
        if (routeType == RouteType.SHORTEST) {
            actualRoute = mapService.calculateShortestRoute(start, end);
        } else {
            actualRoute = mapService.calculateFastestRoute(start, end);
        }
        
        assertNotNull(actualRoute, "Expected a route to be found, but no route was returned.");
        assertFalse(actualRoute.getNodes().isEmpty(), "Expected a non-empty route");
        
        List<Node> actualNodes = actualRoute.getNodes();
        
        assertEquals(expectedPath.size(), actualNodes.size(), "Route length mismatch");
        
        for (int i = 0; i < expectedPath.size(); i++) {
            List<Double> expectedCoord = expectedPath.get(i);
            Node actualNode = actualNodes.get(i);
            
            assertTrue(coordinatesClose(new Coordinates(expectedCoord.get(0), expectedCoord.get(1)), actualNode.toCoordinates()),
                       "Node mismatch at index " + i + ". Expected: " + expectedCoord + ", Actual: " + actualNode);
        }
        
        double routeLength = actualRoute.getTotalDistance();
        double directDistance = start.distanceTo(end);
        double ratio = routeLength / directDistance;
        
        System.out.println("Route length: " + routeLength + " units");
        System.out.println("Direct distance: " + directDistance + " units");
        System.out.println("Route/Direct ratio: " + ratio);
        
        assertTrue(routeLength >= directDistance, "Route shorter than direct distance");
        if (ratio > 2.0) {
            System.out.println("WARNING: Route is more than twice the direct distance (ratio: " + ratio + ")");
        }
        
        Set<Long> nodeIds = new HashSet<>();
        for (Node node : actualNodes) {
            assertTrue(nodeIds.add(node.id()), "Route contains a loop");
        }
        
        System.out.println("Route verified with " + actualNodes.size() + " nodes and total distance of " + routeLength + " units");
    }

    protected boolean coordinatesClose(Coordinates c1, Coordinates c2) {
        double latDiff = Math.abs(c1.getLatitude() - c2.getLatitude());
        double lonDiff = Math.abs(c1.getLongitude() - c2.getLongitude());
        return latDiff <= 1e-6 && lonDiff <= 1e-6;
    }

     protected List<List<Double>> readExpectedPath(String testName) {
        try {
            String jsonPath = "test_data/test_" + getDatasetName() + "_" + testName + ".json";
            Path path = Paths.get(jsonPath);
            if (!Files.exists(path)) {
                System.out.println("Warning: JSON file not found for test: " + testName);
                return null;
            }
            String jsonContent = new String(Files.readAllBytes(path));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonContent, new TypeReference<List<List<List<Double>>>>() {}).get(0);
        } catch (IOException e) {
            fail("Failed to read expected path from JSON: " + e.getMessage());
            return null;
        }
    }

    protected abstract Coordinates getTestPoint();
    protected abstract String getDatasetName();
}