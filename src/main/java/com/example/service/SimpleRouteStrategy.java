package com.example.service;

import com.example.model.Route;
import com.example.model.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class SimpleRouteStrategy implements RouteStrategy {
    @Override
    public Route calculateRoute(Coordinates start, Coordinates end) {
        Route route = new Route(start, end);
        
        // Simple direct route
        List<Coordinates> waypoints = new ArrayList<>();
        waypoints.add(start);
        waypoints.add(end);
        route.setWaypoints(waypoints);
        
        route.calculateTotalDistance();
        return route;
    }
}