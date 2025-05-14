# ---------- Stage 1: Build the application ----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run the application ----------
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/NotificationService-0.0.1-SNAPSHOT.jar NotificationService.jar

# Copy log directory if needed
RUN mkdir -p logs

# Expose correct port (8085 for NotificationService)
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "NotificationService.jar"]
