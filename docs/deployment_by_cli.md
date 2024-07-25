## Deploying StoreMapper to Tomcat via CLI

### Prerequisites
- Java 21 installed
- Maven installed
- Tomcat 10 installed

### Build the Application
1. Clean and install the project:
   ```sh
   mvn clean install
   ```

### Configure Tomcat to use Java 21
1. Edit the Tomcat service file:
   ```sh
   sudo nano /etc/systemd/system/tomcat10.service
   ```
2. Update the JAVA_HOME environment variable:
   ```
   Environment=JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
   ```
3. Save and exit the editor

### Deploy the Application
1. Copy the WAR file to Tomcat's webapps directory as ROOT.war:
   ```sh
   sudo cp target/storemapper.war $CATALINA_HOME/webapps/ROOT.war
   ```
   Note: This deploys the application at the root context (/)

### Manage Tomcat
1. Restart Tomcat:
   ```sh
   sudo systemctl restart tomcat10
   ```
2. View Tomcat logs:
   ```sh
   tail -f $CATALINA_HOME/logs/catalina.out
   ```

### Tomcat Management Commands
- Start Tomcat: `$CATALINA_HOME/bin/startup.sh`
- Stop Tomcat: `$CATALINA_HOME/bin/shutdown.sh`

### Deployment to DigitalOcean Droplet
1. SCP the WAR file to the Droplet:
   ```sh
   scp target/storemapper.war root@64.23.180.16:/var/lib/tomcat10/webapps/ROOT.war
   ```
2. SSH into the Droplet and restart Tomcat:
   ```sh
   ssh root@64.23.180.16
   sudo systemctl restart tomcat10
   ```
3. View Tomcat logs on the Droplet:
   ```sh
   tail -f /var/lib/tomcat10/logs/catalina.2024-07-25.log
   ```

### Troubleshooting
1. Check Tomcat status:
   ```sh
   sudo systemctl status tomcat10
   ```
2. Verify deployed applications in Tomcat Manager:
   - Access http://localhost:8080/manager/html (requires configuration)
3. Ensure no conflicting applications in webapps directory:
   ```sh
   ls -l $CATALINA_HOME/webapps
   ```
   Remove any conflicting WAR files or directories if necessary.

### Notes
- Deploying as ROOT.war ensures the application is accessible at the root context (/)
- Always check Tomcat logs for deployment issues or application errors
- Ensure proper permissions for Tomcat to access the WAR file and its unpacked directory

### Additional Considerations

#### Security
- Ensure the Tomcat manager interface is secured or disabled in production environments.
- Use HTTPS for all production traffic.

#### Backup and Rollback
- Before deploying a new version, create a backup of the current WAR file:
  ```sh
  sudo cp $CATALINA_HOME/webapps/ROOT.war $CATALINA_HOME/webapps/ROOT.war.backup
  ```
- To rollback, stop Tomcat, replace the WAR file with the backup, and restart:
  ```sh
  sudo systemctl stop tomcat10
  sudo mv $CATALINA_HOME/webapps/ROOT.war.backup $CATALINA_HOME/webapps/ROOT.war
  sudo systemctl start tomcat10
  ```

#### Firewall Configuration
- Ensure your firewall allows traffic on port 8080 (or 80/443 for production):
  ```sh
  sudo ufw allow 8080/tcp
  ```

## Environment Setup

### Local Development Environment
- Set CATALINA_HOME to point to the active Tomcat installation
- Set JAVA_HOME to point to JVM 21
- Add JAVA_HOME/bin to PATH
- Example shell configuration:
  ```sh
  export CATALINA_HOME=/opt/tomcat10
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  export PATH=$JAVA_HOME/bin:$PATH
  ```

### DigitalOcean Droplet (Production Environment)
- Environment variables like CATALINA_HOME and JAVA_HOME are typically not set in the shell
- Tomcat is managed as a system service
- Java and Tomcat paths are defined in the service configuration
- To check Java location: `which java`
- To check Tomcat configuration:
  ```sh
  sudo systemctl cat tomcat10.service
  ```
- Tomcat logs are typically found at: `/var/lib/tomcat10/logs/`

### Note on Environment Differences
The local development environment uses shell environment variables for flexibility, while the production environment on DigitalOcean relies on system service configurations for stability and security. When troubleshooting deployment issues, be aware of these differences in how Java and Tomcat are managed between environments.