# Multi-stage Docker build for FinSight Platform
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper & dependencies pom
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy frontend source and Java source
COPY next-app next-app
COPY src src
RUN ./mvnw clean package -DskipTests

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
