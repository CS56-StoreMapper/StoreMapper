# StoreMapper

StoreMapper is a web application for locating and retrieving information about various types of locations, including stores and restaurants. It allows users to search for locations, view details, find the nearest location, and get routes to desired destinations.

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
   - Java Development Kit (JDK) 11 or higher
   - Apache Maven
   - Apache Tomcat 10

3. Build the project:
```sh
mvn clean install
```

4. Deploy the WAR file to Tomcat:
   - Copy the generated `target/storemapper.war` to your Tomcat's `webapps` directory

5. Start Tomcat and access the application at `http://localhost:8080/storemapper`

## Design Documentation

Our project includes two key design documents:

1. `docs/ClassDiagramPreOSM.puml`: A UML class diagram representing the project structure. To view this file, use a PlantUML viewer or online tools like [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/).

2. `docs/class_diagram_summary.md`: A detailed explanation of the class diagram, including rationale for design decisions and implementation approach.

These documents serve as a reference for our project's architecture and design principles. Please refer to them when making significant changes to the project structure.

## Development Roadmap

1. Implement core location management functionality
2. Develop basic route finding capabilities
3. Create user interface with map integration
4. Implement advanced search and filtering options
5. Optimize route finding algorithms
6. Transition from in-memory storage to PostgreSQL with PostGIS
7. Implement data import process for OpenStreetMap data

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.