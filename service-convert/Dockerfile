FROM amazoncorretto:21-alpine
WORKDIR /app
COPY service-convert/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]