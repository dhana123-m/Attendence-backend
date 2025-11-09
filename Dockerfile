# ===============================================================
# 🧱 STAGE 1: Build the JAR using Maven
# ===============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven configuration and download dependencies first (cache optimization)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
COPY lib ./lib
RUN mvn clean package -DskipTests

# ===============================================================
# 🚀 STAGE 2: Run the JAR in a lightweight JRE environment
# ===============================================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# ✅ Install native OpenCV libraries (needed for org.openpnp.opencv)
RUN apt-get update && apt-get install -y libopencv-dev && apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy compiled JAR and dependencies
COPY --from=build /app/target/backend.jar /app/backend.jar
COPY --from=build /app/lib /app/lib

# Copy models, known_faces, and fallback test image for Render
COPY models /app/models
COPY known_faces /app/known_faces
COPY input.jpg /app/input.jpg

# Expose your application port
EXPOSE 8182

# Run the application
ENTRYPOINT ["java", "-jar", "backend.jar"]
