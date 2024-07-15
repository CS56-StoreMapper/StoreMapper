package com.example.service;

import com.example.model.Coordinates;
import com.example.model.Route;
import com.example.model.Node;

/**
 * Defines a strategy for calculating routes between two coordinates.
 */
@FunctionalInterface
public interface RouteStrategy {

    /**
     * Calculates a route between two coordinates.
     *
     * @param start The starting coordinates of the route.
     * @param end The ending coordinates of the route.
     * @return A Route object representing the calculated route.
     */
    Route calculateRoute(Node start, Node end);

    /**
     * Default method to calculate route using Coordinates.
     * This method can be used for backward compatibility or when only Coordinates are available.
     *
     * @param start The starting coordinates of the route.
     * @param end The ending coordinates of the route.
     * @return A Route object representing the calculated route.
     */
    default Route calculateRouteFromCoordinates(Coordinates start, Coordinates end) {
        Node startNode = new Node(-1, start.getLatitude(), start.getLongitude());
        Node endNode = new Node(-2, end.getLatitude(), end.getLongitude());
        return calculateRoute(startNode, endNode);
    }
}