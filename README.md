# 📚 VocabularyAI Backend - Hướng Dẫn Toàn Bộ & Hướng Dẫn Test

**Dự án:** VocabularyAI - Nền tảng học tiếng Anh AI-powered  
**Framework:** Spring Boot 3.3.0, Java 21  
**Database:** MySQL 8.x, Redis 6+  
**Version:** 1.0.0

---

## 📑 Mục Lục

1. [Overview Tổng Quan](#overview-tổng-quan)
2. [Tech Stack](#tech-stack)
3. [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
4. [Cài Đặt & Chạy](#cài-đặt--chạy)
5. [System Prompts Configuration](#system-prompts-configuration-new) ⭐ NEW
6. [Cấu Trúc Project](#cấu-trúc-project)
7. [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
8. [8 Modules Chính](#8-modules-chính)
9. [Flow & Hướng Dẫn Test JSON](#flow--hướng-dẫn-test-json)
10. [Database Schema](#database-schema)
11. [API Reference](#api-reference-quick-lookup)

---

## Overview Tổng Quan

VocabularyAI là một **nền tảng học tiếng Anh AI-powered** toàn diện với:

- 📖 **Quản lý từ vựng** - Học từ mới, favorite, theo dõi tiến độ
- ✅ **Hệ thống quiz** - Kiểm tra kiến thức, tự động chấm điểm
- 🤖 **AI Chat** - Trò chuyện, giải thích từ, kiểm tra ngữ pháp
- 🎤 **Luyện phát âm** - Ghi âm, phân tích phát âm (Whisper STT)
- 📊 **Học tập cá nhân** - Gợi ý thích ứng, lịch học, phân tích yếu điểm
- 🔐 **Xác thực JWT** - Đăng nhập, đăng ký, token refresh
- ⚡ **Caching Redis** - Performance optimization

**Thống kê:**
- 8 Main Modules
- 19 Database Tables
- 80+ API Endpoints
- 15+ Controllers
- 30+ Services

---

## Tech Stack

| Thành Phần | Công Nghệ |
|-----------|----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.3.0 |
| **Security** | Spring Security + JWT |
| **Database** | MySQL 8.x |
| **ORM** | JPA/Hibernate |
| **Migration** | Flyway |
| **Cache** | Redis 6+ |
| **AI** | Google Gemini API |
| **STT** | OpenAI Whisper API |
| **Storage** | Cloudinary CDN |
| **API Doc** | Swagger/OpenAPI 3.0 |
| **Mapping** | MapStruct 1.5.5 |
| **Build** | Maven 3.8.x |
| **Utilities** | Lombok 1.18.30 |

---

## Yêu Cầu Hệ Thống

- ✅ **Java 21 JDK** (hoặc cao hơn)
- ✅ **Maven 3.8.x** (hoặc cao hơn)
- ✅ **MySQL 8.0+**
- ✅ **Redis 6.0+** (tùy chọn nhưng được khuyên dùng)
- ✅ **Git** (để clone project)

---

## Cài Đặt & Chạy

### 1. Clone Project

```bash
git clone <repository-url>
cd vocabulary-ai-backend
```

### 2. Tạo Database

```bash
mysql -u root -p

mysql> CREATE DATABASE datn_2 
       CHARACTER SET utf8mb4 
       COLLATE utf8mb4_unicode_ci;

mysql> EXIT;
```

### 3. Cấu Hình Environment Variables

Tạo file `.env` hoặc set environment variables:

```env
# Database
DB_USERNAME=root
DB_PASSWORD=180703

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT (Min 256 bits!)
JWT_SECRET=dev-secret-key-access-token-change-in-production-min-256-bits-12345678
JWT_REFRESH_SECRET=dev-secret-key-refresh-token-change-in-production-min-256-bits-12345678

# Gemini AI
GEMINI_API_KEY=AQ.Ab8RN6IsF4fZgu5DD1QIR_quqjvbzaWFpGX0kRUQPgYQENp2Fw

# OpenAI Whisper (NEW)
OPENAI_API_KEY=sk-proj-your-api-key-here

# Cloudinary
CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@CLOUD_NAME

# Email (Mailtrap)
MAIL_USERNAME=42125e6c239763
MAIL_PASSWORD=e69e16c700919d

# Server
SERVER_PORT=8080
```

### 4. Cấu Hình application.yml

File `src/main/resources/application.yml` đã sẵn sàng. Nếu cần thay đổi:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/datn_2
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:180703}
```

### 5. Build Project

```bash
mvn clean install
```

### 6. Chạy Application

#### Cách 1: Dùng Maven
```bash
mvn spring-boot:run
```

#### Cách 2: Chạy JAR
```bash
mvn clean package
java -jar target/vocabulary-ai-backend-1.0.0.jar
```

### 7. Kiểm Tra Application

Bạn sẽ thấy log:
```
Started VocabularyAiApplication in X seconds
```

Truy cập:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs
- **Health Check**: http://localhost:8080/api/actuator/health

---

## System Prompts Configuration (NEW - Phase 9) ⭐

### 📝 Tổng Quan

Toàn bộ AI system prompts được **externalize từ hard-coded constants → YAML configuration** để dễ maintain và cấu hình theo môi trường.

**Lợi Ích:**
- ✅ Thay đổi prompt mà không recompile
- ✅ Cấu hình khác nhau cho dev/test/prod
- ✅ Code Java sạch sẽ, dễ đọc
- ✅ Quản lý version prompts dễ dàng
- ✅ Hot-reload prompts (với custom configuration)

### 🔧 Architecture

```java
// 1. Configuration Class
@Configuration
@ConfigurationProperties(prefix = "ai.prompts.system")
@Data
@Getter @Setter
public class SystemPromptConfig {
  private Map<String, Map<String, String>> vocabulary;
  private Map<String, Map<String, String>> grammar;
  private Map<String, Map<String, String>> hint;
  private Map<String, Map<String, String>> chat;
  private Map<String, Map<String, String>> speaking;
  
  // Getters cho từng prompt type
  public String getVocabularySystemPrompt() { ... }
  public String getGrammarSystemPrompt() { ... }
  public String getHintSystemPrompt() { ... }
  public String getChatSystemPrompt() { ... }
  public String getSpeakingAnalysisSystemPrompt() { ... }
  public String getSpeakingFeedbackSystemPrompt() { ... }
  public String getSpeakingCorrectionSystemPrompt() { ... }
}

// 2. Inject vào Service
@Service
@RequiredArgsConstructor
public class PromptEngineeringServiceImpl implements PromptEngineeringService {
  private final SystemPromptConfig systemPromptConfig;
  
  public ChatPrompt buildChatPrompt(String userMessage) {
    String systemPrompt = systemPromptConfig.getChatSystemPrompt();
    // Sử dụng systemPrompt thay vì hard-coded constant
  }
}
```

### 📂 File Configuration

#### Location
```
src/main/resources/application.yml
```

#### Structure
```yaml
ai:
  prompts:
    system:
      vocabulary:
        en: |
          You are an English vocabulary tutor...
          (971 characters)
      grammar:
        en: |
          You are a grammar correction expert...
          (985 characters)
      hint:
        en: |
          You are a learning hint provider...
          (932 characters)
      chat:
        en: |
          You are a helpful English learning assistant...
          (1,136 characters)
      speaking:
        analysis: |
          You are a speaking assessment expert...
          (1,455 characters)
        feedback: |
          You are a speaking coach...
          (1,124 characters)
        correction: |
          You are a speaking correction specialist...
          (870 characters)
```

#### Total Size
- **Vocabulary Prompt**: 971 ký tự
- **Grammar Prompt**: 985 ký tự
- **Hint Prompt**: 932 ký tự
- **Chat Prompt**: 1,136 ký tự
- **Speaking Analysis**: 1,455 ký tự
- **Speaking Feedback**: 1,124 ký tự
- **Speaking Correction**: 870 ký tự
- **TOTAL**: 7,473 ký tự

### 🔄 Migration Path

#### Trước (Hard-coded Constants)
```java
public class PromptConstants {
  public static final String VOCABULARY_PROMPT = 
    "You are an English vocabulary tutor...";
  public static final String GRAMMAR_PROMPT = 
    "You are a grammar correction expert...";
  // ...
}

// Sử dụng
PromptConstants.VOCABULARY_PROMPT
```

#### Sau (Configuration-driven)
```java
// application.yml
ai:
  prompts:
    system:
      vocabulary:
        en: "You are an English vocabulary tutor..."
      grammar:
        en: "You are a grammar correction expert..."

// Sử dụng
systemPromptConfig.getVocabularySystemPrompt()
```

### 🎯 Methods trong SystemPromptConfig

```java
@Configuration
@ConfigurationProperties(prefix = "ai.prompts.system")
@Data
public class SystemPromptConfig {
  
  // Vocabulary Tutor
  public String getVocabularySystemPrompt() {
    return vocabulary.get("en");
  }
  
  // Grammar Correction Expert
  public String getGrammarSystemPrompt() {
    return grammar.get("en");
  }
  
  // Learning Hint Provider
  public String getHintSystemPrompt() {
    return hint.get("en");
  }
  
  // Chat Assistant
  public String getChatSystemPrompt() {
    return chat.get("en");
  }
  
  // Speaking Analysis Evaluator
  public String getSpeakingAnalysisSystemPrompt() {
    return speaking.get("analysis");
  }
  
  // Speaking Feedback Provider
  public String getSpeakingFeedbackSystemPrompt() {
    return speaking.get("feedback");
  }
  
  // Speaking Correction Expert
  public String getSpeakingCorrectionSystemPrompt() {
    return speaking.get("correction");
  }
}
```

### 🧪 Testing

#### Unit Tests
```bash
mvn test -Dtest=SystemPromptConfigTest
# 9/9 tests PASS
```

#### Integration Tests
```bash
mvn test -Dtest=AIIntegrationTest
# 9/9 tests PASS
```

#### Architecture & Smoke Tests
```bash
mvn test -Dtest=ArchitectureAndSmokeTest
# 16/16 tests PASS (7 architecture + 9 smoke tests)
```

#### Chạy tất cả AI tests
```bash
mvn test -Dtest=*Test
```

### 📋 Thay Đổi trong Code

#### 1. SystemPromptConfig.java (NEW)
```
Location: src/main/java/com/vocabularyai/ai/config/SystemPromptConfig.java
Status: ✅ Created
Tests: ✅ 9/9 PASS
```

#### 2. PromptEngineeringServiceImpl.java (MODIFIED)
```
Location: src/main/java/com/vocabularyai/ai/service/prompt/PromptEngineeringServiceImpl.java
Changes:
  - Inject SystemPromptConfig
  - Remove 4 hard-coded constants
  - Update 7 builder methods:
    ✅ buildChatPrompt()
    ✅ buildVocabularyPrompt()
    ✅ buildGrammarPrompt()
    ✅ buildHintPrompt()
    ✅ buildSpeakingAnalysisPrompt()
    ✅ buildSpeakingFeedbackPrompt()
    ✅ buildSpeakingCorrectionPrompt()
Status: ✅ Refactored
```

#### 3. application.yml (MODIFIED)
```
Location: src/main/resources/application.yml
Changes:
  - Added ai.prompts.system section
  - Added 7 system prompts (~500 lines)
  - All prompts moved from Java constants → YAML
Status: ✅ Updated
Total Size: 7,473 ký tự
```

### 🚀 Environment-Specific Configuration

#### Development (application.yml)
```yaml
ai:
  prompts:
    system:
      vocabulary:
        en: "You are a friendly English vocabulary tutor..."
      # ... other prompts
```

#### Production (application-prod.yml)
```yaml
ai:
  prompts:
    system:
      vocabulary:
        en: "You are a professional English vocabulary tutor..."
      # ... optimized prompts for production
```

#### Override via Environment Variables
```bash
export AI_PROMPTS_SYSTEM_VOCABULARY_EN="Custom vocabulary prompt"
export AI_PROMPTS_SYSTEM_GRAMMAR_EN="Custom grammar prompt"

java -jar target/vocabulary-ai-backend-1.0.0.jar
```

### ✅ Verification Checklist

- ✅ SystemPromptConfig loads correctly
- ✅ All 7 prompts loaded from application.yml
- ✅ PromptEngineeringServiceImpl uses config
- ✅ No breaking changes to API
- ✅ 9 unit tests PASS
- ✅ 9 integration tests PASS
- ✅ 16 architecture/smoke tests PASS
- ✅ Spring context starts successfully
- ✅ 535 beans initialized
- ✅ No bean conflicts or duplicates



## Cấu Trúc Project

```
vocabulary-ai-backend/
├── src/
│   ├── main/
│   │   ├── java/com/vocabularyai/
│   │   │   ├── auth/              # 🔐 Authentication & Security
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   ├── user/              # 👤 User Management
│   │   │   ├── vocabulary/        # 📖 Vocabulary Management
│   │   │   ├── learning/          # 📚 Learning Progress
│   │   │   ├── quiz/              # ✅ Quiz System
│   │   │   ├── ai/                # 🤖 AI Integration (Gemini)
│   │   │   ├── speaking/          # 🎤 Speaking Practice
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   │   ├── stt/       # Speech-to-Text
│   │   │   │   │   ├── storage/   # Audio Storage
│   │   │   │   │   └── analysis/  # AI Analysis
│   │   │   │   └── entity/
│   │   │   ├── personalization/   # 📊 Adaptive Learning
│   │   │   ├── common/            # 🔧 Utilities & Exceptions
│   │   │   └── VocabularyAiApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/      # Flyway migrations
│   │           ├── V1__comprehensive_schema.sql
│   │           └── ...
│   └── test/                      # Unit & Integration Tests
├── pom.xml                        # Maven configuration
├── .env                          # Environment variables
└── README.md                     # This file
```

---

## Kiến Trúc Hệ Thống

### High-Level Architecture

```
┌─────────────────────────────────────┐
│  Frontend (React/TypeScript)        │
│  Browser UI ← REST API (JSON)       │
└──────────────┬──────────────────────┘
               │ HTTPS
               ↓
┌──────────────────────────────────────────────────┐
│  Spring Boot Backend (Port 8080)                  │
├──────────────────────────────────────────────────┤
│  Controllers (15)                                 │
│  ├─ AuthController                              │
│  ├─ WordController                              │
│  ├─ LearningController                          │
│  ├─ QuizController                              │
│  ├─ ChatController                              │
│  ├─ GrammarController                           │
│  ├─ SpeakingPracticeController                  │
│  └─ ... (8 more)                               │
├──────────────────────────────────────────────────┤
│  Services Layer (30+)                            │
│  ├─ AuthService, JwtProvider                    │
│  ├─ VocabularyExplanationService (AI)           │
│  ├─ GrammarCheckingService (AI)                 │
│  ├─ SpeakingPracticeService                     │
│  └─ RateLimitingService                         │
├──────────────────────────────────────────────────┤
│  Repositories & Data Access                      │
└──────┬──────────────────┬─────────────┬──────────┘
       │                  │             │
       ↓                  ↓             ↓
   ┌──────────┐    ┌──────────┐   ┌─────────────┐
   │ MySQL 8  │    │ Redis 6+ │   │ Cloudinary  │
   │ (19 tbl) │    │ (Caching)│   │ (Storage)   │
   └──────────┘    └──────────┘   └─────────────┘

External APIs:
├─ Gemini AI (Text/Image/Audio)
├─ OpenAI Whisper (Speech-to-Text)
├─ Cloudinary CDN (Image/Audio Storage)
└─ Mailtrap Email (OTP)
```

---

## 8 Modules Chính

### 1. 🔐 Authentication Module

**Mục đích:** Xác thực, ủy quyền, quản lý token

**Entities:**
- `User` - id, email, username, password, role (ADMIN/STUDENT), status (ACTIVE/INACTIVE/BANNED)
- `RefreshToken` - Token lưu trữ, revocation support
- `OtpVerification` - OTP cho đăng ký và reset password
- `PendingRegistration` - Lưu trữ tạm thời khi đăng ký

**Services:**
- `AuthService` - Login, register, token management
- `JwtProvider` - JWT generation & validation
- `CustomUserDetailsService` - Spring Security integration

**Features:**
- ✅ JWT-based stateless authentication
- ✅ Flexible login (email OR username)
- ✅ User status validation
- ✅ OTP verification
- ✅ Token refresh & revocation

---

### 2. 📖 Vocabulary Management Module

**Mục đích:** Quản lý từ vựng, danh mục, yêu thích

**Entities:**
- `Category` - Danh mục từ vựng
- `Word` - Từ vựng (meaning, example, audio_url, image_url)
- `FavoriteWord` - Từ yêu thích

**Services:**
- `WordService` - CRUD (admin only)
- `CategoryService` - CRUD category
- `FavoriteWordService` - Add/remove favorites
- `VocabularyExplanationService` - Giải thích từ via Gemini

**Features:**
- ✅ Full-text search
- ✅ Level-based filtering (A1-C2)
- ✅ AI-powered explanations
- ✅ Audio & image support

---

### 3. 📚 Learning Progress Module

**Mục đích:** Theo dõi tiến độ học, spaced repetition

**Entities:**
- `LearningProgress` - Tiến độ per word per user
- `LearningHistory` - Lịch sử học tập

**Algorithm: Spaced Repetition**
```
Review Schedule:
- 1st: 1 day after learning
- 2nd: 3 days after 1st
- 3rd: 7 days after 2nd
- 4th: 14 days after 3rd
- 5th: 30 days (Mastered)

If wrong: Reset → Review in 1 day
```

**Services:**
- `LearningService` - Manage progress
- `SpacedRepetitionService` - Calculate review dates
- `ReviewQueueService` - Get words to review

---

### 4. ✅ Quiz System Module

**Mục đích:** Tạo & quản lý quiz, auto-grading

**Entities:**
- `Quiz` - Quiz metadata
- `Question` - MCQ questions
- `QuizAttempt` - User attempts
- `QuizAnswer` - User answers

**Services:**
- `QuizService` - Quiz management
- `QuizGradingService` - Auto-calculate scores

**Features:**
- ✅ MCQ questions
- ✅ Automatic grading
- ✅ Progress integration
- ✅ Detailed explanations

---

### 5. 🤖 AI Integration Module (Gemini)

**Mục đích:** Tích hợp Gemini API cho AI-powered features

**Services:**
```
VocabularyExplanationService
├─ Input: wordId, context
├─ Output: { meaning, examples, synonyms, tips }
├─ Cache: Redis (24 hours)
└─ Rate Limit: Token tracking

GrammarCheckingService
├─ Input: text, language
├─ Output: { errors, corrections, suggestions }
├─ Cache: Redis (7 days)
└─ Rate Limit: Token tracking

LearningHintService
├─ Input: wordId, attemptCount
├─ Output: { hint, difficulty }
├─ Progressive hints (attempt 1, 2, 3...)
├─ Cache: Redis (30 days)
└─ Rate Limit: Token tracking

ChatService
├─ Conversation types: GENERAL, VOCABULARY, GRAMMAR
├─ Create/archive/delete conversations
└─ Stream Gemini responses

Rate Limiting
├─ Daily Tokens: 1M/day
├─ Daily Requests: 1K/day
└─ Tracking: rate_limits table
```

---

### 6. 🎤 Speaking Practice Module (REFACTORING)

**Status:** ⚠️ Hiện tại lỗi (Gemini không hỗ trợ STT)

**Solution:** Thay thế bằng Whisper API

**Flow (After Refactor):**
```
Audio Upload
    ↓
Validate File
    ↓
Upload to Cloudinary
    ↓
Whisper STT (convert audio → transcript)
    ↓
Gemini Analysis (score + feedback)
    ↓
Save to MySQL
    ↓
Return response
```

**Entities:**
- `SpeakingSession` - Session metadata
- `SpeakingAttempt` - Audio + transcript
- `SpeakingFeedback` - Scores + feedback

**Services:**
- `WhisperSpeechToTextService` ✅ (NEW)
- `SpeakingPracticeService` (UPDATE)
- `AudioStorageService` (Cloudinary/S3/Local)
- `SpeakingAIAnalysisService` (Gemini)

---

### 7. 📊 Personalized Learning Module

**Mục đích:** Gợi ý thích ứng, lịch học cá nhân

**Services:**
- `LearningProfileService` - Analyze user profile
- `AdaptiveDifficultyService` - Recommend difficulty
- `PersonalizedRecommendationService` - Suggestions
- `DailyPracticeService` - Daily tasks

**Features:**
- ✅ Weak/strong topic analysis
- ✅ Adaptive difficulty
- ✅ Weekly learning plans
- ✅ Daily practice tasks

---

### 8. 👤 User Management Module

**Mục đích:** Quản lý hồ sơ người dùng

**Services:**
- `UserService` - Profile management
- `UserProfileService` - View/update profile

---

## Flow & Hướng Dẫn Test JSON

### 🔓 FLOW 1: Registration & Login

#### Step 1: Validate & Send OTP

```
POST /api/auth/register/validate
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "username": "johndoe",
  "fullName": "John Doe",
  "password": "SecurePass123!"
}

Response 200:
{
  "message": "OTP sent to email",
  "expiresAt": "2024-01-15T10:35:00Z"
}
```

#### Step 2: Verify OTP & Create Account

```
POST /api/auth/register/verify-otp
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "otp": "123456",
  "username": "johndoe",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}

Response 201:
{
  "message": "Registration successful",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "username": "johndoe",
    "fullName": "John Doe",
    "role": "STUDENT",
    "status": "ACTIVE"
  },
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```

#### Step 3: Login

```
POST /api/auth/login
Content-Type: application/json

Request (Option 1 - Email):
{
  "emailOrUsername": "user@example.com",
  "password": "SecurePass123!"
}

Request (Option 2 - Username):
{
  "emailOrUsername": "johndoe",
  "password": "SecurePass123!"
}

Response 200:
{
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "user@example.com",
    "role": "STUDENT",
    "status": "ACTIVE"
  }
}
```

#### Step 4: Refresh Token

```
POST /api/auth/refresh
Content-Type: application/json

Request:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}

Response 200:
{
  "message": "Token refreshed",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9... (new)",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```

---

### 📖 FLOW 2: Vocabulary Management

#### Get All Words (Paginated & Filtered)

```
GET /api/words?page=0&size=20&level=B1&category=Business
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "content": [
    {
      "id": 1,
      "word": "acquisition",
      "pronunciation": "/ˌækwɪˈzɪʃən/",
      "meaning": "The action of acquiring something",
      "example": "The company announced its acquisition",
      "level": "B1",
      "audioUrl": "https://cdn.example.com/audio/acquisition.mp3",
      "imageUrl": "https://cdn.example.com/images/acquisition.jpg",
      "category": {
        "id": 5,
        "name": "Business"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 250,
    "totalPages": 13
  }
}
```

#### Add to Favorites

```
POST /api/words/1/favorite
Header: Authorization: Bearer <accessToken>

Response 201:
{
  "id": 123,
  "userId": 1,
  "wordId": 1,
  "word": "acquisition",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Get Favorite Words

```
GET /api/words/favorites?page=0&size=20
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "content": [
    { "id": 1, "word": "acquisition", ... },
    { "id": 2, "word": "venture", ... }
  ],
  "pageable": { ... }
}
```

---

### 🎓 FLOW 3: Learning Progress

#### Start Learning Word

```
POST /api/learning/words/1/start
Header: Authorization: Bearer <accessToken>

Response 201:
{
  "id": 42,
  "userId": 1,
  "wordId": 1,
  "word": "acquisition",
  "status": "LEARNING",
  "correctCount": 0,
  "wrongCount": 0,
  "nextReviewAt": "2024-01-16T10:30:00Z"
}
```

#### Review Word (Correct)

```
POST /api/learning/words/1/review
Header: Authorization: Bearer <accessToken>

Request:
{
  "isCorrect": true
}

Response 200:
{
  "id": 42,
  "status": "LEARNING",
  "correctCount": 1,
  "wrongCount": 0,
  "nextReviewAt": "2024-01-19T10:30:00Z"  // +3 days
}
```

#### Review Word (Wrong)

```
POST /api/learning/words/1/review
Header: Authorization: Bearer <accessToken>

Request:
{
  "isCorrect": false
}

Response 200:
{
  "id": 42,
  "status": "LEARNING",
  "correctCount": 1,
  "wrongCount": 1,
  "nextReviewAt": "2024-01-17T10:45:00Z"  // +1 day
}
```

#### Get Review Queue

```
GET /api/learning/review-queue?limit=10
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "content": [
    {
      "wordId": 1,
      "word": "acquisition",
      "daysOverdue": 2,
      "attemptCount": 5
    },
    {
      "wordId": 3,
      "word": "venture",
      "daysOverdue": 1,
      "attemptCount": 3
    }
  ],
  "total": 12
}
```

---

### ✅ FLOW 4: Quiz System

#### Get Published Quizzes

```
GET /api/quizzes?page=0&size=20&level=B1
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "content": [
    {
      "id": 1,
      "title": "Business English - Intermediate",
      "level": "B1",
      "questionCount": 10,
      "timeLimit": 600,
      "category": "Business",
      "status": "PUBLISHED"
    }
  ],
  "pageable": { ... }
}
```

#### Get Quiz Questions

```
GET /api/quizzes/1
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "id": 1,
  "title": "Business English - Intermediate",
  "timeLimit": 600,
  "totalQuestions": 10,
  "questions": [
    {
      "id": 1,
      "content": "What does 'acquisition' mean?",
      "options": [
        "The act of buying",
        "The act of selling",
        "The act of merging",
        "The act of competing"
      ]
    }
  ]
}
```

#### Start Quiz

```
POST /api/quizzes/1/start
Header: Authorization: Bearer <accessToken>

Response 201:
{
  "attemptId": 99,
  "quizId": 1,
  "startedAt": "2024-01-15T10:30:00Z",
  "expiresAt": "2024-01-15T10:40:00Z"
}
```

#### Submit Answers

```
POST /api/quizzes/1/attempts/99/submit
Header: Authorization: Bearer <accessToken>

Request:
{
  "answers": [
    { "questionId": 1, "answer": 0 },
    { "questionId": 2, "answer": 2 },
    { "questionId": 3, "answer": 1 }
  ]
}

Response 200:
{
  "attemptId": 99,
  "score": 80,
  "totalQuestions": 10,
  "correctAnswers": 8,
  "feedback": "Great job! You scored 80%"
}
```

---

### 🤖 FLOW 5: AI Features

#### Get Word Explanation

```
GET /api/v1/ai/vocabulary/explain/1?context=business
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "wordId": 1,
  "word": "acquisition",
  "meaning": "The action of acquiring something",
  "examples": [
    "The company announced its acquisition",
    "His acquisition of knowledge was impressive"
  ],
  "synonyms": ["purchase", "attainment"],
  "usageTips": "Commonly used in business",
  "tokensUsed": 450,
  "source": "CACHE"
}
```

#### Check Grammar

```
POST /api/v1/ai/grammar
Header: Authorization: Bearer <accessToken>

Request:
{
  "text": "He go to the school",
  "language": "en",
  "includeSuggestions": true
}

Response 200:
{
  "originalText": "He go to the school",
  "correctedText": "He goes to school",
  "score": 60,
  "errors": [
    {
      "errorType": "SUBJECT_VERB_AGREEMENT",
      "message": "Verb does not agree",
      "suggestion": "goes"
    }
  ],
  "tokensUsed": 280
}
```

#### Get Learning Hints

```
GET /api/v1/ai/hints/42?attemptCount=1
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "progressId": 42,
  "word": "acquisition",
  "attemptCount": 1,
  "hint": "Think about business - what happens when a company buys another company?",
  "difficulty": "EASY",
  "tokensUsed": 180
}
```

#### AI Chat

```
POST /api/v1/ai/conversations
Header: Authorization: Bearer <accessToken>

Request:
{
  "type": "VOCABULARY",
  "title": "Learning Business English"
}

Response 201:
{
  "id": 15,
  "type": "VOCABULARY",
  "title": "Learning Business English",
  "createdAt": "2024-01-15T10:30:00Z"
}

// Send message:
POST /api/v1/ai/conversations/15/messages
Header: Authorization: Bearer <accessToken>

Request:
{
  "content": "Can you explain what 'due diligence' means?"
}

Response 201:
{
  "id": 1,
  "conversationId": 15,
  "role": "USER",
  "content": "Can you explain what 'due diligence' means?"
}

// AI response (auto-saved):
{
  "id": 2,
  "conversationId": 15,
  "role": "ASSISTANT",
  "content": "Due diligence is the investigation of a business...",
  "tokensUsed": 520
}
```

---

### 🎤 FLOW 6: Speaking Practice (NEW - Whisper STT)

#### Start Speaking Session

```
POST /api/ai/speaking/sessions
Header: Authorization: Bearer <accessToken>

Request:
{
  "topic": "Job Interview Questions",
  "level": "B1"
}

Response 201:
{
  "id": 50,
  "topic": "Job Interview Questions",
  "level": "B1",
  "startedAt": "2024-01-15T10:30:00Z",
  "status": "IN_PROGRESS"
}
```

#### Upload Audio Attempt (Whisper STT)

```
POST /api/ai/speaking/attempts?sessionId=50
Header: Authorization: Bearer <accessToken>
Header: Content-Type: multipart/form-data

Form Data:
  - audio: <binary mp3/wav/webm file, max 50MB>

Processing:
[1] Validate audio file
[2] Upload to Cloudinary
[3] Call Whisper API → Get transcript
[4] Call Gemini API → Get scores + feedback
[5] Save to MySQL
[6] Return response

Response 201:
{
  "attemptId": 125,
  "sessionId": 50,
  "attemptNumber": 1,
  "audioUrl": "https://res.cloudinary.com/.../speaking_50_1.mp3",
  "audioDurationSeconds": 45,
  "transcript": "Good morning. Today I am applying for the position...",
  "transcriptConfidence": 0.92,
  "scores": {
    "grammarScore": 75,
    "vocabularyScore": 82,
    "fluencyScore": 70,
    "contentScore": 88,
    "overallScore": 79
  },
  "feedback": {
    "generalFeedback": "Great effort! Your content was relevant.",
    "suggestions": [
      "Work on fluency - speak more naturally"
    ],
    "strengths": [
      "Well-organized response",
      "Appropriate vocabulary"
    ],
    "weaknesses": [
      "Some hesitation"
    ]
  }
}

// Error: File too large
Response 413:
{
  "status": 413,
  "error": "FileSizeExceededException",
  "message": "File exceeds 50MB limit"
}
```

#### Get Session Detail

```
GET /api/ai/speaking/sessions/50
Header: Authorization: Bearer <accessToken>

Response 200:
{
  "id": 50,
  "topic": "Job Interview Questions",
  "attemptCount": 3,
  "attempts": [
    {
      "id": 125,
      "attemptNumber": 1,
      "overallScore": 79
    },
    {
      "id": 126,
      "attemptNumber": 2,
      "overallScore": 81
    }
  ],
  "statistics": {
    "averageScore": 80,
    "bestScore": 81,
    "improvement": "+2 points"
  }
}
```

---

## Database Schema

### Main Tables

#### users
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  role ENUM('ADMIN', 'STUDENT') DEFAULT 'STUDENT',
  status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### words
```sql
CREATE TABLE words (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  word VARCHAR(255) UNIQUE NOT NULL,
  meaning LONGTEXT NOT NULL,
  example LONGTEXT,
  level ENUM('A1', 'A2', 'B1', 'B2', 'C1', 'C2') DEFAULT 'B1',
  FOREIGN KEY (category_id) REFERENCES categories(id),
  INDEX idx_category_level (category_id, level)
);
```

#### learning_progress
```sql
CREATE TABLE learning_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  word_id BIGINT NOT NULL,
  status ENUM('NEW', 'LEARNING', 'MASTERED') DEFAULT 'NEW',
  correct_count INT DEFAULT 0,
  wrong_count INT DEFAULT 0,
  next_review_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (word_id) REFERENCES words(id),
  UNIQUE KEY unique_user_word (user_id, word_id),
  INDEX idx_next_review_at (next_review_at)
);
```

#### speaking_attempts
```sql
CREATE TABLE speaking_attempts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  audio_url VARCHAR(500) NOT NULL,
  transcript LONGTEXT,
  attempt_number INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (session_id) REFERENCES speaking_sessions(id)
);
```

#### speaking_feedback
```sql
CREATE TABLE speaking_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  attempt_id BIGINT UNIQUE,
  grammar_score DECIMAL(5,2),
  vocabulary_score DECIMAL(5,2),
  fluency_score DECIMAL(5,2),
  content_score DECIMAL(5,2),
  overall_score DECIMAL(5,2),
  feedback LONGTEXT,
  FOREIGN KEY (attempt_id) REFERENCES speaking_attempts(id)
);
```

### Entity Relationships

```
users (1) ──── (N) favorite_words ──── (1) words
users (1) ──── (N) learning_progress ──── (1) words
users (1) ──── (N) quiz_attempts ──── (1) quizzes
users (1) ──── (N) speaking_sessions ──── (N) speaking_attempts ──── (1) speaking_feedback
```

---

## API Reference (Quick Lookup)

| Module | Method | Endpoint | Purpose |
|--------|--------|----------|---------|
| **Auth** | POST | `/api/auth/register/validate` | Validate & send OTP |
| | POST | `/api/auth/register/verify-otp` | Verify OTP |
| | POST | `/api/auth/login` | Login |
| | POST | `/api/auth/refresh` | Refresh token |
| **Vocabulary** | GET | `/api/words` | Get words |
| | POST | `/api/words/{id}/favorite` | Add favorite |
| | GET | `/api/words/favorites` | Get favorites |
| **Learning** | POST | `/api/learning/words/{id}/start` | Start learning |
| | POST | `/api/learning/words/{id}/review` | Review word |
| | GET | `/api/learning/review-queue` | Get review queue |
| **Quiz** | GET | `/api/quizzes` | Get quizzes |
| | POST | `/api/quizzes/{id}/start` | Start quiz |
| | POST | `/api/quizzes/attempts/{id}/submit` | Submit answers |
| **AI** | GET | `/api/v1/ai/vocabulary/explain/{id}` | Explain word |
| | POST | `/api/v1/ai/grammar` | Check grammar |
| | GET | `/api/v1/ai/hints/{id}` | Get hint |
| | POST | `/api/v1/ai/conversations` | Create chat |
| **Speaking** | POST | `/api/ai/speaking/sessions` | Start session |
| | POST | `/api/ai/speaking/attempts` | Upload audio |
| | GET | `/api/ai/speaking/sessions/{id}` | Get session |

**Total: 80+ Endpoints**

---

## Troubleshooting

### ❌ Lỗi: Cannot connect to database
```
✅ Kiểm tra MySQL đang chạy
✅ Kiểm tra DB_USERNAME, DB_PASSWORD
✅ Kiểm tra database datn_2 đã được tạo
```

### ❌ Lỗi: Flyway migration failed
```bash
# Xóa migration history
mysql -u root -p datn_2
mysql> DROP TABLE flyway_schema_history;
mysql> EXIT;

# Chạy lại
mvn spring-boot:run
```

### ❌ Lỗi: Redis connection failed
```
✅ Redis là tùy chọn (dự án vẫn chạy mà không cần)
✅ Nếu cần Redis: redis-server để chạy
```

### ❌ Lỗi: Compile error
```bash
# Kiểm tra Java version
java -version  # Phải là 21

# Clean & rebuild
mvn clean install
```

### ❌ Lỗi: Whisper API không hoạt động
```
✅ Kiểm tra OPENAI_API_KEY đúng
✅ Kiểm tra file audio hợp lệ (mp3, wav, webm)
✅ Kiểm tra OpenAI account có đủ credit
```

---

## Logging

Cấu hình trong `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.vocabularyai: DEBUG
    org.springframework.security: DEBUG
  file:
    name: logs/vocabulary-ai.log
```

---

## Production Deployment

### Environment Variables

```env
# Security
JWT_SECRET=your-very-long-random-secret-key-min-32-chars
JWT_REFRESH_SECRET=your-very-long-random-refresh-key-min-32-chars

# APIs
GEMINI_API_KEY=your-production-gemini-key
OPENAI_API_KEY=your-production-openai-key

# Database
DB_PASSWORD=your-strong-database-password

# Redis
REDIS_PASSWORD=your-redis-password

# Server
SERVER_PORT=8080
```

### Docker Build

```bash
docker build -t vocabulary-ai-backend:1.0.0 .
docker run -p 8080:8080 --env-file .env vocabulary-ai-backend:1.0.0
```

---

## 📞 Support

Nếu gặp vấn đề:
1. ✅ Kiểm tra logs: `tail -f logs/vocabulary-ai.log`
2. ✅ Đọc Troubleshooting section
3. ✅ Đảm bảo các services (MySQL, Redis) đang chạy
4. ✅ Xem Swagger docs: http://localhost:8080/api/swagger-ui.html

---

**VocabularyAI Backend** - Xây dựng để học tập & phát triển! 🚀

**Generated:** 2024-01-15  
**Last Updated:** 2024-01-15
