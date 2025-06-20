# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean install -DskipTests 

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN mkdir -p /app/logs

COPY --from=build /app/NotificationService/target/NotificationService-0.0.1-SNAPSHOT.jar NotificationService.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "NotificationService.jar"]
