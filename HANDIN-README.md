# CS56 Web Application: StoreMapper

## Introduction
StoreMapper is a web-based application desgined to manage and visualize location data, primarily focusing on stores and restaurants. It uses OpenStreetMap (OSM) data structures for its mapping and routing features.

### Project Overview
StoreMapper utilizes Leaflet.js to display an interactive map to users. Users are able to use the map to both search and filter for locations based on OSM tags. After locations are selected, the application determines the shortest and fastest path between locations using Djikstra's algorithm.

### Key Features and Achievements
1. OSM Data Integration
2. Advanced Routing Algorithms
3. Interactive UI
4. Scalable Architecture
5. Efficient Data Handling
6. Comprehensive Testing
7. Cross-Environment Compatibility
8. Performance Optimization

## A. Names of Group Members
* Carson Bell
* Wei Che
* David Dickinson
* Beckham Lee
* Adityan Vairavel

## B. Java Version
JDK 21.0.3 (OpenJDK on Ubuntu/WSL2 and DigitalOcean, Oracle JDK LTS on Windows)

## C. IDEs
* Eclipse IDE for Enterprise Java and Web Developers (includes Incubating components)
   * Version: 2024-06 (4.32.0)
   * Build ID: 20240606-1231
* IntelliJ IDEA 2024.1.4 (Ultimate Edition) 

## D. Challenges Faced
### Integrating OpenStreetMap (OSM) data structures
We needed to transition from a simple Location model to a more complex system based on OpenStreetMap (OSM) data by incorporating Nodes and Ways. We used Python starter code from the 6.009 problem set as a template to convert OSM data into Java objects. We also had to separately create Node and Way classes to represent OSM data, as well as implement a Graph class to represent the data.

### Redesigning the service layer
We needed to update MapService and LocationService to work with the OSM-based graph structure. Additionally, we had to ensure backwards compatibility with the original MapService and LocationService implementations.

### Implementing Pathfinding
We used Djikstra's algorithm to find the shortest path between two locations. To do this, we utilized way tags about speed limits and types of roadways to map way segments to speed limits. We then ultimately used these in a modified Djikstra's algorithm to find the fastest path between two locations.

### UI/UX Challenges
We needed to incorporate an interactive route display using Leaflet.js, which consisted of learning the specifics of Leaflet controls, layers, and markers. We also needed to add styling to certain elements for a cleaner UI: route information, color-coded route segments, and clickable markers on locations. We also implemented a search feature for the "Store" and "Restaurant" categories, which involved extracting unique sets from the OSM data and passing this data from the backend to the front-end. Finally, we encountered challenges learning how to use the JavaServer Pages (JSP) templating.

### Testing & Validation
- [Points about testing challenges]

### Data & Memory Management
Our maps and ground truth paths were sourced from the "Frugal Maps" Fall 2020 MIT 6.009 problem set. The large files in this dataset (~ 500 MB for nodes, ~ 180 MB for ways) were placed into the /data directory and in Github's Large File Storage (LFS) system. However, continuous integration tests by GitHub workflow led to the data being constantly redownloaded, which ate through our 1.5GB LFS quota. To resolve this, we moved the data to Cloudflare R2 and had to workflow download data on-demand.

### Environment Setup
We configured Tomcat to run in different environments (standalone in Ubuntu, Eclipse integrated Tomcat, command line in WSL2 / Digital Ocean). This involved understanding and managing environment variables, like JAVA_HOME and CATALINA_HOME across different setups.

### Deployment
We deployed the application as ```ROOT.war``` to serve at the root context. This had to be done manually in the command line for Ubuntu+WSL2 and Digital Ocean. Additionally, we configured Eclipse to serve the application at the root context.

## E. Current Limitations and Attempted Solutions
- Performance issues
- Memory management
- UI Responsiveness

## F. Future Work
1. Database Integration
2. Performance Optimization
3. Advanced Routing Features
4. User Experience Improvements
5. Data Management and Updates

## Conclusion
- Key learnings from the project
- Skills gained
- Potential for future development
 
