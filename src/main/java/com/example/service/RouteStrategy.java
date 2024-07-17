package com.example.service;

import com.example.model.Graph;
import com.example.model.Route;
import com.example.model.Node;

/**
 * Defines a strategy for calculating routes between two points.
 */
public interface RouteStrategy {

   /**
     * Calculates a route between two nodes.
     *
     * @param start The starting node of the route.
     * @param end The ending node of the route.
     * @return A Route object representing the calculated route.
     */
    Route calculateRoute(Node start, Node end);

    /**
     * Sets the graph to be used for route calculations.
     * This method allows for updating the graph if needed.
     *
     * @param graph The graph representing the road network.
     */
    void setGraph(Graph graph);
}