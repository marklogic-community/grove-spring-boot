[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Run a MarkLogic Grove application with a Spring Boot middle tier
=========

This project produces a package containing a Spring Boot middle tier suitable for use in a 
[MarkLogic Grove](https://marklogic-community.github.io/grove/) application.

Note that this middle tier is intended as a starting point for an application. As with any Spring Boot application, 
it's expected that you'll customize various or all parts of this starting point, such as how users are authenticated or
how documents are persisted. The benefit of grove-spring-boot is that provides an initial working application so that 
you can immediately focus on customizing the application to your own needs.

## How to use grove-spring-boot

Currently, you must clone this repository and build the grove-spring-boot package yourself. Soon, this will be replaced
with versioned releases in github.

To build the package (a zip file), run:

    ./gradlew buildPackage

This produces the following file:

    build/distributions/grove-spring-boot-(version).zip

To use this file in a Grove project that you've already created, simply unzip it in the root of the Grove project. 
That will create a "spring-boot" directory with a README.md file that you can read to understand how to use the 
Spring Boot middle tier. 
