# 🔐 SECURITY & ENVIRONMENT SETUP

## 📁 Files Created

### 1. `.env` (⚠️ KHÔNG COMMIT)
- Chứa credentials thật cho local development
- File này đã được add vào `.gitignore`

### 2. `.env.example` (✅ Commit được)
- Template cho `.env`
- Không chứa credentials thật
- Team members copy file này thành `.env` và điền values

### 3. `application.properties.template` (✅ Commit được)
- Template cho `application.properties`
- Sử dụng environment variables: `${AWS_ACCESS_KEY_ID}`

---

## 🚀 CÁCH SỬ DỤNG

### **Local Development:**

1. **Copy `.env.example` thành `.env`:**
   ```bash
   cp .env.example .env
   ```

2. **Điền credentials thật vào `.env`:**
   ```env
   AWS_ACCESS_KEY_ID=AKIA_YOUR_REAL_KEY
   AWS_SECRET_ACCESS_KEY=YOUR_REAL_SECRET
   ```

3. **Chạy app với environment variables:**
   ```bash
   # IntelliJ: Run → Edit Configurations → Environment Variables
   # Hoặc dùng spring-dotenv (đã có trong pom.xml)
   ./mvnw spring-boot:run
   ```

---

### **Docker:**

File `docker-compose.simple.yml` đã được config để load từ `.env`:

```yaml
environment:
  AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}
  AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}
```

**Chạy:**
```bash
docker-compose -f docker-compose.simple.yml up -d --build
```

---

### **Production (Render/Railway):**

Set environment variables trên dashboard:

**Render:**
1. Dashboard → Service → Environment
2. Add variables:
   - `AWS_ACCESS_KEY_ID` = your-key
   - `AWS_SECRET_ACCESS_KEY` = your-secret
3. Save Changes → Redeploy

---

## 📋 FILES STRUCTURE

```
d:\WorkSpace\SWD\SWD-BE\
├── .env                              ❌ KHÔNG COMMIT (credentials thật)
├── .env.example                      ✅ COMMIT (template)
├── .gitignore                        ✅ ĐÃ CẬP NHẬT (ignore sensitive files)
├── src/main/resources/
│   ├── application.properties        ❌ KHÔNG COMMIT (credentials thật)
│   ├── application.properties.template ✅ COMMIT (template)
│   ├── application-docker.properties ✅ COMMIT (Docker config)
│   └── application-prod.properties   ❌ KHÔNG COMMIT (Production secrets)
```

---

## ⚠️ QUAN TRỌNG

### **KHÔNG BAO GIỜ COMMIT:**
- `.env`
- `application.properties` (nếu có credentials)
- Any file chứa: passwords, API keys, secrets

### **CÓ THỂ COMMIT:**
- `.env.example`
- `application.properties.template`
- `application-docker.properties` (nếu dùng env vars)

---

## 🔄 WORKFLOW

### **Developer mới join project:**

```bash
# 1. Clone repo
git clone https://github.com/Tanaka-2204/SWD-BE.git
cd SWD-BE

# 2. Copy template
cp .env.example .env
cp src/main/resources/application.properties.template src/main/resources/application.properties

# 3. Nhận credentials từ team lead
# Update .env và application.properties với credentials thật

# 4. Chạy app
./mvnw spring-boot:run
```

---

## 🛡️ BEST PRACTICES

1. ✅ Rotate AWS keys định kỳ (3-6 tháng)
2. ✅ Không share credentials qua Slack/Email
3. ✅ Dùng AWS Secrets Manager cho production
4. ✅ Enable AWS CloudTrail để audit
5. ✅ Restrict IAM permissions (principle of least privilege)

---

## 🔐 NẾU LỠ COMMIT CREDENTIALS

```bash
# Xóa file khỏi Git
git rm --cached .env
git rm --cached src/main/resources/application.properties

# Commit
git commit -m "Remove sensitive files"

# Force push
git push --force

# ROTATE AWS KEYS NGAY!
```

---

## 📞 SUPPORT

Nếu cần credentials cho development, liên hệ:
- Team Lead
- DevOps Engineer
