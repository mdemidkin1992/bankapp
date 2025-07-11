FROM amazoncorretto:21-alpine
WORKDIR /app
COPY config-server/build/libs/*.jar app.jar
COPY config-server/src/main/resources/config-repo /app/config-repo
ENTRYPOINT ["java", "-jar", "/app/app.jar"]