# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user with a specific UID for security best practices
RUN adduser -D -u 1000 user
USER user

# Set the port the server will run on, required by Hugging Face Spaces
ENV SERVER_PORT=7860

# Copy the built JAR file from the build stage with correct ownership
COPY --from=build --chown=user /app/target/*.jar app.jar

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
