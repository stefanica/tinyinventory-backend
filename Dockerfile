FROM openjdk:24-slim-bullseye
ADD target/inventory-backend.jar inventory-backend.jar
ENTRYPOINT ["java", "-jar", "/inventory-backend.jar"]