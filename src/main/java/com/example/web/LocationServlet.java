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

@WebServlet("/locations")
public class LocationServlet extends HttpServlet {
    // Logger for logging errors
    private static final Logger LOG = Logger.getLogger(LocationServlet.class.getName());

    private LocationService locationService;

    @Override
    public void init() throws ServletException {
        locationService = new InMemoryLocationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        LOG.info("GET request received: " + request.getRequestURL());
        
        List<Location> locations = locationService.getAllLocations();
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Gson gson = new Gson();
        String jsonLocations = gson.toJson(locations);
        
        response.getWriter().write(jsonLocations);
    }
}