FROM gradle:7.6.4-jdk8 AS build
WORKDIR /workspace
COPY . .
RUN gradle :services:dashboard-service:bootJar --no-daemon

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /workspace/services/dashboard-service/build/libs/*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
