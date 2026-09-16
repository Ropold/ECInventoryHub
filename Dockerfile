# Das Jar wird in der Pipeline gebaut (Job "build-backend", inkl. Frontend unter
# src/main/resources/static) und vom Job "push-to-docker-hub" nach backend/target geladen.
# Deshalb hier kein eigener Maven-Build: der wuerde das Frontend verlieren.
FROM eclipse-temurin:21-jre
LABEL authors="ropold"
EXPOSE 9876
COPY backend/target/ecinventoryhub.jar ecinventoryhub.jar
ENTRYPOINT ["java", "-jar", "ecinventoryhub.jar"]
