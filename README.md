This is a Spring Boot-based implementation of the Grove API. 

To run this locally, either run the Application class in your IDE, or run the following commands:

    gradle bootRun
    java -jar build/libs/grove-spring-boot-0.1.0.jar

To try this out in a Grove application, create a Grove project like you normally would, but before running "npm start", 
find the following line in your ui/package.json file:

    "proxy": "http://localhost:9003"

And change it to:

    "proxy": "http://localhost:8080"

That's telling the React app to talk to Spring Boot instead of the Node middle tier (which listens on port 9003).

The Spring Boot application - including connection information to MarkLogic - can be configured in 
src/main/resources/application.properties. 

Full Spring Boot docs are at https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/ . 
 
