FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
ENV MAVEN_OPTS="-Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2"
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests -Dmaven.wagon.http.retryHandler.count=10 package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:InitialRAMPercentage=20 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8081

COPY --from=build /workspace/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
