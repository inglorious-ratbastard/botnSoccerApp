# Dockerfile for botnSoccerApp (Spring Boot)

# 1️⃣ Base image: Java 17 JDK
FROM eclipse-temurin:17-jdk-alpine

# 2️⃣ Set working directory
WORKDIR /app

# 3️⃣ Copy the Maven project
# Adjust the folder name if your Maven module is inside a subfolder
COPY botnSoccerApp/pom.xml ./pom.xml
COPY botnSoccerApp/src ./src

# 4️⃣ Build the project
# Try Maven wrapper first, fallback to system Maven
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# 5️⃣ Expose the dynamic port
EXPOSE 8080

# 6️⃣ Run the Spring Boot jar
# Auto-detect the jar inside target/
CMD java -jar $(ls target/*.jar | head -n 1)
