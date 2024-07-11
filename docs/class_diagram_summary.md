# StoreMapper Class Diagram Overview

## Key Components

### Data Models

1. **`Location` (Abstract Class)**
   - Represents the base class for all locations in our system.
   - Contains common properties like `id`, `name`, and `coordinates`.
   - Defines an abstract method `getType()` to be implemented by subclasses.

2. **`Store` and `Restaurant` (Concrete Classes)**
   - Extend the `Location` class, representing specific types of locations.
   - Implement the `getType()` method to return their respective `LocationType`.

3. **`Coordinates`**
   - Encapsulates latitude and longitude.
   - Provides methods for distance calculation and finding destination points.

4. **`LocationType` (Enum)**
   - Defines the types of locations (`STORE`, `RESTAURANT`).

5. **`Route`**
   - Represents a route between two points.
   - Stores start and end coordinates, waypoints, and total distance.

6. **`NamedRoute`**
   - Extends `Route` with named start and end points.

7. **`Bounds`**
   - Represents a rectangular area on the map.
   - Used for querying locations within a specific region.

### Services

1. **`LocationService` (Interface)**
   - Defines methods for CRUD operations on locations.

2. **`InMemoryLocationService`**
   - Implements LocationService using in-memory storage.
   - Useful for testing and prototyping.

3. **`MapService`**
   - Core service that combines location data with spatial operations.
   - Uses LocationService for data access and RouteStrategy for route calculations.

4. **`RouteStrategy` (Interface)**
   - Defines the contract for route calculation algorithms.

5. **`SimpleRouteStrategy`**
   - A basic implementation of RouteStrategy.

### Web Components

1. **`LocationServlet`**
   - Handles HTTP requests related to locations.
   - Acts as a controller, delegating business logic to services.

### Utility Classes

1. **`LocationFactory`**
   - Creates Location objects based on type.

2. **`TestDataGenerator`**
   - Generates sample data for testing.

3. **`DistanceUtil`**
   - Provides utility methods for distance conversions.

4. **`ColoredConsoleHandler`**
   - Enhances logging with colored console output.

## Design Rationale

1. **Separation of Concerns**: The design separates data models, business logic (services), and web components, making the system modular and easier to maintain.

2. **Abstraction**: The use of abstract classes (`Location`) and interfaces (`LocationService`, `RouteStrategy`) allows for flexibility and future extensibility.

3. **Encapsulation**: Each class encapsulates its data and behavior, promoting information hiding and reducing dependencies.

4. **Open/Closed Principle**: The system is open for extension (e.g., new location types, route strategies) but closed for modification.

5. **Dependency Inversion**: High-level modules (`MapService`) depend on abstractions (`LocationService`, `RouteStrategy`), not concrete implementations.

## Service Layer Design

### Separation of LocationService and MapService

The separation of LocationService and MapService is based on the principles of separation of concerns and single responsibility:

- **LocationService**: Handles basic CRUD operations on Location objects, manages data persistence and retrieval, and provides methods for searching locations based on criteria.

- **MapService**: Offers higher-level, map-specific operations, utilizes LocationService for data access, implements spatial operations, calculates routes, and combines multiple LocationService operations for complex functionalities.

This separation provides benefits such as clear code organization, easier maintenance, improved testability, flexibility to swap implementations, and better scalability.

### RouteStrategy and Route Design

- `RouteStrategy` is designed to work with `Coordinates` rather than `Locations` for flexibility, reusability, and separation of concerns.
- `Route` focuses on geographical data (coordinates), while `NamedRoute` extends it with naming information, allowing for versatility in different contexts.

## Proposed Implementation Approach

1. **Initial Setup**:
   - Set up the project structure with Maven
   - Configure basic Jakarta EE and Tomcat 10 settings

2. **Core Backend Components**:
   - Implement a simple `Location` class
   - Create a `LocationService` interface
   - Develop a basic in-memory implementation of `LocationService`
   - Implement `LocationServlet` to handle HTTP requests

3. **Basic Frontend** (in parallel with backend development):
   - Create a basic HTML page (index.jsp)
   - Integrate Leaflet.js for map functionality
   - Implement simple JavaScript to interact with the backend API

4. **Integration**:
   - Connect the frontend to the `LocationServlet`
   - Implement basic CRUD operations through the UI

5. **Iterative Enhancement**:
   - Gradually add more complex backend features (e.g., `MapService`, `RouteStrategy`)
   - Enhance the frontend with additional features as they become available in the backend
   - Implement more sophisticated data models (`Store`, `Restaurant`, etc.) as needed

6. **Ongoing Tasks**:
   - Set up and maintain project configuration (Maven, web.xml, etc.)
   - Implement unit tests for new components as they are developed
   - Refine and optimize existing code

7. **Advanced Features** (as needed):
   - Implement more complex routing algorithms
   - Add database persistence (transition from in-memory to database storage)
   - Enhance search functionality and spatial queries

This approach allows for parallel development of frontend and backend, early integration, and iterative improvement. It provides a flexible framework that can be adapted as the project evolves and requirements become clearer.