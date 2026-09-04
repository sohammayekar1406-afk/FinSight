# Multi-stage Docker build for FinSight Platform
FROM maven:3.9.16-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml ./
COPY next-app next-app
COPY src src

RUN mvn -B clean package -DskipTests

# Minimal production runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root unprivileged security execution
RUN addgroup -S finsight && adduser -S finsight -G finsight
USER finsight:finsight

COPY --from=builder /app/target/finsight-*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
