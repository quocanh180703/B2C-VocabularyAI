# TASK 18: MVP Verification - AI Speaking Module Testing Guide

**Phase:** Phase 8  
**Task:** 18/19 (MVP Manual Testing)  
**Status:** In Progress  
**Build Status:** ✅ mvn package -DskipTests SUCCESS

---

## 📋 Overview

TASK 18 là **MVP Verification** - kiểm chứng happy path của toàn bộ Speaking Module (TASK 1-17) thông qua manual testing với Postman.

### Scope
- Test tất cả 32 endpoints của Speaking Module (TASK 14-17)
- Verify request/response format đúng
- Check error handling (400, 401, 403, 404, 429, 500)
- Validate JWT authentication
- Verify ownership checks
- Test pagination
- Happy path only (NO integration tests với Gemini API)

### Not In Scope
- Unit tests (deferred to Final Testing phase)
- Integration tests với Gemini AI
- Load testing / performance testing
- Edge cases beyond happy path

---

## 🔧 Prerequisites

1. **Start the application:**
   ```bash
   cd d:\demo-datn\vocabulary-ai-backend
   mvn spring-boot:run
   ```
   Server sẽ run ở `http://localhost:8080`

2. **Database setup:**
   - MySQL running on localhost:3306
   - Database: `datn_2`
   - Flyway migrations applied automatically

3. **Environment variables (optional):**
   ```bash
   set GEMINI_API_KEY=your-api-key
   set JWT_SECRET=your-secret
   ```

4. **Test tools:**
   - Postman (recommended) hoặc Thunder Client
   - Browser (xem Swagger UI)
   - cURL (nếu muốn manual test)

---

## 📖 API Documentation

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### API Docs (JSON)
```
http://localhost:8080/v3/api-docs
```

---

## 🧪 Test Scenarios

### SCENARIO 1: Authentication & Session Management

#### 1.1 Login (Phase 7 - AI Chat Module)
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test.user@example.com",
  "password": "your-password"
}

Expected Response:
{
  "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "user": {
    "id": 1,
    "email": "test.user@example.com"
  }
}
```

**Important:** Lưu `accessToken` - dùng cho tất cả subsequent requests

#### 1.2 Use Bearer Token
```http
Authorization: Bearer {accessToken}
```

---

### SCENARIO 2: Speaking Session Management (TASK 14)

#### 2.1 Create Speaking Session
```http
POST http://localhost:8080/api/ai/speaking/sessions
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "topic": "My Daily Routine",
  "level": "B1",
  "description": "Talk about your typical day"
}

Expected Response: 201 Created
{
  "id": 1,
  "topic": "My Daily Routine",
  "level": "B1",
  "status": "IN_PROGRESS",
  "attemptCount": 0,
  "overallScore": null,
  "createdAt": "2026-08-26T10:30:00"
}
```

**Save sessionId = 1** - dùng cho steps tiếp theo

#### 2.2 Get Session List
```http
GET http://localhost:8080/api/ai/speaking/sessions
Authorization: Bearer {accessToken}

Query Parameters:
- page=0 (default)
- size=20 (default)
- sort=createdAt,desc (default)

Expected Response: 200 OK
{
  "content": [
    {
      "id": 1,
      "topic": "My Daily Routine",
      "level": "B1",
      "status": "IN_PROGRESS",
      ...
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

#### 2.3 Get Session Detail
```http
GET http://localhost:8080/api/ai/speaking/sessions/{sessionId}
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK
{
  "id": 1,
  "topic": "My Daily Routine",
  "level": "B1",
  "status": "IN_PROGRESS",
  "attemptCount": 0,
  "description": "Talk about your typical day",
  "createdAt": "2026-08-26T10:30:00"
}
```

#### 2.4 Filter by Status
```http
GET http://localhost:8080/api/ai/speaking/sessions/status/IN_PROGRESS
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "content": [
    { session object }
  ]
}
```

#### 2.5 Filter by Level
```http
GET http://localhost:8080/api/ai/speaking/sessions/level/B1
Authorization: Bearer {accessToken}

Expected Response: 200 OK
```

#### 2.6 Get Latest In-Progress Session
```http
GET http://localhost:8080/api/ai/speaking/sessions/latest/in-progress
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "id": 1,
  ...
}
```

---

### SCENARIO 3: Speaking Practice - Upload & Analysis (TASK 15)

#### 3.1 Prepare Audio File
- Dùng file audio khô (test file): `sample.mp3` hoặc `sample.wav`
- Kích thước: < 10 MB
- Format: MP3, WAV, WEBM, OGG, M4A, FLAC

**Create test audio (using ffmpeg):**
```bash
# Generate 5 second silence
ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 5 -q:a 9 -acodec libmp3lame test-audio.mp3

# Or use an existing audio file
```

#### 3.2 Submit Speaking Attempt
```http
POST http://localhost:8080/api/ai/speaking/attempts
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

Parameters:
- sessionId: 1 (form field)
- audioFile: {file} (binary file)

Expected Response: 201 Created
{
  "session": {
    "id": 1,
    "topic": "My Daily Routine",
    "level": "B1",
    "status": "IN_PROGRESS",
    "overallScore": null
  },
  "attempt": {
    "id": 1,
    "attemptNumber": 1,
    "audioUrl": "/tmp/vocabulary-ai/audio/speaking_1_attempt_1.mp3",
    "audioDurationSeconds": 5,
    "transcript": "sample transcript from STT",
    "transcriptConfidence": 0.95,
    "createdAt": "2026-08-26T10:35:00"
  },
  "scores": {
    "overallScore": 75.5,
    "grammarScore": 78.0,
    "vocabularyScore": 72.0,
    "fluencyScore": 76.0,
    "contentScore": 74.0,
    "pronunciationScore": null
  },
  "feedback": {
    "feedback": "Good job! Your pronunciation...",
    "corrections": [...],
    "suggestions": [...],
    "strengths": [...],
    "weaknesses": [...]
  },
  "pronunciation": null,
  "statistics": {
    "totalAttempts": 1,
    "averageScore": 75.5,
    "bestScore": 75.5,
    "improvement": null
  }
}
```

**Save attemptId = 1** - dùng để lấy feedback

#### 3.3 Get Practice Session Info
```http
GET http://localhost:8080/api/ai/speaking/attempts/{sessionId}
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK
{
  "session": { ... },
  "attempt": { last attempt info },
  "scores": { ... },
  "feedback": { ... },
  "statistics": {
    "totalAttempts": 1,
    "averageScore": 75.5,
    "bestScore": 75.5
  }
}
```

#### 3.4 Error Case: Invalid Audio File
```http
POST http://localhost:8080/api/ai/speaking/attempts
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

Parameters:
- sessionId: 1
- audioFile: {text file or > 10MB file}

Expected Response: 400 Bad Request
{
  "error": "Invalid audio file"
}
```

#### 3.5 Error Case: Session Not Found
```http
POST http://localhost:8080/api/ai/speaking/attempts
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

Parameters:
- sessionId: 99999
- audioFile: {valid file}

Expected Response: 404 Not Found
{
  "error": "Session not found"
}
```

---

### SCENARIO 4: Feedback Management (TASK 16)

#### 4.1 Get Attempt Feedback
```http
GET http://localhost:8080/api/ai/speaking/feedback/attempts/{attemptId}
Authorization: Bearer {accessToken}
attemptId = 1

Expected Response: 200 OK
{
  "id": 1,
  "attemptId": 1,
  "sessionId": 1,
  "overallScore": 75.5,
  "grammarScore": 78.0,
  "vocabularyScore": 72.0,
  "fluencyScore": 76.0,
  "contentScore": 74.0,
  "pronunciationScore": null,
  "feedback": "Good job!...",
  "corrections": "[ JSON or String ]",
  "suggestions": "[ JSON or String ]",
  "strengths": "[ JSON or String ]",
  "weaknesses": "[ JSON or String ]",
  "createdAt": "2026-08-26T10:35:00"
}
```

#### 4.2 Get Session Feedback Summary
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK
{
  "id": 1,
  "sessionId": 1,
  "overallScore": 75.5,
  ...
}

Or if no feedback:
Expected Response: 404 Not Found
```

#### 4.3 List All Feedback in Session
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}/list
Authorization: Bearer {accessToken}
sessionId = 1

Query Parameters:
- page=0
- size=20
- sort=createdAt,desc

Expected Response: 200 OK
{
  "content": [
    { feedback object }
  ],
  "totalElements": 1
}
```

#### 4.4 Get Best Attempt in Session
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}/best
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK
{
  "id": 1,
  "overallScore": 75.5,
  ...
}
```

#### 4.5 Sort Feedback by Overall Score
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-score
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK (sorted by score descending)
```

#### 4.6 Sort by Grammar Score
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-grammar
Authorization: Bearer {accessToken}

Expected Response: 200 OK
```

#### 4.7 Sort by Vocabulary Score
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-vocabulary
Authorization: Bearer {accessToken}

Expected Response: 200 OK
```

#### 4.8 Get Session Statistics
```http
GET http://localhost:8080/api/ai/speaking/feedback/sessions/{sessionId}/statistics
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK
{
  "overallScore": 75.5,
  "grammarScore": 78.0,
  "vocabularyScore": 72.0,
  "fluencyScore": 76.0,
  "contentScore": 74.0,
  "attemptCount": 1
}
```

---

### SCENARIO 5: Progress Tracking (TASK 17)

#### 5.1 Get Full Speaking Progress
```http
GET http://localhost:8080/api/ai/speaking/progress
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "statistics": {
    "totalSessions": 1,
    "completedSessions": 0,
    "totalAttempts": 1,
    "totalTimeSpent": 5,
    "averageSessionDuration": 5
  },
  "averageScores": {
    "overallScore": 75.5,
    "grammarScore": 78.0,
    "vocabularyScore": 72.0,
    "fluencyScore": 76.0,
    "contentScore": 74.0,
    "pronunciationScore": null
  },
  "bestScores": {
    "overallScore": 75.5,
    ...
  },
  "recentPerformance": {
    "last7Days": {...},
    "last30Days": {...}
  },
  "streaks": {
    "currentStreak": 1,
    "longestStreak": 1
  },
  "achievements": [
    "First speaking session!"
  ],
  "scoreTrend": "Improving",
  "lastUpdateAt": "2026-08-26T10:35:00"
}
```

#### 5.2 Get Score Trend (7 days)
```http
GET http://localhost:8080/api/ai/speaking/progress/trend/7
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "days": 7,
  "scoreTrend": "Improving",
  "analyzedAt": "2026-08-26T10:40:00"
}
```

#### 5.3 Get Score Trend (30 days)
```http
GET http://localhost:8080/api/ai/speaking/progress/trend/30
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "days": 30,
  "scoreTrend": "Stable",
  "analyzedAt": "2026-08-26T10:40:00"
}
```

#### 5.4 Error: Invalid Days Parameter
```http
GET http://localhost:8080/api/ai/speaking/progress/trend/15
Authorization: Bearer {accessToken}

Expected Response: 400 Bad Request
{
  "error": "Days must be 7 or 30"
}
```

#### 5.5 Get Milestones
```http
GET http://localhost:8080/api/ai/speaking/progress/milestones
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "milestones": [
    "First speaking session!",
    ...
  ],
  "totalMilestones": 1,
  "retrievedAt": "2026-08-26T10:40:00"
}
```

#### 5.6 Get Progress Summary (Dashboard)
```http
GET http://localhost:8080/api/ai/speaking/progress/summary
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "totalSessions": 1,
  "completedSessions": 0,
  "totalAttempts": 1,
  "averageScore": 75.5,
  "bestScore": 75.5,
  "currentStreak": 1,
  "scoreTrend": "Improving",
  "nextMilestone": "5 speaking sessions!"
}
```

---

### SCENARIO 6: Complete Session Flow

#### 6.1 Complete Session
```http
PUT http://localhost:8080/api/ai/speaking/sessions/{sessionId}/complete
Authorization: Bearer {accessToken}
sessionId = 1

Expected Response: 200 OK
{
  "id": 1,
  "status": "COMPLETED",
  "overallScore": 75.5,
  ...
}
```

#### 6.2 Get Completed Session
```http
GET http://localhost:8080/api/ai/speaking/sessions/{sessionId}
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "id": 1,
  "status": "COMPLETED",
  ...
}
```

#### 6.3 Get Top Sessions (Completed sorted by score)
```http
GET http://localhost:8080/api/ai/speaking/sessions/completed/sorted-by-score
Authorization: Bearer {accessToken}

Query: page=0, size=10

Expected Response: 200 OK
{
  "content": [
    {
      "id": 1,
      "status": "COMPLETED",
      "overallScore": 75.5,
      ...
    }
  ]
}
```

---

### SCENARIO 7: Session Cancellation & Deletion

#### 7.1 Cancel Session
```http
PUT http://localhost:8080/api/ai/speaking/sessions/{sessionId}/cancel
Authorization: Bearer {accessToken}

Expected Response: 200 OK
{
  "id": 1,
  "status": "CANCELLED",
  ...
}
```

#### 7.2 Delete Session (only for cancelled/completed)
```http
DELETE http://localhost:8080/api/ai/speaking/sessions/{sessionId}
Authorization: Bearer {accessToken}

Expected Response: 204 No Content
```

---

### SCENARIO 8: Security Tests

#### 8.1 Missing Authorization Header
```http
GET http://localhost:8080/api/ai/speaking/sessions
(No Authorization header)

Expected Response: 401 Unauthorized
```

#### 8.2 Invalid Token
```http
GET http://localhost:8080/api/ai/speaking/sessions
Authorization: Bearer invalid-token

Expected Response: 401 Unauthorized
```

#### 8.3 Access Other User's Session
```http
# User A creates session (id=1)
POST /api/ai/speaking/sessions (as User A)

# User B tries to access User A's session
GET /api/ai/speaking/sessions/1 (as User B)

Expected Response: 403 Forbidden
```

---

## 📊 Test Checklist

### TASK 14: SpeakingSessionController
- [ ] POST - Create session (201)
- [ ] GET - List sessions (200, paginated)
- [ ] GET - Filter by status (200)
- [ ] GET - Filter by level (200)
- [ ] GET - Get session detail (200)
- [ ] GET - Latest in-progress (200)
- [ ] GET - Top sessions by score (200)
- [ ] PUT - Complete session (200)
- [ ] PUT - Cancel session (200)
- [ ] DELETE - Delete session (204)
- [ ] Security: Ownership verification (403)

### TASK 15: SpeakingPracticeController
- [ ] POST - Submit attempt with audio (201)
- [ ] GET - Get practice session (200)
- [ ] Validation: Invalid audio file (400)
- [ ] Validation: Session not found (404)
- [ ] Audio file multipart upload works
- [ ] Response includes: attempt, scores, feedback, statistics

### TASK 16: SpeakingFeedbackController
- [ ] GET - Get attempt feedback (200)
- [ ] GET - Get session feedback (200/404)
- [ ] GET - List feedback in session (200, paginated)
- [ ] GET - Get best attempt (200/404)
- [ ] GET - Sorted by score (200)
- [ ] GET - Sorted by grammar (200)
- [ ] GET - Sorted by vocabulary (200)
- [ ] GET - Statistics (200)

### TASK 17: SpeakingProgressController
- [ ] GET - Full progress (200)
- [ ] GET - Progress summary (200)
- [ ] GET - Trend 7 days (200)
- [ ] GET - Trend 30 days (200)
- [ ] GET - Trend invalid days (400)
- [ ] GET - Milestones (200)

### Security & Error Handling
- [ ] Missing authorization (401)
- [ ] Invalid token (401)
- [ ] Ownership verification (403)
- [ ] Not found (404)
- [ ] Invalid input (400)
- [ ] Response format JSON correct
- [ ] Swagger docs accessible

---

## 🚀 How to Run Tests

### Option 1: Postman GUI
1. Import `postman-collection.json`
2. Set up `postman-environment.json`
3. Run each request manually or use Postman Runner

### Option 2: Thunder Client (VS Code)
1. Install Thunder Client extension
2. Import collection
3. Test each endpoint

### Option 3: cURL Commands
```bash
# Get sessions
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/ai/speaking/sessions

# Create session
curl -X POST \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"topic":"My Routine","level":"B1"}' \
  http://localhost:8080/api/ai/speaking/sessions

# Submit audio
curl -X POST \
  -H "Authorization: Bearer {token}" \
  -F "sessionId=1" \
  -F "audioFile=@test-audio.mp3" \
  http://localhost:8080/api/ai/speaking/attempts
```

### Option 4: Automated Testing (Newman)
```bash
npm install -g newman
newman run postman-collection.json \
  -e postman-environment.json
```

---

## 📝 Notes

### About Gemini AI Integration
- Những API calls sử dụng AI (STT, Analysis) cần `GEMINI_API_KEY` 
- Nếu không có API key, cách hay là mock responses hoặc skip những test này
- Những test khác (session management, feedback retrieval) không cần API key

### Recommendations
1. Kiểm tra logs: `mvn spring-boot:run` để xem detailed errors
2. Use Swagger UI để explore endpoints interactively
3. Verify database state: `SELECT * FROM ai_speaking_sessions;`
4. Check Redis: `redis-cli` để xem cached data

### For Next Phase
- Integrate với CI/CD pipeline
- Create integration tests
- Load testing
- Real audio file testing

---

## ✅ Pass Criteria

**MVP PASS:** Tất cả 32+ endpoints:
1. ✅ Respond với correct HTTP status code
2. ✅ Return valid JSON responses
3. ✅ Verify ownership checks work
4. ✅ Pagination works correctly
5. ✅ Error handling returns appropriate errors
6. ✅ JWT authentication required
7. ✅ No compilation/runtime errors

---

**Next Task:** TASK 19 - Documentation & Phase 8 Completion  
**Estimated Time:** 2-3 hours for full manual testing
