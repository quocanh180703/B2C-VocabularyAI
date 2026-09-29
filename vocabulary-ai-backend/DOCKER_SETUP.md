# 🐳 Docker Setup Guide - VocabularyAI Backend

## 📋 Yêu Cầu

- Docker 20.10+
- Docker Compose 2.0+
- 2GB RAM (tối thiểu)
- 5GB disk space (tối thiểu)

Kiểm tra cài đặt:
```bash
docker --version
docker-compose --version
```

---

## 🚀 Quick Start

### 1. Dựng MySQL & Redis (Recommended)

```bash
# Vào thư mục project
cd d:\demo-datn\vocabulary-ai-backend

# Start services
docker-compose up -d

# Xem status
docker-compose ps

# Xem logs (follow mode)
docker-compose logs -f

# Stop services
docker-compose down

# Stop + xóa volumes (reset data)
docker-compose down -v
```

### 2. Check Services Status

```bash
# MySQL
docker-compose ps mysql
# Output: Should be "Up X seconds"

# Redis
docker-compose ps redis
# Output: Should be "Up X seconds"

# Adminer (DB UI)
docker-compose ps adminer
# Output: Should be "Up X seconds"
```

### 3. Access Services

| Service | URL/Address | Credentials |
|---------|------------|------------|
| **MySQL** | `localhost:3306` | user: `root`, pass: `180703` |
| **Redis** | `localhost:6379` | No auth |
| **Adminer** | http://localhost:8081 | Login with MySQL creds |

### 4. Adminer Web Interface (MySQL Management)

1. Open: **http://localhost:8081**
2. Login form:
   - Server: `mysql` (or `localhost`)
   - Username: `root`
   - Password: `180703`
   - Database: `datn_2`
3. Click "Login"

---

## 🔍 Troubleshooting

### ❌ Error: "Port 3306 already in use"

```bash
# Option 1: Stop existing MySQL
docker-compose down

# Option 2: Use different port in docker-compose.yml
# Change: ports: - "3307:3306"
```

### ❌ Error: "Docker daemon not running"

```bash
# Start Docker Desktop (Windows/Mac) or Docker service (Linux)
# Windows: Open Docker Desktop app
# Mac: open /Applications/Docker.app
# Linux: sudo systemctl start docker
```

### ❌ Error: "Cannot connect to MySQL"

```bash
# Check MySQL is running
docker-compose ps mysql

# Check logs
docker-compose logs mysql

# Verify with health check
docker-compose exec mysql mysqladmin ping -h localhost -u root -p180703
```

### ❌ Error: "Cannot connect to Redis"

```bash
# Check Redis is running
docker-compose ps redis

# Check logs
docker-compose logs redis

# Ping Redis
docker-compose exec redis redis-cli ping
# Should output: PONG
```

---

## 📊 Data Management

### MySQL Backup

```bash
# Export database
docker-compose exec mysql mysqldump -u root -p180703 datn_2 > backup.sql

# Import database
docker-compose exec -T mysql mysql -u root -p180703 datn_2 < backup.sql
```

### Redis Commands

```bash
# Connect to Redis
docker-compose exec redis redis-cli

# Inside redis-cli:
PING              # Test connection
DBSIZE            # Show number of keys
KEYS *            # List all keys
GET <key>         # Get value
FLUSHALL          # Delete all keys (careful!)
INFO              # Server info
```

### MySQL Commands

```bash
# Connect to MySQL
docker-compose exec mysql mysql -u root -p180703 datn_2

# Inside MySQL:
SHOW TABLES;
SELECT * FROM users;
DESCRIBE users;
```

---

## 🔨 Building Backend Docker Image

### Build Image

```bash
# Build
docker build -t vocabulary-ai-backend:1.0.0 .

# View image
docker images | grep vocabulary-ai

# Run image
docker run -p 8080:8080 \
  --env-file .env \
  --name vocabulary-ai-app \
  vocabulary-ai-backend:1.0.0

# Stop container
docker stop vocabulary-ai-app

# Remove container
docker rm vocabulary-ai-app
```

### Push to Registry (Optional)

```bash
# Tag for Docker Hub
docker tag vocabulary-ai-backend:1.0.0 your-username/vocabulary-ai-backend:1.0.0

# Login to Docker Hub
docker login

# Push
docker push your-username/vocabulary-ai-backend:1.0.0
```

---

## 🌐 Full Stack with Docker

### docker-compose-full.yml (optional)

```yaml
version: '3.8'
services:
  mysql:
    # ... MySQL config
  redis:
    # ... Redis config
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/datn_2
      - SPRING_REDIS_HOST=redis
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
```

Run:
```bash
docker-compose -f docker-compose-full.yml up -d
```

---

## 📝 Environment Variables

File `.env` is loaded automatically:

```env
# Database
DB_USERNAME=root
DB_PASSWORD=180703

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Others...
```

For Docker Compose, use:
```bash
docker-compose --env-file .env up -d
```

---

## ✅ Health Checks

### MySQL Health
```bash
docker-compose exec mysql mysqladmin ping -h localhost -u root -p180703
# Output: mysqld is alive
```

### Redis Health
```bash
docker-compose exec redis redis-cli ping
# Output: PONG
```

### Application Health
```bash
curl http://localhost:8080/api/actuator/health
# Output: {"status":"UP"}
```

---

## 🧹 Cleanup

### Stop all services
```bash
docker-compose down
```

### Stop + remove volumes (reset data)
```bash
docker-compose down -v
```

### Remove all stopped containers
```bash
docker container prune -f
```

### Remove unused images
```bash
docker image prune -a -f
```

---

## 📚 References

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [Adminer Web UI](https://www.adminer.org/)

---

**Generated:** 2024-01-15  
**For:** VocabularyAI Backend Project
