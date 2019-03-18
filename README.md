The intent of this project is to produce a package containing a Spring Boot middle tier suitable for use in a 
[MarkLogic Grove](https://marklogic-community.github.io/grove/) application.

To build the package (a zip file), run:

    ./gradlew buildPackage

This produces the following file:

    build/distributions/grove-spring-boot-(version).zip

To use this file in a Grove project that you've already created, simply unzip it in the root of the Grove project. 
That will create a "spring-boot" directory with a README.md file that you can read to understand how to use the 
Spring Boot middle tier. 
