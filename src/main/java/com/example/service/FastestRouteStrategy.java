package com.example.service;

import com.example.model.FastestPathFinder;
import com.example.model.Graph;
import com.example.model.Node;
import com.example.model.Route;

import java.util.List;

public class FastestRouteStrategy implements RouteStrategy {
    private Graph graph;
    private FastestPathFinder pathFinder;

    public FastestRouteStrategy(Graph graph) {
        setGraph(graph);
    }

    @Override
    public Route calculateRoute(Node start, Node end) {
        List<Node> path = pathFinder.findFastestPath(start, end);
        return path != null ? new Route(path, graph) : null;
    }

    @Override
    public void setGraph(Graph graph) {
        this.graph = graph;
        this.pathFinder = new FastestPathFinder(graph);
    }
}
