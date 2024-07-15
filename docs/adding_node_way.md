# Updated Integration Plan for OSM Data Structures

## 1. Modify Node Class ✅ Completed
- Converted to record
- Added toCoordinates() method
- Implemented getTag(String key) method

## 2. Enhance Location Class ✅ Completed
- Added optional Node field
- Implemented constructors for both OSM and non-OSM locations
- Added methods to access OSM data when available

## 3. Update Route Class 🔄 In Progress
- Modify to use Node objects instead of Coordinates
- Update distance calculation methods

## 4. Create Graph Class ✅ Completed
- Implemented using Node and Way objects
- Added methods for pathfinding and routing

## 5. Update Existing Classes
- Way ✅ Completed (converted to record)
- Coordinates ✅ No changes needed

## 6. Service Layer Updates
- OSMDataService ✅ Completed
- MapService 🔄 In Progress
  - Updated to use the new Graph class for routing
  - Implement methods to convert between Location and Node

## 7. Data Loading and Processing ✅ Completed
- OSMDataService now handles loading and processing efficiently

## 8. UI and API Updates 🔲 To Be Implemented
- Update to work with enhanced Location class
- Add functionality to display OSM data when available

## 9. Testing 🔄 In Progress
- Update existing tests for new Location and Node structures
- Add tests for Graph and updated Route classes
- Implement integration tests for OSM data flow

## 10. Documentation 🔄 In Progress
- Update JavaDocs for modified classes
- Create overall architecture documentation explaining OSM integration

## 11. Performance Optimization 🔲 To Be Implemented
- Implement spatial indexing for efficient nearest-node queries
- Profile and optimize Graph operations

## 12. Additional Features 🔲 To Be Considered
- Implement tag-based filtering for locations
- Add support for OSM relations if needed

## 13. Pathfinding Algorithm Implementation 🔄 In Progress
- Implement A* or Dijkstra's algorithm in the Graph class
- Optimize for performance and accuracy

## 14. Route Class Enhancements 🔲 To Be Implemented
- Add methods to calculate total distance and estimated time
- Implement turn-by-turn directions generation

## 15. MapService Integration 🔄 In Progress
- Fully integrate Graph and Route classes into MapService
- Implement methods to find nearest nodes for non-OSM locations

## 16. Error Handling and Edge Cases 🔲 To Be Implemented
- Handle scenarios where no route is found
- Implement fallback mechanisms for incomplete OSM data

## 17. Caching and Performance 🔲 To Be Considered
- Implement caching for frequently requested routes
- Optimize graph traversal for large datasets

## 18. User Interface for Route Display 🔲 To Be Implemented
- Create UI components to display calculated routes
- Implement interactive features (e.g., zooming, panning)

## 19. API Endpoints for Routing 🔲 To Be Implemented
- Create RESTful endpoints for route calculation
- Implement request/response formats for routing data

## 20. Logging and Monitoring 🔲 To Be Implemented
- Add logging for pathfinding operations
- Implement performance monitoring for route calculations

# Conceptual Implementation of Pathfinding / Route Creation

```java
public class Graph {
    private Map<Long, Node> nodes;
    private List<Way> ways;
    private Map<Long, Set<Way>> adjacencyList;

    public Graph(List<Node> nodes, List<Way> ways) {
        this.nodes = nodes.stream().collect(Collectors.toMap(Node::getId, n -> n));
        this.ways = ways;
        buildAdjacencyList();
    }

    private void buildAdjacencyList() {
        adjacencyList = new HashMap<>();
        for (Way way : ways) {
            adjacencyList.computeIfAbsent(way.getStartNode().getId(), k -> new HashSet<>()).add(way);
            // If the way is not one-way, add the reverse direction
            if (!"yes".equals(way.getTags().get("oneway"))) {
                adjacencyList.computeIfAbsent(way.getEndNode().getId(), k -> new HashSet<>())
                    .add(new Way(way.getEndNode(), way.getStartNode(), way.getData()));
            }
        }
    }

    public Route findRoute(Node start, Node end) {
        // Implement A* or Dijkstra's algorithm here
        List<Node> path = runPathfindingAlgorithm(start, end);
        return new Route(path);
    }

    private List<Node> runPathfindingAlgorithm(Node start, Node end) {
        // Implementation of A* or Dijkstra's algorithm
        // This is a placeholder for the actual implementation
        return new ArrayList<>();
    }
}

public class MapService {
    private Graph graph;

    public MapService(List<Node> nodes, List<Way> ways) {
        this.graph = new Graph(nodes, ways);
    }

    public Route calculateRoute(Node start, Node end) {
        return graph.findRoute(start, end);
    }
}
```