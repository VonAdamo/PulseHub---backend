FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY auth-service/pom.xml auth-service/pom.xml
COPY bff-service/pom.xml bff-service/pom.xml
COPY bot-service/pom.xml bot-service/pom.xml
COPY message-service/pom.xml message-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY proto proto

RUN chmod +x mvnw && ./mvnw -B -pl message-service -am dependency:go-offline

COPY message-service/src message-service/src

RUN ./mvnw -B -pl message-service -am package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=build /workspace/message-service/target/message-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
