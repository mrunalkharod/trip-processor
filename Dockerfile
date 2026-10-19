# ---- Build ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src src
RUN mvn -q -B clean package -DskipTests

# ---- Run  ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/target/little-pay-assessment.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
