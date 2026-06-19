FROM eclipse-temurin:21-jdk
LABEL authors="ropold"
EXPOSE 8080
COPY backend/target/ecinventoryhub.jar ecinventoryhub.jar
ENTRYPOINT ["java", "-jar", "ecinventoryhub.jar"]