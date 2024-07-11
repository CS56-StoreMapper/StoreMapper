package com.example.service;

import com.example.model.Coordinates;
import com.example.model.Route;

/**
 * Defines a strategy for calculating routes between two coordinates.
 */
public interface RouteStrategy {

    /**
     * Calculates a route between two coordinates.
     *
     * @param start The starting coordinates of the route.
     * @param end The ending coordinates of the route.
     * @return A Route object representing the calculated route.
     */
    Route calculateRoute(final Coordinates start, final Coordinates end);
}