FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app
COPY . .
RUN ./gradlew :backend:bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
