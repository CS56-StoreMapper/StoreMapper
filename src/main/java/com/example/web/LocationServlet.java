package com.example.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.service.LocationService;
import com.example.service.InMemoryLocationService;
import com.example.model.Coordinates;
import com.example.model.Graph;
import com.example.model.Location;
import com.example.model.Route;
import com.example.model.Way;
import com.example.model.Node;
import com.example.service.MapService;
import com.example.util.OSMDataLoader;
import com.example.util.TypeLoader;
import com.example.util.DistanceUtil;
import com.example.util.MemoryUtil;

@WebServlet(name = "LocationServlet", urlPatterns = {"/", "/locations", "/route", "/nearest", "/within-radius", "/search"})
public class LocationServlet extends HttpServlet {
    private static final boolean IS_TEST_ENVIRONMENT = System.getProperty("maven.test") != null;
    private static final boolean BOUNDS_CHECKING_ENABLED = !IS_TEST_ENVIRONMENT;
    private static final Logger logger = Logger.getLogger(LocationServlet.class.getName());
    
    // Load cuisine and shop types from JSON files
    private static final List<String> cuisineTypes = TypeLoader.loadTypes("unique_cuisine_types.json");
    private static final List<String> shopTypes = TypeLoader.loadTypes("unique_shops_types.json");
    
    // Create a Gson instance to convert Java objects to JSON
    private static final Gson gson = new Gson();

    private LocationService locationService;
    private MapService mapService;
    private OSMDataLoader osmDataLoader = new OSMDataLoader();

    @Override
    public void init() throws ServletException {
        logger.info("Initializing LocationServlet. " + 
                    (IS_TEST_ENVIRONMENT ? "Test environment detected. " : "Production environment detected. ") +
                    "Bounds checking " + (BOUNDS_CHECKING_ENABLED ? "enabled" : "disabled"));
        // locationService = new InMemoryLocationService(40);
        try {
            String nodesFile = "/prod_data/west_los_angeles.nodes.json";
            String waysFile = "/prod_data/west_los_angeles.ways.json";
            
            // Debug: List all resources in the prod_data directory
            try (InputStream is = getClass().getResourceAsStream("/prod_data")) {
                if (is == null) {
                    logger.warning("prod_data directory not found");
                } else {
                    logger.info("prod_data directory found");
                }
            } catch (IOException e) {
                logger.warning("Error checking prod_data directory: " + e.getMessage());
            }

            logger.info("Loading nodes from: " + nodesFile);
            List<Node> nodes = loadNodesFromResource(nodesFile);
            logger.info("Loaded " + nodes.size() + " nodes");
            
            logger.info("Loading ways from: " + waysFile);
            List<Way> ways = loadWaysFromResource(waysFile);
            logger.info("Loaded " + ways.size() + " ways");
            
            Graph graph = new Graph(nodes, ways);
            logger.info("Graph created with " + graph.getNodeCount() + " nodes and " + graph.getWayCount() + " ways");
            
            locationService = new InMemoryLocationService(graph);
            logger.info("LocationService created");

            mapService = new MapService(locationService, graph);
            logger.info("MapService created");  
            logger.info("Memory usage after servlet initialization: " + MemoryUtil.getMemoryUsage());
        } catch (Exception e) {
            logger.severe("Error initializing LocationServlet: " + e.getMessage());
            throw new ServletException("Error initializing LocationServlet", e);
        }

        // mapService = new MapService(locationService, locationService.getGraph());
        logger.info("LocationServlet initialized with " + locationService.getAllLocations().size() + " locations");
    }



    private List<Node> loadNodesFromResource(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return osmDataLoader.loadData(is, Node::fromMap);
        }
    }
    
    private List<Way> loadWaysFromResource(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return osmDataLoader.loadData(is, Way::fromMap);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        logger.info("GET request received: " + request.getRequestURL());
        logger.info("Servlet path: " + servletPath);

        try {
            switch (servletPath) {
                case "/locations" -> handleGetAllLocations(request, response);
                case "/route" -> handleRouteRequest(request, response);
                case "/nearest" -> handleNearestLocationRequest(request, response);
                case "/within-radius" -> handleLocationsWithinRadiusRequest(request, response);
                case "/search" -> handleSearchRequest(request, response);
                default -> {

                    request.setAttribute("cuisineTypesJson", gson.toJson(cuisineTypes));
                    request.setAttribute("shopTypesJson", gson.toJson(shopTypes));

                    // For JSTL use
                    request.setAttribute("cuisineTypes", cuisineTypes);
                    request.setAttribute("shopTypes", shopTypes);

                    // Add categories for the category select
                    List<String> categories = List.of("restaurant", "store");
                    request.setAttribute("categories", categories);
                    logger.info("Setting categories: " + categories);

                    // Forward the request to the index.jsp page
                    logger.info("Forwarding to index.jsp");
                    request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);

                    // Note: The JSP can now access these attributes using ${cuisineTypes} and ${shopTypes}
                    // These will be valid JSON strings that can be directly assigned to JavaScript variables
                }
            }
        } catch (Exception e) {
            logger.severe("Error processing GET request: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    private void handleGetAllLocations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Handling get all locations request");
        List<Location> locations = locationService.getAllLocations();
        logger.info("Found " + locations.size() + " locations");

        sendJsonResponse(response, locations);
    }

    private void handleRouteRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String startLatParam = request.getParameter("startLat");
        String startLonParam = request.getParameter("startLon");
        String endLatParam = request.getParameter("endLat");
        String endLonParam = request.getParameter("endLon");
        String routeType = request.getParameter("type"); 
        routeType = (routeType == null) ? "fastest" : routeType; // Default to fastest if not provided

        logger.info("Route request received - Start: " + startLatParam + ", " + startLonParam + 
                " End: " + endLatParam + ", " + endLonParam + " Type: " + routeType);

        if (startLatParam == null || startLonParam == null || endLatParam == null || endLonParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }

        try {
            double startLat = Double.parseDouble(startLatParam);
            double startLon = Double.parseDouble(startLonParam);
            double endLat = Double.parseDouble(endLatParam);
            double endLon = Double.parseDouble(endLonParam);

            logger.info("Start lat: " + startLat + ", start lon: " + startLon + ", end lat: " + endLat + ", end lon: " + endLon);
            Coordinates start = new Coordinates(startLat, startLon);
            Coordinates end = new Coordinates(endLat, endLon);

            Route route;
            if ("fastest".equals(routeType)) {
                route = mapService.calculateFastestRoute(start, end);
            } else {
                route = mapService.calculateShortestRoute(start, end);
            }

            if (route == null) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "No route found");
                return;
            }

            double distanceKm = route.getTotalDistance();
            double estimatedTimeMinutes = route.getEstimatedTime(routeType.equals(routeType));

            Map<String, Object> routeData = new HashMap<>();
            routeData.put("coordinates", route.getNodes().stream()
                                            .map(node -> Map.of("latitude", node.lat(), "longitude", node.lon()))
                                            .toList());
            // Add the new segments data with speed limits
            routeData.put("segments", route.getRouteSegments());
            // logger.info("Route segments: " + routeData.get("segments"));
            
            double distanceMiles = DistanceUtil.kmToMiles(distanceKm);
            routeData.put("distance", String.format("%.2f", distanceMiles)); // Convert to km and format
            logger.info("Distance: " + distanceMiles + " miles");
            
            routeData.put("estimatedTime", String.format("%.2f", estimatedTimeMinutes));
            logger.info("Estimated time: " + estimatedTimeMinutes + " minutes");

            sendJsonResponse(response, routeData);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid coordinate format");
        } catch (Exception e) {
            logger.severe("Error calculating route: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error calculating route");
        }
    }

    private void handleNearestLocationRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));
        Coordinates point = new Coordinates(lat, lon);

        Optional<Location> nearestLocation = mapService.findNearestLocation(point);
        if (nearestLocation.isPresent()) {
            sendJsonResponse(response, nearestLocation.get());
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "No locations found");
        }
    }

    private void handleLocationsWithinRadiusRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));
        double radiusKm = Double.parseDouble(request.getParameter("radius"));
        Coordinates center = new Coordinates(lat, lon);

        List<Location> locations = mapService.findLocationsWithinRadius(center, radiusKm);
        sendJsonResponse(response, locations);
    }

    private void handleSearchRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = request.getParameter("name");
        String category = request.getParameter("category");
        String type = request.getParameter("type");

        // Handle null values
        query = (query == null) ? "" : query.trim();
        category = (category == null) ? "" : category.trim();
        type = (type == null) ? "" : type.trim();

        double lat, lon, radiusKm;
        try {
            lat = Double.parseDouble(request.getParameter("lat"));
            lon = Double.parseDouble(request.getParameter("lon"));
            radiusKm = Double.parseDouble(request.getParameter("radius"));
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Invalid or missing lat/lon/radius parameters. Using default values.");
            lat = 34.0; // Default latitude
            lon = -118.3; // Default longitude
            radiusKm = 10.0; // Default radius
        }

        Coordinates center = new Coordinates(lat, lon);
        Coordinates adjustedCenter = center;

        if (BOUNDS_CHECKING_ENABLED) {
            adjustedCenter = mapService.adjustToBounds(center);
            if (!center.equals(adjustedCenter)) {
                logger.info("Search center adjusted from " + center + " to " + adjustedCenter + " to fit within available data bounds.");
            }
        }

        logger.info("Searching for " + (category.isEmpty() ? "all" : category) + " locations" + 
                    (type.isEmpty() ? "" : " of type " + type) + 
                    (query.isEmpty() ? "" : " with keyword: " + query) + 
                    " within radius " + radiusKm + " km from " + center);

        List<Location> locations = mapService.searchLocationsWithinRadiusAndKeyword(query, category, type, center, radiusKm);
        logger.info("Found " + locations.size() + " matching locations");
        

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("locations", locations);
        responseData.put("adjustedCenter", adjustedCenter);
        responseData.put("isTestEnvironment", IS_TEST_ENVIRONMENT);
        responseData.put("boundsCheckingEnabled", BOUNDS_CHECKING_ENABLED);

        sendJsonResponse(response, responseData);
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String jsonResponse = gson.toJson(data);
        logger.info("Sending JSON response: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    } 

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        gson.toJson(new ErrorResponse(message), response.getWriter());
    }
}