# Use official Maven + JDK image
FROM maven:3.9.3-eclipse-temurin-17-alpine

# Set working directory
WORKDIR /app

# Copy your project files
COPY botnSoccer/pom.xml ./pom.xml
COPY botnSoccer/src ./src

# Build the Spring Boot project (skip tests for speed)
RUN mvn clean package -DskipTests

# Expose the port
EXPOSE 8080

# Run the Spring Boot jar (auto-detect)
CMD ["sh", "-c", "java -jar target/*.jar"]
