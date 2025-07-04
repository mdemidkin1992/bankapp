# === Сборка jar ===
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :config-server:build --no-daemon -x test

# === Сборка docker образа ===
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/config-server/build/libs/*.jar app.jar
COPY --from=build /app/config-server/src/main/resources/config-repo /app/config-repo
ENTRYPOINT ["java", "-jar", "/app/app.jar"]