# Start with Java 17 JDK
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 1️⃣ Copy project files from correct subfolder
COPY botnSoccer/pom.xml ./pom.xml
COPY botnSoccer/src ./src

# 2️⃣ Build the project
# Try the Maven wrapper if present, or fallback to system mvn
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# 3️⃣ Expose default Spring Boot port
EXPOSE 8080

# 4️⃣ Run the generated jar (auto‑detect name)
CMD ["sh", "-c", "java -jar target/*.jar"]
