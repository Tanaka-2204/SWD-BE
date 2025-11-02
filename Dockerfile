# ==============================================================================
# STAGE 1: Build Stage (Maven & JDK 17)
# Sử dụng maven:3.9.9-eclipse-temurin-17-alpine (nhẹ và nhanh)
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

# Đặt thư mục làm việc
WORKDIR /build

# Copy pom.xml và tải dependencies (tận dụng Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B -Dassembly.skip=true

# ==============================================================================
# STAGE 2: Runtime Stage (JRE 17 Alpine) - Tối ưu và Bảo mật
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

# CÀI ĐẶT WGET CHO HEALTH CHECK
# Cần thiết nếu bạn muốn giữ Health Check (và endpoint /actuator/health của bạn là public)
RUN apk add --no-cache wget

# Đặt thư mục làm việc
WORKDIR /app

# Tạo user non-root để chạy application (bảo mật)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy file JAR từ build stage
# LƯU Ý: Đảm bảo tên file JAR khớp với tên file JAR mà Maven tạo ra trong thư mục target/
COPY --from=build /build/target/*.jar app.jar

# Đổi owner của file JAR về user spring
RUN chown -R spring:spring /app

# Chuyển sang user spring (không dùng root)
USER spring:spring

# Expose port (Dù Render dùng biến môi trường PORT, việc này vẫn tốt cho container)
EXPOSE 8080

# Health check - Giữ nguyên logic của bạn
# LƯU Ý: Chỉ giữ lại nếu /actuator/health KHÔNG yêu cầu xác thực.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options tối ưu cho container
# Sử dụng biến môi trường $PORT của Render để cấu hình cổng Spring Boot
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Lệnh chạy ứng dụng. 
# Spring Boot sẽ tự động lắng nghe trên cổng được set bởi biến môi trường 'SERVER_PORT'
# Chúng ta sẽ set SERVER_PORT = $PORT của Render.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker} -jar app.jar"]
