FROM gradle:8-jdk17 AS build
WORKDIR /src
COPY . .
RUN gradle clean build -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/build/libs/*.jar app.jar
CMD ["java", "-Dspring.profiles.active=local", "-jar", "app.jar"]