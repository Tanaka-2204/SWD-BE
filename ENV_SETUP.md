# 🔧 Environment Setup Guide

## 📋 Tổng Quan

Dự án này sử dụng **biến môi trường** để quản lý cấu hình nhạy cảm (credentials, database passwords, v.v.)

## 🗂️ Cấu Trúc Files

```
.
├── .env                    # ⚠️ File thực (không commit, chứa credentials thật)
├── .env.example           # ✅ Template mẫu (commit được)
├── docker-compose.yml     # Sử dụng biến từ .env
├── application.properties          # Config mặc định
├── application-docker.properties   # Config cho Docker
└── application-prod.properties     # Config cho Production
```

## 🚀 Cách Setup

### **Bước 1: Copy template**
```bash
cp .env.example .env
```

### **Bước 2: Điền thông tin thực vào `.env`**

Mở file `.env` và thay thế các giá trị:

```env
# Database
POSTGRES_PASSWORD=YOUR_SECURE_PASSWORD_HERE  # ← Đổi thành password thật

# AWS S3
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID     # ← Lấy từ AWS Console
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_KEY    # ← Lấy từ AWS Console
AWS_S3_BUCKET=your-bucket-name               # ← Tên S3 bucket

# AWS Cognito
JWT_JWK_SET_URI=https://cognito-idp...       # ← URL từ Cognito User Pool
AWS_COGNITO_USER_POOL_ID=ap-southeast-2_xxx  # ← User Pool ID
AWS_COGNITO_CLIENT_ID=xxxxxxxxxxxxx          # ← App Client ID
AWS_COGNITO_CLIENT_SECRET=xxxxxxxxx          # ← App Client Secret
```

### **Bước 3: Chạy Docker Compose**
```bash
docker-compose up -d
```

## 🔒 Bảo Mật

### ✅ **Được commit:**
- `.env.example` (template)
- `application-docker.properties` (config không có secret)
- `docker-compose.yml`

### ❌ **KHÔNG được commit:**
- `.env` (đã có trong `.gitignore`)
- Bất kỳ file chứa credentials thật

## 📝 Chi Tiết Các Biến

| Biến | Mô Tả | Ví Dụ |
|------|-------|-------|
| `POSTGRES_DB` | Tên database | `loyaltysystem_kzfr` |
| `POSTGRES_USER` | Username PostgreSQL | `loyaltysystem_kzfr_user` |
| `POSTGRES_PASSWORD` | Password PostgreSQL | `LDPaZKe...` |
| `AWS_ACCESS_KEY_ID` | AWS Access Key | `AKIA...` |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key | `wJalr...` |
| `AWS_REGION` | AWS Region | `ap-southeast-1` |
| `AWS_S3_BUCKET` | S3 Bucket name | `loyalty-system-bucket` |
| `JWT_JWK_SET_URI` | Cognito JWKS URL | `https://cognito-idp...` |
| `AWS_COGNITO_USER_POOL_ID` | Cognito User Pool ID | `ap-southeast-2_xxx` |
| `SPRING_PROFILE` | Spring active profile | `docker` |
| `JAVA_OPTS` | JVM options | `-Xms512m -Xmx1024m` |

## 🐛 Troubleshooting

### Lỗi: "environment variable not set"
→ Kiểm tra xem file `.env` có tồn tại không

### Lỗi: "connection refused" khi connect DB
→ Đảm bảo `POSTGRES_PASSWORD` trong `.env` khớp với config

### Lỗi: "Invalid JWT"
→ Kiểm tra `JWT_JWK_SET_URI` có đúng không

## 📚 Tài Liệu Liên Quan

- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [AWS Cognito Setup](https://docs.aws.amazon.com/cognito/)
