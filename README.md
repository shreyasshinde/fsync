fsync
=====

'fsync' is a Java application that synchronizes folders across machines over HTTP. It is designed to run as a daemon or background service that keeps track of files in a registered folder and updates any changes to the folder to its peers.

The application is built using the File Watcher service in Java 7 to observe changes in the folders and the replication works over HTTP using an embedded Jetty web server.

Building the source
-------------------
1. Checkout the source.
	- git clone https://github.com/shreyasshinde/fsync.git fsync
	- cd fsync
2. Run maven
	- mvn clean install


Running the application
-----------------------
java -classpath "<all the JAR files>" com.fync.App

Last updated: 2/12/2014
