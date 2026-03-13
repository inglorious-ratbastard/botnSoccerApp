# Use official Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the project
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the Spring Boot app
CMD ["java", "-jar", "target/botnSoccerApp-0.0.1-SNAPSHOT.jar"]
