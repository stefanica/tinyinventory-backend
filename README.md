# TinyInventory Java + Spring Boot Backend App

* 70% done -> percentage of the backend project completed
* 30% available live -> percentage available for live view on https://tinyinventory.com

The whole project uses Docker for containerization and a VPS for deployment.  
The Docker images are updated when certain steps are done/ready, that's why there is a difference in the two percentages above.

This is one of a 3-parts project: Backend, Frontend, and an Android App

## Technologies used:
* Java – object-oriented programming language used for backend development
* Spring Boot – Java-based framework for building robust, scalable web applications and REST APIs
* Spring Web – Spring Boot module for creating RESTful APIs and web applications
* Spring Security – handles authentication and authorization
* JWT (JSON Web Token) – used for stateless authentication and securing RESTful API endpoints
* Spring Data JPA – abstraction layer for database operations using Java Persistence API (JPA)
* Hibernate – ORM (Object-Relational Mapping) tool, used under the hood by Spring Data JPA
* PostgreSQL – open-source relational database management system used for storing and querying data
* Maven – build automation tools used to manage dependencies and project lifecycle
* Docker – for containerizing the backend application and database
* Docker Compose – for orchestrating multi-container setups like app + PostgreSQL
