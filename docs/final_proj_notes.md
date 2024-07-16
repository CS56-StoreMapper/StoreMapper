## Deploying StoreMapper to Tomcat:

### Build the WAR file

```sh
mvn clean package
```

### Copy the WAR file to the Tomcat webapps directory

```sh
sudo cp target/storemapper.war /opt/tomcat/webapps/
```

### Deploy via VSCode

Right-click on the tomcat server, select "Deploy


`$CATALINA_HOME/bin/startup.sh`
`$CATALINA_HOME/bin/shutdown.sh`

`tail -f $CATALINA_HOME/logs/catalina.out`


## Migrating Tomcat from 9.0 to 10.0

<https://tomcat.apache.org/migration-10.html>

<https://stackoverflow.com/questions/65703840/how-to-properly-configure-jakarta-ee-libraries-in-maven-pom-xml-for-tomcat>
- Was key to update the pom.xml file to include the jakarta.servlet dependencies
- Also had to update the web.xml file to use the correct version of the servlet API


## Maven

### Install but skip tests

`mvn install -DskipTests`

## OSM
<https://wiki.openstreetmap.org/wiki/Overpass_API#Quick_Start_(60_seconds):_Interactive_UI>
<https://osm2pgsql.org/>

### Conversion

#### OSMConverter

`mvn exec:java@osm-converter -Dexec.args="data/planet_-118.458_33.966_dc3ecec2.osm"`

#### Pickle to JSON

`python src/main/java/com/example/tools/pickle_to_json.py <input_pickle_file> <output_json_file>`

#### JSON to Java Serialized

`mvn exec:java@json-to-java-serialized -Dexec.args="data/mit.nodes.json data/mit.nodes.ser"`

#### Read Serialized File

`mvn exec:java@serialized-file-reader -Dexec.args="data/mit.nodes.ser"`

## Google Maps
<https://developers.google.com/maps/documentation/places/web-service/overview>

## JUnit

`mvn test -Dtest=CoordinatesTest`
`mvn test -Dtest=CoordinatesTest#testMethodName`



# Design

