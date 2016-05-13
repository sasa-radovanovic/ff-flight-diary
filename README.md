# ff-flight-diary (Frequent Flyers Flight Diary)
## Personal Flight Diary to store your flight history and compare worldwide data

[Wireframes/pages - how it looks like when deployed](http://sasa-radovanovic.github.io/)

First of all - **this is not commercial application**, but it's a good starting point. This web service allows users to register and store their flights. User can review his/her personal statistics based on various parameters. Public part of the service provides statistics based on entire system.

### Motivation

This application connects two of my big passions - aviation and coding. You might say that there is commercial solution out there [Flight Diary](http://flightdiary.net/), and you would be right. Those guys did hell of a job, and this project is not flightdiary.net competitor - it is just my personal homage to it, implemented using my view what frequent flyers would like to have in that kind of service.

### Software components

This small project was my "getting-to-know-each-other-better" with Vert.x toolkit/framework [Vert.x](http://vertx.io/). There are some stuff that i would rewrite, but since this was written in just 3 weeks using 2-3 hours/day, i am pretty pleased with the way it turned out.

* Programming language - Java 8
* Middleware framework - Vert.x 3.2.1, with [Vert.x modules](http://vertx.io/docs/)  
  * Vert.x core 
  * Vert.x web
  * Vert.x JDBC client
  * Vert.x PostgreSQL client
  * Vert.x Mail Client
  * Vert.x JDBC auth
  * Vert.x-Unit
* Database - PostgreSQL 9.3/9.5 (Note that you can easily change SQL database underneath - but make sure to include database adapter in pom.xml)
* Ehcache
* Frontend
  * AngularJS (Note that there are two separate Angular applications, more on use case link)
  * Bootstrap
  * Google Maps API (Make sure to use your API key if you plan to deploy this for other than demo/testing purposes)

### Installation

In order to run instance, download code. Download Java 8 (JDK or JRE, depending what do you plan to do with the code) if you do not have one on your machine. Download and set PostgreSQL (or other SQL database in which case there should be some adjustments), and create database for the application (i.e. flight_vertx). Download and setup maven. Once you downloaded the project, there will be 3 .csv files there. You can put them anywhere on your file system, provided you replace location parameters in FlightDiaryApplication.java and set the to your one (i.e. i kept mine on desktop, therefore airports.csv is loaded using following code).

```
String airportsLocation = "C:\\Users\\rsasa\\Desktop\\airports.csv"; 
String airlinesLocation = "C:\\Users\\rsasa\\Desktop\\airlines.csv";
String airplaneTypesLocation = "C:\\Users\\rsasa\\Desktop\\airplanes.csv";
```

Don't forget other options from FlightDiaryApplication.java. Even if you will not change anything, review it - description explains usage of every parameter.
```
// Use cleanDatabase and fillWithFixtures together - both true or false
// If set to true, database (flights and users tables) will be cleaned on start-up 
boolean cleanDatabase = false;
		
// If set to true, database (flights and users tables) will be filled with fixtures
boolean fillWithFixtures = false;
		
// Database settings. If you want to run it against other database, be sure to change pom.xml
String databaseJDBCUrl = "jdbc:postgresql://localhost:5432/flight_vertx";
String databaseUsername = "postgres";
String databasePassword = "insight";
String databaseDriver = "org.postgresql.Driver";
		
// CSV files from which database gets filled. Theses files arrived with the application
String airportsLocation = "C:\\Users\\rsasa\\Desktop\\airports.csv"; 
String airlinesLocation = "C:\\Users\\rsasa\\Desktop\\airlines.csv";
String airplaneTypesLocation = "C:\\Users\\rsasa\\Desktop\\airplanes.csv";

// Mail settings. You can leave this for testing. I opened this mail in that purpose - it's your's free to use.
String email = "flight.diary@yandex.com";
String smtpLoc = "smtp.yandex.com";
int smtpPort = 587;
String emailUsername = "flight.diary@yandex.com";
String emailPassword = "frequentflyer";
		
// Deployment port. If set to 8080, your application will be available at http://localhost:8080
int deploymentPort = 8080;
// Set your deployment address - this is used when sending link inside the mail message
Constants.DEPLOYMENT_URL = "http://localhost:8080";
```
Deployment:

- Deploying from Eclipse

Import project into Eclipse IDE. Run maven update. Set all necessary options in FlightDiaryApplication.java and simply click on "Run as > Java Application" on that exact file. In console there should stack about application start-up procedure. Be sure to set *cleanDatabase* and *fillWithFixtures* flags to true.


- Deploying from file system

Make sure you have set JAVA_HOME environment variable. Build application using:

```
mvn clean compile -e
```
And package it with:
```
mvn clean package -e
```

Navigate your terminal (or command prompt) to PROJECT_PATH\target and run flight-diary fat jar with:
```
java -jar flight-diary-far.jar
```
Of course - the name of the application is bigger as it contains version, but you get the idea.

And... That's it. If everything went as planned - you should navigate your browser to http://localhost:8080 and the app is there.
Note that during boot there will be heavy logs in console/terminal since application initializes all system components and loads data from .csv files into database.

### Design patterns

I did try to comply to Clean Code principles as much as i did, although i am aware that code deviates in some places from it. There should be one serious code revision after which i could say that it's been done properly. Nonetheless, i tried using as much as GoF Design patterns as i could.

Some of them are:

Creational patterns | Structural patterns | Behavior patterns
------------ | ----------------- | -----------------
Factory Pattern | Decorator Pattern  | Front Controller Pattern
Abstract Factory Pattern | Facade Pattern | Data Access Object Pattern
Singleton Pattern | MVC Pattern | Template Pattern
Builder Pattern |  | 
Prototype Pattern |  | 

### Use cases and printscreens

Application-go-through is available at the following link. You can find all screens available, use cases, and detailed description.

### Tests

Tests are written using Vertx Unit test module - [Vertx Unit Test module!](http://vertx.io/docs/vertx-unit/java/), and are not fully developed. They only cover some basic and the most elementary API calls. 

### Contributors

I am the sole contributor to this small project and for all other information you can contact me by mail:
- sasa1kg@yahoo.com
- sasa.radovanovic@live.com

Don't forget - Eat, sleep, fly, repeat! :)

### License

This code is available through MIT licence. 

