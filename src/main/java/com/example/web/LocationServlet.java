package com.example.web;

import java.io.IOException;
import java.util.List;

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

@WebServlet(name = "LocationServlet", urlPatterns = {"/", "/locations", "/route"})
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
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String queryString = request.getQueryString();
        logger.info("GET request received: " + request.getRequestURL());
        logger.info("Servlet path: " + servletPath);
        logger.info("Path info: " + pathInfo);

        try {
            if ("/locations".equals(servletPath)) {
                handleGetAllLocations(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/route")) {
                logger.info("Handling route request");
                handleRouteRequest(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
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