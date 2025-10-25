# ===========================================
# STAGE 1: Build Stage
# ===========================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Copy pom.xml và download dependencies trước (để cache Docker layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code
COPY src ./src

# Build application (skip tests để build nhanh hơn)
RUN mvn clean package -DskipTests -B

# ===========================================
# STAGE 2: Runtime Stage
# ===========================================
FROM eclipse-temurin:17-jre-alpine

# Install wget để dùng cho health check
RUN apk add --no-cache wget

WORKDIR /app

# Tạo user non-root để chạy application (bảo mật)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy file JAR từ build stage
COPY --from=build /build/target/*.jar app.jar

# Đổi owner của file JAR về user spring
RUN chown -R spring:spring /app

# Chuyển sang user spring (không dùng root)
USER spring:spring

# Expose port 8080
EXPOSE 8080

# Health check - kiểm tra app có sống không mỗi 30s
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options tối ưu cho container
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Chạy ứng dụng với profile từ environment variable (mặc định là docker)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILE:-docker} -jar app.jar"]