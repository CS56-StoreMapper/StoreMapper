package com.example.service;

import com.example.model.Route;
import com.example.model.Node;
import com.example.model.Coordinates;

import java.util.List;

public class SimpleRouteStrategy implements RouteStrategy {
    @Override
    public Route calculateRoute(Node start, Node end) {
        // Simple direct route
        return new Route(List.of(start, end));
    }

    @Override
    public Route calculateRouteFromCoordinates(Coordinates start, Coordinates end) {
        // Override the default method for efficiency
        Node startNode = new Node(-1, start.getLatitude(), start.getLongitude());
        Node endNode = new Node(-2, end.getLatitude(), end.getLongitude());
        return new Route(List.of(startNode, endNode));
    }
}