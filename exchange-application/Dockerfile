
FROM eclipse-temurin:21-jdk-alpine

COPY target/exchange-application-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
