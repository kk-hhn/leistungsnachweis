# Use Gradle to build the Spring Boot app
FROM gradle:8.10.2-jdk21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the entire code
COPY . .

RUN gradle clean shadowJar --no-daemon

# Use OpenJDK to run the app
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app
# Copy the previously generated jar file
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the main class from the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]