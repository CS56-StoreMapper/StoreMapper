package com.example;

import com.example.util.OSMDataLoader;
import com.example.model.Node;
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
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseMapTest {
    private static final Logger logger = Logger.getLogger(BaseMapTest.class.getName());
    protected Graph graph;
    protected MapService mapService;
    protected LocationService locationService;
    private final OSMDataLoader osmDataLoader = new OSMDataLoader();

    @BeforeAll
    public void setUp() throws IOException, ClassNotFoundException {
        // Load MIT dataset
        String nodesFile = System.getProperty("user.dir") + "/data/mit.nodes.ser";
        String waysFile = System.getProperty("user.dir") + "/data/mit.ways.ser";
        
        logger.info("Loading nodes from: " + nodesFile);
        List<Node> nodes = loadNodesFromFile(nodesFile);
        logger.info("Loaded " + nodes.size() + " nodes");
        
        logger.info("Loading ways from: " + waysFile);
        List<Way> ways = loadWaysFromFile(waysFile);
        logger.info("Loaded " + ways.size() + " ways");
        

        graph = new Graph(nodes, ways);
        logger.info("Graph created with " + graph.getNodeCount() + " nodes and " + graph.getWayCount() + " ways");
        graph.printGraphStructure();
        
        locationService = new InMemoryLocationService(graph);
        logger.info("LocationService created");
        
        mapService = new MapService(locationService, graph);
        logger.info("MapService created");
        
        // Add test for findNearestLocation
        Coordinates testPoint = new Coordinates(42.3601, -71.0942); // Example coordinates for Boston
        logger.info("Calling findNearestLocation with coordinates: " + testPoint);
        Optional<Location> nearestLocation = mapService.findNearestLocation(testPoint, null);
        logger.info("Nearest location: " + nearestLocation);
        logger.info("setUp method completed");

    }

    private List<Node> loadNodesFromFile(String filename) throws IOException, ClassNotFoundException {
        return osmDataLoader.loadData(filename, Node::fromMap);
    }

    private List<Way> loadWaysFromFile(String filename) throws IOException, ClassNotFoundException {
        return osmDataLoader.loadData(filename, Way::fromMap);
    }
}