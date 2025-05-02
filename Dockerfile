# First stage: Build the application
FROM maven:3.9.6-eclipse-temurin-17 as builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Second stage: Run the application
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/NotificationService-0.0.1-SNAPSHOT.jar NotificationService.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "NotificationService.jar"]
