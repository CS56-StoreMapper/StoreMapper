# StoreMapper

StoreMapper is a web application for locating and retrieving information about various types of locations, including stores and restaurants. It allows users to search for locations, view details, find the nearest location, and get routes to desired destinations.

![`docs/images/screenshots/StoreMapper_UI.png`](docs/images/screenshots/StoreMapper_UI.png)

## Features

- Search for locations by name or type
- View location details
- Find the nearest location to user's position
- Get routes between locations
- Add new locations to the system

## Technology Stack

- Backend: Java, Jakarta EE, Tomcat 10, Maven
- Frontend: HTML5, CSS3, JavaScript, Leaflet.js
- Database: PostgreSQL with PostGIS extension (planned for future implementation)
- Map Data: OpenStreetMap

## Project Structure

The application demonstrates a clear separation of concerns across its layers:

1. UI Layer: HTML, CSS, and JavaScript with Leaflet.js for map functionality
2. Networking Layer: Java servlets for handling HTTP requests
3. Business Logic Layer: Core services for spatial operations and routing
4. Data Access Layer: Interface for data persistence (currently in-memory, future database implementation planned)

## Setup Instructions

1. Clone the repository:

2. Ensure you have the following installed:
   - Java Development Kit (JDK) 21
   - Apache Maven
   - Apache Tomcat 10.1

3. Build the project:
```sh
mvn clean install
```

4. Deploy the WAR file to Tomcat:
   - Copy the generated `target/storemapper.war` to your Tomcat's `webapps` directory

5. Start Tomcat and access the application at `http://localhost:8080/storemapper`

## Design Documentation

Our project includes several key design documents:

1. [`docs/architecture.md`](docs/architecture.md): A comprehensive overview of the StoreMapper architecture, including the class diagram, key components, data flow, design decisions, and future considerations. This document reflects the current state of the project, including the integration of OpenStreetMap data structures.

2. [`docs/ClassDiagramPostOSM.puml`](docs/ClassDiagramPostOSM.puml): A UML class diagram representing the current project structure. To view this file, use a PlantUML viewer or online tools like [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/).

3. [`docs/class_diagram_summary.md`](docs/class_diagram_summary.md): A detailed explanation of the pre-OSM class diagram, including rationale for design decisions and implementation approach. While some aspects may be outdated, it provides historical context for the project's evolution.

4. [`docs/adding_node_way.md`](docs/adding_node_way.md): An integration plan and roadmap for OSM data structures, reflecting the current state of the project and future development plans.

These documents serve as a reference for our project's architecture and design principles. Please refer to them when making significant changes to the project structure.

## Development Roadmap

1. Implement core location management functionality
2. Develop basic route finding capabilities
3. Create user interface with map integration
4. Implement advanced search and filtering options
5. Optimize route finding algorithms
6. Transition from in-memory storage to PostgreSQL with PostGIS
7. Implement data import process for OpenStreetMap data

For a more detailed breakdown of our current development status and future plans, including OSM integration and performance optimizations, please refer to `docs/adding_node_way.md`.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.