# ---------------------------
# 1️⃣ Build Stage (Maven + Corretto 21)
# ---------------------------
FROM maven:3.9.9-amazoncorretto-21 AS builder

WORKDIR /app

# Copy pom first for caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# ---------------------------
# 2️⃣ Runtime Stage (Lightweight)
# ---------------------------
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy built jar
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Optimized JVM for container
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
