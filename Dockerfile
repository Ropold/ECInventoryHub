FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
LABEL authors="ropold"
EXPOSE 8080
COPY --from=build /app/target/ecinventoryhub.jar ecinventoryhub.jar
ENTRYPOINT ["java", "-jar", "ecinventoryhub.jar"]
