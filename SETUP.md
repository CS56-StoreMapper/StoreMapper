# StoreMapper Setup Guide

This guide will walk you through the process of setting up your development environment for the StoreMapper project.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

1. Java Development Kit (JDK) 11 or higher
2. Apache Maven
3. Apache Tomcat 10
4. Git
5. An Integrated Development Environment (IDE) of your choice (e.g., IntelliJ IDEA, Eclipse)

## Step-by-Step Setup

1. Clone the repository:

```sh
git clone https://github.com/CS56-StoreMapper/StoreMapper.git
cd StoreMapper
```


2. Set up Apache Tomcat 10:
   - Download Apache Tomcat 10 from the official website
   - Extract the downloaded file to a location on your computer
   - Set the CATALINA_HOME environment variable to point to your Tomcat installation directory

3. Configure your IDE:
   - Open the project in your IDE
   - Set up the project as a Maven project
   - Configure Tomcat server in your IDE:
     - For IntelliJ IDEA: File > Settings > Build, Execution, Deployment > Application Servers > Add > Tomcat Server
     - For Eclipse: Window > Preferences > Server > Runtime Environments > Add > Apache Tomcat v10.0

4. Build the project:

```sh
mvn clean install
```

5. Run the application:
   - In your IDE, run the project on the Tomcat server
   - Alternatively, you can deploy the WAR file manually:

```sh
cp target/storemapper.war $CATALINA_HOME/webapps
$CATALINA_HOME/bin/startup.sh
```

6. Access the application:
   - Open a web browser and go to `http://localhost:8080/storemapper`

## Troubleshooting

- If you encounter any "Class not found" errors, ensure that all dependencies are correctly downloaded by Maven
- For any server-related issues, check the Tomcat logs in `$CATALINA_HOME/logs/`

## Next Steps

- Familiarize yourself with the project structure as outlined in the CONTRIBUTING.md file
- Read through the README.md for an overview of the project
- Start working on your assigned tasks or pick an issue from the project board

If you encounter any problems during setup, please reach out to the team for assistance.