package com.example.service;

import com.example.model.*;

import java.util.List;

public class DijkstraRouteStrategy implements RouteStrategy {
    private Graph graph;
    private DijkstraPathFinder pathFinder;

    public DijkstraRouteStrategy(Graph graph) {
        setGraph(graph);
    }

    @Override
    public Route calculateRoute(Node start, Node end) {
        List<Node> path = pathFinder.findShortestPath(start, end);
        if (path == null || path.isEmpty()) {
            return null;
        }
        return new Route(path);
    }

    @Override
    public void setGraph(Graph graph) {
        this.graph = graph;
        this.pathFinder = new DijkstraPathFinder(graph);
    }
}