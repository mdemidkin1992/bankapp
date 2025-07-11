FROM amazoncorretto:21-alpine
WORKDIR /app
COPY service-transfer/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]