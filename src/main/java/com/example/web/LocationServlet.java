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
import com.example.model.Location;

@WebServlet(name = "LocationServlet", urlPatterns = {"/", "/locations"})
public class LocationServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(LocationServlet.class.getName());

    private LocationService locationService;

    @Override
    public void init() throws ServletException {
        LOG.info("LocationServlet initialized");
        locationService = new InMemoryLocationService(50);
        LOG.info("LocationServlet initialized with " + locationService.getAllLocations().size() + " locations");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String queryString = request.getQueryString();
        LOG.info("GET request received: " + request.getRequestURL());
        LOG.info("Servlet path: " + servletPath);
        LOG.info("Path info: " + pathInfo);

        try {
            if ("/locations".equals(servletPath)) {
                handleGetAllLocations(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOG.severe("Error processing GET request: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    private void handleGetAllLocations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.info("Handling get all locations request");
        List<Location> locations = locationService.getAllLocations();
        LOG.info("Found " + locations.size() + " locations");

        sendJsonResponse(response, locations);
    }


    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String jsonResponse = new Gson().toJson(data);
        LOG.info("Sending JSON response: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    } 

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        new Gson().toJson(new ErrorResponse(message), response.getWriter());
    }
}