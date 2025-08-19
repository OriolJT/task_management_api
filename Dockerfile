# Dockerfile

# Build stage
FROM eclipse-temurin:21 as build
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Run stage
FROM eclipse-temurin:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
