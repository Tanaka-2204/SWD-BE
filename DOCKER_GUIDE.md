# ============================================================
# HƯỚNG DẪN SỬ DỤNG DOCKER CHO LOYALTY SYSTEM
# ============================================================

## 📦 CẤU TRÚC DỰ ÁN

```
d:\WorkSpace\SWD\SWD-BE\
├── src/
│   └── main/
│       └── resources/
│           ├── application.properties           # Config mặc định (localhost)
│           └── application-docker.properties    # Config cho Docker
├── Dockerfile                                   # Định nghĩa Docker image
├── docker-compose.yml                           # Orchestration 2 containers
└── .dockerignore                                # File bỏ qua khi build
```

## 🚀 CÁCH SỬ DỤNG

### ✅ BƯỚC 1: Khởi động tất cả services

```bash
# Giải thích: 
# - docker-compose up: Khởi động các services được định nghĩa trong docker-compose.yml
# - -d: Chạy ở chế độ detached (chạy nền)
# - --build: Build lại Docker image trước khi chạy

docker-compose up -d --build
```

**Điều gì đang xảy ra:**
1. Docker sẽ tải PostgreSQL image (nếu chưa có)
2. Build Spring Boot application thành Docker image
3. Tạo network `loyalty-network` để các container giao tiếp
4. Khởi động container `loyalty-postgres` (PostgreSQL)
5. Đợi PostgreSQL sẵn sàng (health check)
6. Khởi động container `loyalty-backend` (Spring Boot App)

---

### ✅ BƯỚC 2: Xem logs của ứng dụng

```bash
# Giải thích:
# - docker-compose logs: Xem logs của containers
# - -f: Follow mode (xem real-time)
# - loyalty-backend: Tên container cần xem logs

docker-compose logs -f loyalty-backend
```

**Bạn sẽ thấy:**
- Spring Boot đang khởi động
- Kết nối đến PostgreSQL
- Hibernate tạo/update tables
- Message "Started DemoApplication in X seconds"

---

### ✅ BƯỚC 3: Kiểm tra health của ứng dụng

```bash
# Giải thích:
# Gọi API health check endpoint để xem app có hoạt động không

curl http://localhost:8080/actuator/health
```

**Response mong đợi:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

---

### ✅ BƯỚC 4: Test API

```bash
# Giải thích:
# Test endpoint API students

curl http://localhost:8080/api/students
```

---

### ✅ BƯỚC 5: Xem trạng thái các containers

```bash
# Giải thích:
# - docker-compose ps: Liệt kê tất cả containers
# Hiển thị: tên container, command, state, ports

docker-compose ps
```

**Output mẫu:**
```
NAME                IMAGE                    STATUS         PORTS
loyalty-backend     swd-be-loyalty-backend   Up 2 minutes   0.0.0.0:8080->8080/tcp
loyalty-postgres    postgres:17-alpine       Up 2 minutes   0.0.0.0:5432->5432/tcp
```

---

### ✅ BƯỚC 6: Kết nối vào PostgreSQL (Tùy chọn)

```bash
# Giải thích:
# - docker exec: Chạy lệnh trong container đang chạy
# - -it: Interactive terminal
# - loyalty-postgres: Tên container
# - psql: PostgreSQL client
# - -U: Username
# - -d: Database name

docker exec -it loyalty-postgres psql -U loyaltysystem_kzfr_user -d loyaltysystem_kzfr
```

**Trong PostgreSQL shell:**
```sql
-- Xem tất cả tables
\dt

-- Xem dữ liệu trong table student
SELECT * FROM student;

-- Thoát
\q
```

---

### ✅ BƯỚC 7: Dừng tất cả containers

```bash
# Giải thích:
# - docker-compose down: Dừng và xóa containers
# Lưu ý: Data trong volume vẫn được giữ lại

docker-compose down
```

---

### ✅ BƯỚC 8: Dừng và XÓA DATA (Cẩn thận!)

```bash
# Giải thích:
# - -v: Xóa cả volumes (xóa data trong database)
# Cảnh báo: Tất cả dữ liệu trong PostgreSQL sẽ bị mất!

docker-compose down -v
```

---

## 🔧 CÁC LỆNH HỮU ÍCH KHÁC

### 📊 Xem logs của PostgreSQL

```bash
# Giải thích:
# Xem logs của database để debug connection issues

docker-compose logs -f loyalty-postgres
```

---

### 🔄 Restart một container cụ thể

```bash
# Giải thích:
# Chỉ restart container backend, không ảnh hưởng đến database

docker-compose restart loyalty-backend
```

---

### 🛠️ Rebuild khi có thay đổi code

```bash
# Giải thích:
# Khi bạn sửa code Java, cần rebuild image

docker-compose up -d --build loyalty-backend
```

---

### 🔍 Vào shell của container

```bash
# Giải thích:
# Truy cập terminal bên trong container để debug

docker exec -it loyalty-backend sh
```

**Trong shell:**
```bash
# Xem file JAR
ls -la

# Xem biến môi trường
env | grep SPRING

# Thoát
exit
```

---

### 📈 Xem resource usage

```bash
# Giải thích:
# Xem CPU, RAM, Network usage của containers

docker stats
```

---

### 🗑️ Xóa tất cả unused images

```bash
# Giải thích:
# Cleanup các Docker images không dùng nữa để tiết kiệm disk

docker image prune -a
```

---

### 💾 Backup database

```bash
# Giải thích:
# Export toàn bộ database ra file SQL

docker exec -it loyalty-postgres pg_dump -U loyaltysystem_kzfr_user loyaltysystem_kzfr > backup.sql
```

---

### 📥 Restore database

```bash
# Giải thích:
# Import dữ liệu từ file SQL vào database

docker exec -i loyalty-postgres psql -U loyaltysystem_kzfr_user -d loyaltysystem_kzfr < backup.sql
```

---

## 🐛 TROUBLESHOOTING

### ❌ Lỗi: Port 8080 already in use

**Nguyên nhân:** Có ứng dụng khác đang dùng port 8080

**Giải pháp:**

```bash
# Windows: Tìm và kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Hoặc thay đổi port trong docker-compose.yml
# Sửa "8080:8080" thành "8081:8080"
```

---

### ❌ Lỗi: Cannot connect to database

**Nguyên nhân:** PostgreSQL chưa sẵn sàng hoặc config sai

**Giải pháp:**

```bash
# Kiểm tra PostgreSQL có chạy không
docker-compose ps loyalty-postgres

# Xem logs của PostgreSQL
docker-compose logs loyalty-postgres

# Kiểm tra connection
docker exec -it loyalty-postgres pg_isready
```

---

### ❌ Lỗi: Container keeps restarting

**Nguyên nhân:** App bị crash khi khởi động

**Giải pháp:**

```bash
# Xem logs chi tiết
docker-compose logs loyalty-backend

# Kiểm tra health check
docker inspect loyalty-backend | grep Health -A 10
```

---

### ❌ Lỗi: Build failed

**Nguyên nhân:** Lỗi compile hoặc dependency issue

**Giải pháp:**

```bash
# Build lại với no cache
docker-compose build --no-cache

# Hoặc build thủ công để xem lỗi chi tiết
docker build -t loyalty-backend .
```

---

## 📝 WORKFLOW THỰC TẾ

### Khi bắt đầu làm việc (Buổi sáng)

```bash
# Khởi động tất cả
docker-compose up -d

# Xem logs để chắc chắn app đã chạy
docker-compose logs -f loyalty-backend

# Test API
curl http://localhost:8080/actuator/health
```

---

### Khi sửa code

```bash
# 1. Sửa code trong IDE
# 2. Rebuild và restart container
docker-compose up -d --build loyalty-backend

# 3. Xem logs
docker-compose logs -f loyalty-backend
```

---

### Khi kết thúc ngày làm việc

```bash
# Dừng tất cả (giữ lại data)
docker-compose down

# Hoặc để chạy nền (không cần dừng)
# Containers sẽ tự động restart khi máy khởi động lại
```

---

## 🎯 TÓM TẮT CÁC LỆNH QUAN TRỌNG

```bash
# Khởi động
docker-compose up -d --build

# Xem logs
docker-compose logs -f loyalty-backend

# Kiểm tra trạng thái
docker-compose ps

# Dừng
docker-compose down

# Xóa cả data
docker-compose down -v

# Rebuild khi sửa code
docker-compose up -d --build loyalty-backend

# Vào PostgreSQL
docker exec -it loyalty-postgres psql -U loyaltysystem_kzfr_user -d loyaltysystem_kzfr
```

---

## 📌 LƯU Ý QUAN TRỌNG

1. **Data persistence:** Data trong PostgreSQL được lưu trong volume `loyalty_postgres_data`, sẽ không mất khi restart container

2. **Network:** Hai containers giao tiếp với nhau thông qua network `loyalty-network`

3. **Health check:** Docker tự động kiểm tra health của containers và restart nếu cần

4. **Profile:** App chạy với profile `docker`, đọc config từ `application-docker.properties`

5. **Port mapping:** 
   - PostgreSQL: localhost:5432 → container:5432
   - Spring Boot: localhost:8080 → container:8080

---

**Chúc bạn làm việc hiệu quả với Docker! 🚀**
