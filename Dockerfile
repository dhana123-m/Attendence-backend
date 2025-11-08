# Use an official Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# ------------------------------------------
# Use a lightweight JRE image for running
# ------------------------------------------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/backend.jar /app/backend.jar

# Expose port (change if your app uses a different one)
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "backend.jar"]
