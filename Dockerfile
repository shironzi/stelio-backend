# Build stage
FROM maven:3.8.5-eclipse-temurin-8-alpine AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:8-jre-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx384m", "-jar", "/app.jar"]