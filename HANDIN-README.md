# CS56 Web Application

## Deployed Application on DigitalOcean

[StoreMapper](http://64.23.180.16/)

## A. Names of Group Members

Carson Bell
Wei Che
David Dickinson
Beckham Lee
Adityan Vairavel

## B. Java Version

JDK 21.0.3 (OpenJDK on Ubuntu/WSL2 and DigitalOcean, Oracle JDK LTS on Windows)

## C. IDEs

1. Eclipse IDE for Enterprise Java and Web Developers (includes Incubating components)
   Version: 2024-06 (4.32.0)
   Build id: 20240606-1231

2. Visual Studio Code
   Version: 1.89.1

3. IntelliJ IDEA 2024.1.4 (Ultimate Edition)


## D. Challenges Faced

### Integrating OpenStreetMap (OSM) data structures

- Transitioning from a simple Location model to a more complex OSM-based system with Nodes and Ways.
- Used Python starter code from the 6.009 problem set as a template for converting OSM data to Java objects
- Creating `Node` and `Way` classes to represent the OSM data
- Implementing a `Graph` class to represent the OSM data, adapting `Route` to work with the new `Graph`

### Redesigning the service layer

- Updating `MapService` and `LocationService` to work with the new OSM-based graph structure
- Ensuring backwards compatibility with the original `MapService` and `LocationService` implementations

### Implementing Pathfinding

- Implementing Dijkstra's algorithm to find the shortest path between two locations
- Utilizing way tags about speed limits & types of roadway to map way segments to speed limits, then using these in a modified Dijkstra's algorithm to find the fastest path between two locations

### UI/UX Challenges

- Implementing interactive route display using Leaflet.js
  - Learning the specfics of Leaflet controls, layers, and markers
- Adding styling to elements integrated with Leaflet for improved UI/UX
  - Route Information (distance, estimated time)
  - Color-coded route segments based on speed limit
  - Clickable markers on locations
- Allow for search in categories "Store" & "Restaurant", and categories specific to each of those categories
  - Extracted unique sets of these from the OSM data, challenges passing along those from the backend to the front-end
- Challenges with JSP templating

### Testing & Validation

- Adapted MIT tests + ground-truth data (provided for students to test against in the problem set for small/medium/large OSM data [MIT/Midwest/Cambridge]) to verify correctness of Shortest-paths and Fastest-paths
- Additional tests to validate MapService, LocationService, Graph, Coordinates

### Data & Memory Management

- We used data (maps + ground truth paths) from the "Frugal Maps" Fall 2020 MIT 6.009 problem set
    - Large files (~ 500 MB for nodes, ~ 180 MB for ways) were initially stored in the `/data` directory and in GitHub using LFS
    - But running continuous integration tests by GitHub workflow led to the data being redownloaded multiple times and used up the group's 1.5 GB LFS quota
    - So we moved the data to Cloudflare R2 and had the workflow download the data on demand (similarly the data should be downloaded by a user when running the application tests locally for the first time)
- Another issue that we ran into was that the code runner on GitHub would go OOM when trying to load these large nodes and ways files
  - So we needed to implement a chunking & garbage collection strategy in `INMemoryLocationService` to prevent the heap from being exhausted, which is triggered by setting a flag in the GitHub workflow

### Environment Setup

- Configuring Tomcat for different environments (standalone in Ubuntu, Eclipse integrated Tomcat, command line in WSL2 / Digital Ocean)
- Understanding and managing environment variables (`JAVA_HOME`, `CATALINA_HOME`) across different setups

### Deployment

- Deploying the application as `ROOT.war` to serve at the root context (manually by CLI for Ubuntu+WSL2, Digital Ocean)
- Configuring Eclipse to serve the application at the root context


## E. Anything not working? If so, what is it and what have you tried to get it to work?


- Performance issues:
  - Slow performance when loading and processing very large OSM datasets
  - For now limited to small/medium datasets (~ 100 MB nodes, ~ 50 MB ways)
  - For larger datasets, we'd need to implement a more efficient chunking strategy, spatial indexing to find nearest node to given coordinates
- Memory management:
  - Search without any qualifiers on the 4GB DO droplet leads to Heap overflow errors
- UI Responsiveness
  - Frontend will crash if the user loads all of the locatoins in WestLA at once
    - We would need to implement a level-of-detail (LOD) strategy to load only the visible locations at once
    - We could use a similar strategy to Google Maps's LOD to load the OSM data in a similar way (e.g. load the nearest 100 nodes to the current viewport, then the nearest 100 nodes to the next viewport, prune the rest)


## F. Future Work

1. Database Integration:
   - Move from an in-memory `MapService` to a database-backed solution
   - Implement PostGIS + PostgreSQL to store and query OSM data efficiently
   - Develop a data access layer to handle database operations
   - Optimize spatial queries using PostGIS functions
2. Performance Optimization:
   - Implement advanced spatial indexing (e.g., R-tree) for faster nearest-neighbor queries
   - Develop a caching strategy for frequently accessed routes and locations
   - Optimize pathfinding algorithms for larger datasets
3. Advanced Routing Features:
   - Implement multi-modal routing (e.g., combining walking, cycling, and public transport)
   - Add support for real-time traffic data in route calculations
   - Develop an algorithm for optimizing multi-stop routes
4. User Experience Improvements:
   - Implement a more sophisticated UI with responsive design for mobile devices
   - Develop a user account system for saving favorite locations and routes
   - Add support for user-generated content (e.g., reviews, ratings for locations)
5. Data Management and Updates:
   - Develop a system for regular updates of OSM data
   - Implement a data validation and cleaning pipeline for user-submitted location data
   - Create an admin interface for managing location data and user content