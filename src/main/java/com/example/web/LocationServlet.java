package com.example.web;

import java.io.IOException;
import java.util.List;
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
import com.example.model.Location;
import com.example.model.Route;
import com.example.service.MapService;

@WebServlet(name = "LocationServlet", urlPatterns = {"/", "/locations", "/route", "/nearest", "/within-radius", "/search"})
public class LocationServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LocationServlet.class.getName());

    private LocationService locationService;
    private MapService mapService;

    @Override
    public void init() throws ServletException {
        logger.info("LocationServlet initialized");
        locationService = new InMemoryLocationService(40);
        mapService = new MapService(locationService, locationService.getGraph());
        logger.info("LocationServlet initialized with " + locationService.getAllLocations().size() + " locations");
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
                default -> request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
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

        if (startLatParam == null || startLonParam == null || endLatParam == null || endLonParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }

        try {
            double startLat = Double.parseDouble(startLatParam);
            double startLon = Double.parseDouble(startLonParam);
            double endLat = Double.parseDouble(endLatParam);
            double endLon = Double.parseDouble(endLonParam);

            Coordinates start = new Coordinates(startLat, startLon);
            Coordinates end = new Coordinates(endLat, endLon);

            Route route = mapService.calculateRoute(start, end);
            sendJsonResponse(response, route);
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
        String query = request.getParameter("query");
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));
        double radiusKm = Double.parseDouble(request.getParameter("radius"));
        Coordinates center = new Coordinates(lat, lon);

        logger.info("Searching for locations with keyword: " + query + " within radius " + radiusKm + " km from " + center);
        List<Location> locations = mapService.searchLocationsWithinRadiusAndKeyword(query, center, radiusKm);
        sendJsonResponse(response, locations);
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String jsonResponse = new Gson().toJson(data);
        logger.info("Sending JSON response: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    } 

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        new Gson().toJson(new ErrorResponse(message), response.getWriter());
    }
}