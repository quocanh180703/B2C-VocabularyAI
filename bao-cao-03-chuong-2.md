# CHƯƠNG 2: PHƯƠNG PHÁP THIẾT KẾ

## 2.1. Phương Pháp Thiết Kế Hệ Thống

### 2.1.1. Quy Trình Thiết Kế

VocabularyAI được thiết kế theo quy trình sau:

**Bước 1: Thu Thập Yêu Cầu**
- Phỏng vấn người dùng (học viên, giáo viên)
- Phân tích các nền tảng hiện có
- Xác định các mục tiêu chính

**Bước 2: Phân Tích Yêu Cầu**
- Tách biệt yêu cầu chức năng (Functional)
- Xác định yêu cầu phi chức năng (Non-Functional)
- Tạo use cases cho từng tính năng

**Bước 3: Thiết Kế Kiến Trúc**
- Kiến trúc 3-layer: Presentation - Business - Data
- Phân chia modules
- Định nghĩa APIs

**Bước 4: Thiết Kế Chi Tiết**
- Database design
- API design (RESTful)
- UI design

**Bước 5: Phát Triển**
- Phát triển theo Agile/Scrum
- Mỗi sprint 2 tuần
- Daily standup & code review

**Bước 6: Kiểm Thử**
- Unit testing
- Integration testing
- System testing

**Bước 7: Triển Khai**
- Deploy lên staging
- User acceptance testing (UAT)
- Deploy lên production

### 2.1.2. Mô Hình Agile/Scrum

VocabularyAI sử dụng Agile/Scrum với:
- Sprint duration: 2 tuần
- Daily standup: 15 phút
- Sprint review & retrospective: cuối sprint
- Backlog grooming: 1-2 lần/sprint

---

## 2.2. Yêu Cầu Chức Năng (Functional Requirements)

### Bảng 2.1: Yêu Cầu Chức Năng

| Mô Đun | Yêu Cầu Chức Năng | Độ Ưu Tiên | Trạng Thái |
|--------|------------------|-----------|-----------|
| **Auth** | Login/Register/OTP/Forgot Password | P0 | ✅ Hoàn thành |
| **Auth** | JWT token generation & refresh | P0 | ✅ Hoàn thành |
| **Auth** | Role-based access control (User/Admin) | P0 | ✅ Hoàn thành |
| **Vocabulary** | CRUD vocabulary | P0 | ✅ Hoàn thành |
| **Vocabulary** | Search & filter words | P0 | ✅ Hoàn thành |
| **Vocabulary** | Favorite words management | P1 | ✅ Hoàn thành |
| **Learning** | Create personalized learning queue | P0 | ✅ Hoàn thành |
| **Learning** | Review queue with spaced repetition | P0 | ✅ Hoàn thành |
| **Learning** | Record learning history | P0 | ✅ Hoàn thành |
| **Quiz** | Create quiz questions | P0 | ✅ Hoàn thành |
| **Quiz** | Quiz submission & scoring | P0 | ✅ Hoàn thành |
| **Quiz** | Quiz analytics & progress | P1 | ✅ Hoàn thành |
| **Speaking** | Record audio input | P1 | ✅ Hoàn thành |
| **Speaking** | Speech recognition (Whisper API) | P1 | ✅ Hoàn thành |
| **Speaking** | Pronunciation scoring | P2 | ✅ Hoàn thành |
| **AI** | AI explanation (Gemini API) | P1 | ✅ Hoàn thành |
| **AI** | AI-generated quiz questions | P2 | ⏳ Nâng cao |
| **Admin** | User management | P1 | ✅ Hoàn thành |
| **Admin** | Word management (CRUD) | P1 | ✅ Hoàn thành |
| **Admin** | System statistics & reports | P1 | ✅ Hoàn thành |
| **User** | View profile & stats | P0 | ✅ Hoàn thành |
| **User** | Update profile | P0 | ✅ Hoàn thành |

### 2.2.1. Chi Tiết Yêu Cầu Chức Năng

#### Module Authentication

1. **User Registration**
   - Nhập: email, password, full name
   - Validate: email format, password strength
   - Output: User account created, OTP sent
   - Constraints: Không cho phép duplicate email

2. **User Login**
   - Nhập: email, password
   - Validate: Email tồn tại, password đúng
   - Output: JWT token (access + refresh token)
   - Constraints: Rate limit: 5 attempts/5 minutes

3. **OTP Verification**
   - Nhập: email, OTP
   - Validate: OTP chính xác, chưa hết hạn (5 phút)
   - Output: Email verified, có thể login
   - Constraints: OTP 6 ký tự số

4. **Forgot Password**
   - Nhập: email
   - Output: OTP sent để reset password
   - Constraints: OTP chỉ hiệu lực 5 phút

#### Module Vocabulary

1. **Get All Words**
   - Params: page, pageSize, search, category
   - Output: List of words with definition
   - Constraints: Default page size = 20

2. **Get Word Detail**
   - Params: wordId
   - Output: Word details (definition, examples, part of speech, collocations)
   - Constraints: Return 404 if not found

3. **Add to Favorite**
   - Params: wordId
   - Output: Word added to favorites
   - Constraints: Duplicates handled gracefully

4. **Remove from Favorite**
   - Params: wordId
   - Output: Word removed from favorites

#### Module Learning

1. **Get Review Queue**
   - Output: Words to review today based on spaced repetition
   - Constraints: Max 50 words per day
   - Logic: Sử dụng SM-2 algorithm để tính interval

2. **Submit Learning Progress**
   - Nhập: wordId, quality (0-5)
   - Output: Learning history recorded, next review date updated
   - Logic: Cập nhật EF (Easiness Factor) dựa trên SM-2

3. **Get Learning Statistics**
   - Output: Tổng từ học, tỷ lệ correct/incorrect, thời gian học
   - Period: Today/Week/Month

#### Module Quiz

1. **Get Quizzes**
   - Output: List of available quizzes
   - Params: category, difficulty

2. **Start Quiz**
   - Nhập: quizId
   - Output: Quiz questions (random order, without answers)

3. **Submit Quiz**
   - Nhập: quizId, answers
   - Output: Score, correct/incorrect count, detailed feedback
   - Logic: Calculate score = (correct / total) * 100

#### Module Speaking

1. **Get Speaking Practice**
   - Output: Random word/sentence to practice
   - Constraints: Only for subscribed users (hoặc trong trial)

2. **Submit Speaking Practice**
   - Nhập: audio file, wordId
   - Output: Transcribed text, pronunciation score
   - Logic: Sử dụng Whisper API để transcribe, so sánh với expected

3. **Get Speaking Result**
   - Output: Pronunciation score, feedback, suggestions

#### Module Admin

1. **Get All Users**
   - Output: User list with stats
   - Params: page, pageSize, search

2. **Get System Statistics**
   - Output: Total users, total words, total quiz submissions
   - Params: period (today/week/month)

---

## 2.3. Yêu Cầu Phi Chức Năng (Non-Functional Requirements)

### Bảng 2.2: Yêu Cầu Phi Chức Năng

| Loại | Yêu Cầu | Tiêu Chí | Ưu Tiên |
|------|---------|---------|--------|
| **Performance** | Response time | < 500ms (95th percentile) | P0 |
| **Performance** | Throughput | >= 1000 req/sec | P0 |
| **Performance** | Database query | < 100ms | P0 |
| **Scalability** | Concurrent users | Support 10,000 | P1 |
| **Scalability** | Database records | Support 10M words | P1 |
| **Availability** | Uptime | 99.5% | P1 |
| **Security** | Data encryption | TLS 1.3 for transmission | P0 |
| **Security** | Password storage | Bcrypt hashing | P0 |
| **Security** | CORS** | Allow only authorized origins | P0 |
| **Security** | SQL injection | Prepared statements | P0 |
| **Security** | XSS prevention | Input validation & output encoding | P0 |
| **Usability** | Load time | < 3s (cold start) | P0 |
| **Usability** | Responsiveness | Mobile, tablet, desktop | P0 |
| **Usability** | Accessibility** | WCAG 2.0 Level A | P2 |
| **Maintainability** | Code coverage | >= 80% | P1 |
| **Maintainability** | Documentation | API docs, code comments | P1 |
| **Compatibility** | Browser** | Chrome, Firefox, Safari, Edge | P1 |
| **Compatibility** | OS** | Windows, Mac, Linux (backend) | P1 |

### 2.3.1. Chi Tiết Yêu Cầu Phi Chức Năng

**Performance:**
- API response time target: 500ms (95th percentile)
- Database query time: < 100ms (with proper indexing)
- Static asset load: Cached, < 1s
- Real-time features: WebSocket latency < 200ms (if needed)

**Scalability:**
- Horizontal scaling: Support multiple instances via load balancer
- Database scaling: Read replicas for analytics
- Cache scaling: Redis cluster support
- File upload: Cloudinary for unlimited storage

**Security:**
- Authentication: JWT with 24-hour access token, 7-day refresh token
- Password: Bcrypt hashing (cost factor 10)
- HTTPS: TLS 1.3 mandatory
- CORS: Whitelist frontend origins
- Rate limiting: 100 requests per minute per IP

**Reliability:**
- Database backup: Daily backups
- Error logging: Centralized logging (ELK stack nếu có)
- Monitoring: Prometheus + Grafana
- Health checks: Every 30 seconds

---

## 2.4. Kiến Trúc Hệ Thống

### 2.4.1. Kiến Trúc Tổng Thể

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                  │
│  React Frontend (UI Components, Pages, Services)      │
└─────────────────┬───────────────────────────────────────┘
                  │ HTTPS / REST API
┌─────────────────▼───────────────────────────────────────┐
│                  API Gateway / Load Balancer            │
│  (Nginx / AWS ALB)                                      │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│               Business Logic Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Auth Module  │  │Vocab Module  │  │ Learning Mod │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Quiz Module  │  │Speaking Mod  │  │  AI Module   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │ Admin Module │  │Personalization │                  │
│  └──────────────┘  └──────────────┘                     │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                 Data Access Layer                       │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │ Spring JPA   │  │  Repository  │                     │
│  └──────────────┘  └──────────────┘                     │
└─────────────────┬───────────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
    ┌───▼─────┐      ┌─────▼────┐
    │  MySQL  │      │  Redis   │
    └─────────┘      └──────────┘

External Services:
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│ Google Gemini API│ │Google Whisper API│ │  Cloudinary API  │
└──────────────────┘ └──────────────────┘ └──────────────────┘
```

### 2.4.2. Module Architecture

**Backend Modules (Spring Boot):**

1. **Auth Module**
   - Classes: AuthController, AuthService, JwtProvider, OtpService
   - Database: users, otp_logs
   - Endpoints: /api/auth/register, /api/auth/login, /api/auth/verify-otp

2. **Vocabulary Module**
   - Classes: WordController, WordService, WordRepository
   - Database: words, word_meanings, collocations
   - Endpoints: /api/words, /api/words/{id}, /api/words/search

3. **Learning Module**
   - Classes: LearningController, LearningService, LearningRepository
   - Database: learning_progress, learning_history
   - Endpoints: /api/learning/review-queue, /api/learning/progress

4. **Quiz Module**
   - Classes: QuizController, QuizService, QuestionService
   - Database: quizzes, questions, quiz_submissions, answers
   - Endpoints: /api/quizzes, /api/quizzes/{id}/questions

5. **Speaking Module**
   - Classes: SpeakingController, SpeakingService, WhisperIntegration
   - Database: speaking_practice, speaking_records
   - Endpoints: /api/speaking/practice, /api/speaking/submit

6. **AI Module**
   - Classes: AiController, GeminiIntegration, PromptService
   - External: Google Gemini API
   - Endpoints: /api/ai/explain, /api/ai/generate-quiz

7. **Admin Module**
   - Classes: AdminController, AdminService
   - Endpoints: /api/admin/users, /api/admin/statistics

8. **User Module**
   - Classes: UserController, UserService
   - Endpoints: /api/users/profile, /api/users/update

---

## 2.5. Thiết Kế Cơ Sở Dữ Liệu

### 2.5.1. Entity-Relationship Diagram (ERD)

```
users (1) ──────── (N) learning_progress
users (1) ──────── (N) quiz_submissions
users (1) ──────── (N) favorites
users (1) ──────── (N) speaking_records
users (1) ──────── (N) learning_history

words (1) ──────── (N) learning_progress
words (1) ──────── (N) quiz_submissions
words (1) ──────── (N) favorites
words (1) ──────── (N) speaking_records
words (1) ──────── (N) collocations
words (1) ──────── (N) word_meanings

categories (1) ──────── (N) words

quizzes (1) ──────── (N) questions
quizzes (1) ──────── (N) quiz_submissions

questions (1) ──────── (N) answers

quiz_submissions (1) ──────── (N) answers
```

### 2.5.2. Bảng Dữ Liệu Chính

**Bảng 2.3: Schema của 19 Bảng Dữ Liệu**

| Bảng | Cột | Kiểu | Mô Tả |
|------|-----|------|-------|
| **users** | id | BIGINT PK | User ID |
| | email | VARCHAR | Email (UNIQUE) |
| | password | VARCHAR | Hashed password |
| | full_name | VARCHAR | Tên người dùng |
| | role | ENUM | USER / ADMIN |
| | created_at | TIMESTAMP | Ngày tạo |
| **words** | id | BIGINT PK | Word ID |
| | word_text | VARCHAR | Từ vựng (UNIQUE) |
| | part_of_speech | VARCHAR | Loại từ (noun, verb...) |
| | level | ENUM | A1-C2 |
| | category_id | BIGINT FK | Category ID |
| **learning_progress** | id | BIGINT PK | Progress ID |
| | user_id | BIGINT FK | User ID |
| | word_id | BIGINT FK | Word ID |
| | quality | INT | Chất lượng trả lời (0-5) |
| | next_review | DATE | Ngày ôn tập tiếp |
| | repetition | INT | Số lần đã ôn |
| | easiness_factor | FLOAT | Hệ số dễ nhớ (SM-2) |
| | interval | INT | Khoảng thời gian (days) |
| | created_at | TIMESTAMP | Ngày tạo |
| **quizzes** | id | BIGINT PK | Quiz ID |
| | title | VARCHAR | Tên quiz |
| | category_id | BIGINT FK | Category ID |
| | total_questions | INT | Số câu hỏi |
| **quiz_submissions** | id | BIGINT PK | Submission ID |
| | user_id | BIGINT FK | User ID |
| | quiz_id | BIGINT FK | Quiz ID |
| | score | FLOAT | Điểm (0-100) |
| | submitted_at | TIMESTAMP | Thời gian submit |

### 2.5.3. Indexing Strategy

Để tối ưu performance, tạo indexes cho:

```sql
-- Bảng users
CREATE INDEX idx_users_email ON users(email);

-- Bảng words
CREATE INDEX idx_words_category ON words(category_id);
CREATE INDEX idx_words_level ON words(level);

-- Bảng learning_progress
CREATE INDEX idx_learning_user_word ON learning_progress(user_id, word_id);
CREATE INDEX idx_learning_next_review ON learning_progress(user_id, next_review);

-- Bảng quiz_submissions
CREATE INDEX idx_quiz_submissions_user ON quiz_submissions(user_id);
CREATE INDEX idx_quiz_submissions_quiz ON quiz_submissions(quiz_id);
```

---

## 2.6. Lựa Chọn Công Nghệ

### 2.6.1. Tiêu Chí Lựa Chọn

1. **Popularity & Community Support**
   - Phải có cộng đồng lớn, resources nhiều
   - Ví dụ: Java > C#, React > Vue

2. **Performance**
   - Backend: Java > Python (runtime performance)
   - Frontend: React/Vue > jQuery

3. **Scalability**
   - Spring Boot hỗ trợ clustering
   - React hỗ trợ lazy loading, code splitting

4. **Developer Productivity**
   - TypeScript > JavaScript (type safety)
   - Spring Boot > bare Java (boilerplate reduction)

5. **Cost**
   - Free/open-source preferred
   - Google Gemini < GPT-4 (cost)

### 2.6.2. Technology Stack Final

**Backend:**
```
Java 21
├── Spring Boot 3.3.0
├── Spring Data JPA (Hibernate)
├── Spring Security (JWT)
├── Spring Cloud (nếu microservices sau)
├── Maven (build)
├── MySQL 8.x (database)
├── Redis 6+ (cache)
├── Lombok (boilerplate reduction)
├── Junit 5 (testing)
└── Mockito (mocking)
```

**Frontend:**
```
React 19.2.8 + TypeScript
├── Vite (bundler)
├── Axios (HTTP client)
├── React Router (routing)
├── Tailwind CSS (styling)
├── ESLint (code quality)
└── Jest (testing)
```

**External Services:**
```
Google APIs
├── Gemini API (AI explanation)
├── Whisper API (speech recognition)
└── Cloudinary (image upload)

Email Service
└── Gmail SMTP (email sending)
```

**DevOps:**
```
├── Docker & Docker Compose
├── GitHub Actions (CI/CD)
├── Linux servers (deployment)
└── Nginx (reverse proxy)
```

---

## 2.7. Mô Tả Các Thuật Toán

### 2.7.1. Spaced Repetition Algorithm (SM-2)

**Thuật toán:**

```javascript
function calculateNextReview(quality, lastInterval, lastEF) {
  let newEF = lastEF + (0.1 - (5 - quality) * 0.08);
  
  // EF không được < 1.3
  if (newEF < 1.3) newEF = 1.3;
  
  let newInterval;
  if (quality < 3) {
    // Trả lời sai, reset
    newInterval = 1;
  } else if (lastInterval === 0) {
    // Lần đầu tiên
    newInterval = 1;
  } else if (lastInterval === 1) {
    // Lần thứ 2
    newInterval = 3;
  } else {
    // Lần thứ 3+
    newInterval = Math.round(lastInterval * newEF);
  }
  
  return {
    interval: newInterval,
    easinessFactor: newEF,
    nextReviewDate: addDays(today(), newInterval)
  };
}
```

**Ví dụ cụ thể:**
- Học từ "persist" (lần đầu)
  - Quality = 5 (đúng ngay lập tức)
  - EF mới = 2.5 (mặc định)
  - Interval mới = 1 ngày
  - Next review = Ngày mai

- Ôn tập "persist" (lần thứ 2, sau 1 ngày)
  - Quality = 4 (đúng nhưng chậm)
  - EF mới = 2.5 + (0.1 - (5-4)*0.08) = 2.42
  - Interval mới = 3 ngày
  - Next review = Ngày hôm nay + 3 ngày

### 2.7.2. Personalized Learning Queue Algorithm

**Input:**
- User: user_id
- Today: current date

**Output:**
- List of words to review today

**Thuật toán:**

```javascript
function getReviewQueueForToday(userId, today) {
  // 1. Lấy tất cả learning_progress của user
  const userProgress = getProgress(userId);
  
  // 2. Lọc những từ cần ôn hôm nay (next_review <= today)
  const wordsToReview = userProgress.filter(
    p => p.nextReview <= today
  );
  
  // 3. Sort theo độ ưu tiên:
  //    - Những từ sắp hết hạn (next_review gần nhất trước)
  //    - Những từ có tỷ lệ sai cao (quality < 3)
  const sorted = wordsToReview.sort((a, b) => {
    // Ưu tiên 1: Những từ sắp hết hạn
    if (a.nextReview !== b.nextReview) {
      return a.nextReview - b.nextReview;
    }
    // Ưu tiên 2: Những từ khó (quality thấp)
    return a.quality - b.quality;
  });
  
  // 4. Limit: Max 50 từ/ngày
  return sorted.slice(0, 50);
}
```

### 2.7.3. AI Explanation Generation

**Input:**
- word: từ vựng
- context: ngữ cảnh (optional)

**Output:**
- explanation: Giải thích
- examples: Danh sách ví dụ

**Thuật toán:**

```javascript
async function generateExplanation(word, context) {
  // 1. Check cache (Redis)
  const cached = await redis.get(`explanation:${word}`);
  if (cached) return cached;
  
  // 2. Call Gemini API
  const prompt = `
    Explain the word "${word}" in simple English.
    Include:
    1. Definition
    2. Part of speech
    3. 3 example sentences
    4. Common collocations
  `;
  
  const response = await geminiAPI.generate(prompt);
  
  // 3. Cache result for 30 days
  await redis.set(`explanation:${word}`, response, 'EX', 30*24*3600);
  
  return response;
}
```

---

## 2.8. Tóm Tắt Chương

**Chương 2 đã trình bày:**

1. **Phương pháp thiết kế**: Quy trình 7 bước từ thu thập yêu cầu đến triển khai

2. **Yêu cầu chức năng**: 22 yêu cầu chức năng được phân chia thành 8 module

3. **Yêu cầu phi chức năng**: 18 yêu cầu về performance, scalability, security

4. **Kiến trúc hệ thống**: 3-layer architecture, 8 modules, module interaction

5. **Thiết kế cơ sở dữ liệu**: 19 bảng, relationship, indexing strategy

6. **Lựa chọn công nghệ**: Java 21 + Spring Boot 3.3.0 + React 19.2.8

7. **Các thuật toán**: SM-2 Spaced Repetition, Learning Queue, AI Explanation

Với thiết kế chi tiết này, **Chương 3** sẽ trình bày chi tiết quá trình triển khai từng tính năng của VocabularyAI.

---

**Hết Chương 2**
