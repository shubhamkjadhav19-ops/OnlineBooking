FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY  ./
RUN mvn -B -DskipTests package

