# VocabularyAI Backend

Backend cho nền tảng học tiếng Anh VocabularyAI với tích hợp AI (Gemini).

## Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 3.3.0
- **Database**: MySQL 8.x
- **ORM**: JPA/Hibernate
- **Migration**: Flyway
- **Security**: Spring Security + JWT
- **Cache**: Redis
- **AI**: Google Gemini API
- **API Documentation**: Swagger/OpenAPI
- **Mapping**: MapStruct
- **Build**: Maven
- **Utilities**: Lombok

## Yêu cầu hệ thống

- Java 21 JDK
- Maven 3.8.x hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- Redis 6.0 hoặc cao hơn (tùy chọn)

## Cài đặt và chạy

### 1. Tạo Database

```bash
mysql -u root -p

mysql> CREATE DATABASE vocabulary_ai 
       CHARACTER SET utf8mb4 
       COLLATE utf8mb4_unicode_ci;

mysql> EXIT;
```

### 2. Cấu hình Environment Variables

Tạo tệp `.env` trong thư mục project root:

```env
# Database
DB_PASSWORD=your_mysql_password
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_REFRESH_SECRET=your-super-secret-refresh-key-change-this-in-production

# Gemini AI
GEMINI_API_KEY=your-gemini-api-key

# Server
SERVER_PORT=8080
```

### 3. Cấu hình application.yml

File `src/main/resources/application.yml` đã được cấu hình. Nếu cần thay đổi:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vocabulary_ai
    username: root
    password: ${DB_PASSWORD:root}
```

### 4. Build Project

```bash
mvn clean install
```

### 5. Chạy Application

#### Cách 1: Dùng Maven
```bash
mvn spring-boot:run
```

#### Cách 2: Chạy JAR
```bash
mvn clean package
java -jar target/vocabulary-ai-backend-1.0.0.jar
```

### 6. Kiểm tra Application

Sau khi chạy, bạn sẽ thấy:

```
Started VocabularyAiApplication in X seconds
```

**Swagger UI**: http://localhost:8080/api/swagger-ui.html

**API Docs**: http://localhost:8080/api/v3/api-docs

**Health Check**: http://localhost:8080/api/actuator/health

## Cấu trúc Project

```
src/main/java/com/vocabularyai/
├── auth/                 # Authentication & Authorization
├── user/                 # User Management
├── vocabulary/           # Vocabulary Management
├── learning/             # Learning Progress & History
├── quiz/                 # Quiz System
├── ai/                   # AI Integration
├── speaking/             # Speaking Practice
├── common/               # Common utilities
└── VocabularyAiApplication.java

src/main/resources/
├── application.yml       # Spring Boot Configuration
└── db/migration/
    └── V1__init_schema.sql  # Database Schema
```

## Database Migrations

Project sử dụng **Flyway** để quản lý database migrations.

- Tất cả migrations nằm trong `src/main/resources/db/migration/`
- Naming convention: `V{number}__{description}.sql`
- Migrations sẽ tự động chạy khi ứng dụng khởi động

**Lưu ý**: 
- Không bao giờ sửa migration đã chạy (V1__init_schema.sql)
- Các thay đổi sau phải tạo V2, V3, ... etc

## API Endpoints

### Hiện tại (Phase 1)
- Health Check: `GET /api/actuator/health`

### Sắp tới
- Authentication (Register, Login, Refresh Token)
- User Management
- Vocabulary CRUD & Search
- Learning Management
- Quiz System
- AI Chat & Features
- Speaking Practice

## Development Order

Dự án được xây dựng theo các Phase:

1. **Phase 1**: Project Setup (✓ Hoàn tất)
2. **Phase 2**: Common (Exception Handler, Response, Utils)
3. **Phase 3**: User & Authentication
4. **Phase 4**: Category & Vocabulary
5. **Phase 5**: Learning
6. **Phase 6**: Quiz
7. **Phase 7**: AI
8. **Phase 8**: Speaking
9. **Phase 9**: Redis
10. **Phase 10**: Testing

## Troubleshooting

### Lỗi: "Cannot connect to database"
- Kiểm tra MySQL đã chạy
- Kiểm tra credentials trong `.env`
- Kiểm tra database `vocabulary_ai` đã được tạo

### Lỗi: "Flyway migration failed"
- Xóa flyway_schema_history từ database và chạy lại
```bash
mysql -u root -p vocabulary_ai
mysql> DROP TABLE flyway_schema_history;
mysql> EXIT;
```
- Sau đó chạy lại application

### Lỗi: "Redis connection failed"
- Kiểm tra Redis đã chạy (tùy chọn, không bắt buộc cho Phase 1)
- Redis sẽ được sử dụng từ Phase 9

### Lỗi: Compile error
- Kiểm tra Java version: `java -version` (phải là 21)
- Clean Maven: `mvn clean`
- Rebuild: `mvn install`

## Logging

Log level được cấu hình trong `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.vocabularyai: DEBUG
    org.springframework.security: DEBUG
```

Thay đổi level nếu cần debug chi tiết.

## Swagger/OpenAPI

Sau khi chạy, truy cập: **http://localhost:8080/api/swagger-ui.html**

Swagger sẽ hiển thị tất cả endpoints đã được annotate với `@OpenAPI` / `@Operation`.

## Environment Variables (Production)

```env
# Thay đổi các giá trị dưới đây cho production:
JWT_SECRET=your-very-long-random-secret-key-min-32-chars
JWT_REFRESH_SECRET=your-very-long-random-refresh-key-min-32-chars
GEMINI_API_KEY=your-production-gemini-api-key
DB_PASSWORD=your-strong-database-password
REDIS_PASSWORD=your-redis-password-if-needed
```

## Liên hệ & Support

Nếu gặp vấn đề, hãy:
1. Kiểm tra logs
2. Xem Troubleshooting section
3. Đảm bảo tất cả services (MySQL, Redis) đã chạy

---

**VocabularyAI Backend** - Xây dựng để học tập và phát triển! 🚀
