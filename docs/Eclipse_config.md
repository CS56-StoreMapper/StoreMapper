# Eclipse Configuration for StoreMapper Project

## Project Setup
1. JDK Version: 21
2. Eclipse Version: 2023-09 (4.29) or later
3. Tomcat Version: 10.1.24

## Eclipse Workspace Settings
1. Java Build Path:
   - JRE System Library: JavaSE-21

2. Project Facets:
   - Java: 21
   - Dynamic Web Module: 5.0
   - JavaScript: 1.0

3. Web Project Settings:
   - Context root: /

## Server Configuration (Tomcat)
1. Server Runtime Environment: apache-tomcat-10.1.24
2. Server name: apache-tomcat-10.1.24 at localhost
3. Host name: localhost
4. Server Ports:
   - HTTP/1.1: 8081
   - Tomcat admin port: 8006

5. Deployment Assembly:
   - Ensure all necessary source folders and dependencies are properly mapped

6. Module Configuration:
   - Path: / (root context)

## Maven Configuration
1. Update project's pom.xml:
   ```xml
   <properties>
       <maven.compiler.source>21</maven.compiler.source>
       <maven.compiler.target>21</maven.compiler.target>
   </properties>
   ```

2. Ensure all dependencies are correctly specified in pom.xml

## Project-Specific Settings
1. Java Compiler:
   - Compiler compliance level: 21

2. Project Facets:
   - Java: 21
   - Dynamic Web Module: 5.0

3. Deployment Assembly:
   - Verify all necessary source folders and Maven dependencies are included

## Run Configuration
1. Server: apache-tomcat-10.1.24 at localhost
2. Start URL: http://localhost:8081/

## Step-by-Step Eclipse UI Configuration for StoreMapper

1. Open Eclipse and select your workspace.

2. Import the StoreMapper project:
   - File > Import > Maven > Existing Maven Projects
   - Browse to your project root and select the pom.xml file

3. Configure JDK:
   - Right-click project > Properties > Java Build Path
   - Libraries tab > JRE System Library > Edit
   - Select Workspace default JRE (JavaSE-21)

4. Set up Project Facets:
   - Right-click project > Properties > Project Facets
   - Check Java (21), Dynamic Web Module (5.0), JavaScript (1.0)
   - Click "Further configuration available..." for Dynamic Web Module
   - Set Content directory to src/main/webapp
   - Click OK

5. Configure Web Project Settings:
   - Right-click project > Properties > Web Project Settings
   - Set Context root to /
   - Click Apply and Close

6. Set up Tomcat Server:
   - Window > Show View > Servers
   - Right-click > New > Server
   - Choose Apache > Tomcat v10.1 Server
   - Set Server name: apache-tomcat-10.1.24 at localhost
   - Click Next, add StoreMapper project, Finish

7. Configure Tomcat:
   - Double-click the server in Servers view
   - Set HTTP/1.1 port to 8081
   - Set Admin port to 8006
   - Save and close

8. Verify Maven Configuration:
   - Open pom.xml
   - Ensure compiler source and target are set to 21
   - Save changes

9. Set Java Compiler:
   - Right-click project > Properties > Java Compiler
   - Enable project-specific settings
   - Set Compiler compliance level to 21
   - Click Apply and Close

10. Configure Deployment Assembly:
    - Right-click project > Properties > Deployment Assembly
    - Verify src/main/webapp is mapped to /
    - Add Maven Dependencies if not present

11. Set Run Configuration:
    - Right-click project > Run As > Run on Server
    - Choose existing Tomcat server
    - Set Start URL to http://localhost:8081/
    - Click Finish

12. Final Steps:
    - Right-click Tomcat server > Clean
    - Right-click Tomcat server > Publish

13. Test the setup:
    - Start the server
    - Open a browser and navigate to http://localhost:8081/

## Troubleshooting

If you encounter build path errors or issues with Java release 21, try the following:

1. Ensure JDK 21 is installed on your system.

2. Configure Eclipse to use JDK 21:
   - Window > Preferences > Java > Installed JREs
   - Add JDK 21 if not listed and set as default

3. Update Maven configuration:
   - Right-click project > Properties > Maven
   - Check "Resolve dependencies from Workspace projects"

4. Clean and update the project:
   - Right-click project > Maven > Update Project
   - Check "Force Update of Snapshots/Releases"
   - Click OK

5. Rebuild the project:
   - Project > Clean... > Clean all projects

6. Verify Eclipse version:
   - Ensure you're using Eclipse version 2023-09 (4.29) or later

7. Check Tomcat configuration:
   - Verify Tomcat 10.1.24 is properly set up in Eclipse

8. Maven installation:
   - Verify Maven is correctly installed and configured in Eclipse
   - Try running `mvn clean install` from the command line in the project directory

## Additional Notes
- Ensure Tomcat 10.1.24 is compatible with your local setup
- Clean and republish the server after making configuration changes
- If issues persist, try deploying as ROOT.war in Tomcat's webapps directory