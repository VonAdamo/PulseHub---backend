FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY auth-service/pom.xml auth-service/pom.xml
COPY bff-service/pom.xml bff-service/pom.xml
COPY bot-service/pom.xml bot-service/pom.xml
COPY message-service/pom.xml message-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml

RUN chmod +x mvnw && ./mvnw -B -pl user-service -am dependency:go-offline

COPY user-service/src user-service/src

RUN ./mvnw -B -pl user-service -am package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=build /workspace/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
