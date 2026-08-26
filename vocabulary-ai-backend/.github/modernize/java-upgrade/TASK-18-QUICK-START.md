# TASK 18 - MVP Testing: Quick Start Guide

**Duration:** 30-45 minutes  
**Tools:** Postman, cURL, or Thunder Client  
**Prerequisite:** Application running on `http://localhost:8080`

---

## ⚡ 5-Minute Setup

### Step 1: Start the Application
```bash
cd d:\demo-datn\vocabulary-ai-backend
mvn spring-boot:run
```

Wait for startup message:
```
Tomcat started on port(s): 8080 (http)
```

### Step 2: Verify Database Connection
```bash
# Check MySQL connection (in separate terminal)
mysql -u root -p180703 datn_2 -e "SELECT 1;"
```

### Step 3: Check API Health
```bash
# Browser or curl
curl http://localhost:8080/health
```

---

## 🔑 Step 1: Authentication

### Get Access Token

**Option A: Using Postman**
1. Import `postman-collection-speaking.json`
2. Import `postman-environment.json`
3. Go to "Authentication" folder → "Login" request
4. Click Send
5. Copy `accessToken` from response
6. Go to environment and paste into `ACCESS_TOKEN` variable

**Option B: Using cURL**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "your-password"
  }'

# Save the accessToken
TOKEN="your-access-token-here"
```

---

## 🎯 Step 2: Test Happy Path (5 requests)

### Test 1: Create Session
```bash
curl -X POST http://localhost:8080/api/ai/speaking/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "My Daily Routine",
    "level": "B1",
    "description": "Talk about your typical day"
  }'

# Response:
# {
#   "id": 1,
#   "topic": "My Daily Routine",
#   "level": "B1",
#   "status": "IN_PROGRESS",
#   ...
# }

# Save SESSION_ID=1
```

### Test 2: Get Sessions List
```bash
curl -X GET "http://localhost:8080/api/ai/speaking/sessions?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Verify: totalElements > 0
```

### Test 3: Get Session Detail
```bash
curl -X GET http://localhost:8080/api/ai/speaking/sessions/1 \
  -H "Authorization: Bearer $TOKEN"

# Verify: Returns session with id=1
```

### Test 4: Submit Audio Attempt
```bash
# First, create a test audio file (5 seconds of silence)
# Using ffmpeg:
ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 5 -q:a 9 -acodec libmp3lame test-audio.mp3

# Then upload:
curl -X POST http://localhost:8080/api/ai/speaking/attempts \
  -H "Authorization: Bearer $TOKEN" \
  -F "sessionId=1" \
  -F "audioFile=@test-audio.mp3"

# Response:
# {
#   "attempt": { "id": 1, ... },
#   "scores": { "overallScore": 75.5, ... },
#   "feedback": { ... },
#   ...
# }

# Save ATTEMPT_ID=1
```

### Test 5: Get Feedback
```bash
curl -X GET http://localhost:8080/api/ai/speaking/feedback/attempts/1 \
  -H "Authorization: Bearer $TOKEN"

# Verify: Returns feedback with scores
```

---

## 📊 Full Test Suite (Use Postman Runner)

### Import Collections
1. **File → Import**
2. Select `postman-collection-speaking.json`
3. Select `postman-environment.json`

### Update Variables
In Postman environment:
- Set `ACCESS_TOKEN` = your JWT token
- Set `SESSION_ID` = 1 (created in Test 1)
- Set `ATTEMPT_ID` = 1 (created in Test 4)

### Run Collection
1. **Collections → Speaking Module**
2. Click **Run** (top-right)
3. Select "Run sequentially"
4. Click **Run**

Expected: All 32+ requests should pass ✅

---

## ✅ Validation Checklist

### TASK 14: Sessions (10 requests)
- [ ] Create session → 201
- [ ] Get all sessions → 200 (paginated)
- [ ] Filter by status → 200
- [ ] Filter by level → 200
- [ ] Get session detail → 200
- [ ] Get latest in-progress → 200
- [ ] Get top sessions → 200
- [ ] Complete session → 200
- [ ] Cancel session → 200
- [ ] Delete session → 204

### TASK 15: Practice (2 requests)
- [ ] Submit audio attempt → 201
  - Check: `attempt.audioDurationSeconds` present
  - Check: `scores.overallScore` present
  - Check: `feedback.feedback` not empty
- [ ] Get practice session → 200
  - Check: `statistics.totalAttempts` = 1

### TASK 16: Feedback (8 requests)
- [ ] Get attempt feedback → 200
- [ ] Get session feedback → 200
- [ ] List feedback in session → 200 (paginated)
- [ ] Get best feedback → 200
- [ ] Sorted by score → 200
- [ ] Sorted by grammar → 200
- [ ] Sorted by vocabulary → 200
- [ ] Get statistics → 200
  - Check: All average scores present

### TASK 17: Progress (5 requests)
- [ ] Get full progress → 200
  - Check: `statistics.totalSessions` = 1
  - Check: `achievements` array present
- [ ] Get summary → 200
- [ ] Trend 7 days → 200
- [ ] Trend 30 days → 200
- [ ] Get milestones → 200

### Security (4 requests)
- [ ] Missing auth header → 401
- [ ] Invalid token → 401
- [ ] Not found → 404
- [ ] Invalid parameter → 400

---

## 🐛 Troubleshooting

### Issue: Connection Refused
**Solution:** Make sure application is running
```bash
mvn spring-boot:run
```

### Issue: 401 Unauthorized
**Solution:** Token has expired or invalid
1. Get new token from login endpoint
2. Update `ACCESS_TOKEN` in Postman environment

### Issue: 403 Forbidden
**Solution:** You're accessing another user's session
- Ensure using same user's token

### Issue: 404 Not Found
**Solution:** Session/attempt doesn't exist
1. Create new session first
2. Update SESSION_ID and ATTEMPT_ID variables

### Issue: 400 Bad Request
**Solution:** Check request format
- Verify JSON is valid
- Check all required fields present
- For multipart: sessionId should be form field, not part of URL

### Issue: Audio Upload Fails
**Solution:** Create proper audio file
```bash
# Generate test audio using ffmpeg
ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 5 -q:a 9 -acodec libmp3lame test-audio.mp3

# Verify file exists
ls -lh test-audio.mp3
```

---

## 📝 Test Results Template

Copy this template to document your test results:

```markdown
# MVP Testing Results - TASK 18

Date: 2026-08-26
Tester: [Your Name]
Application Version: Phase 8

## Test Environment
- OS: Windows
- Server: http://localhost:8080
- Database: MySQL datn_2
- Build: mvn package -DskipTests

## Test Results

### TASK 14: Speaking Sessions
| Endpoint | Method | Status | Notes |
|----------|--------|--------|-------|
| /sessions | POST | ✅ 201 | Created session |
| /sessions | GET | ✅ 200 | Listed 1 session |
| /sessions/1 | GET | ✅ 200 | Got detail |
| /sessions/status/IN_PROGRESS | GET | ✅ 200 | Filtered |
| /sessions/level/B1 | GET | ✅ 200 | Filtered |
| /sessions/latest/in-progress | GET | ✅ 200 | Got latest |
| /sessions/1/complete | PUT | ✅ 200 | Completed |
| /sessions/completed/sorted-by-score | GET | ✅ 200 | Listed |
| /sessions/1/cancel | PUT | ✅ 200 | Cancelled |
| /sessions/1 | DELETE | ✅ 204 | Deleted |

### TASK 15: Speaking Practice
| Endpoint | Method | Status | Notes |
|----------|--------|--------|-------|
| /attempts | POST | ✅ 201 | Uploaded audio |
| /attempts/1 | GET | ✅ 200 | Got session |

### TASK 16: Speaking Feedback
| Endpoint | Method | Status | Notes |
|----------|--------|--------|-------|
| /feedback/attempts/1 | GET | ✅ 200 | Got feedback |
| /feedback/sessions/1 | GET | ✅ 200 | Got session feedback |
| /feedback/sessions/1/list | GET | ✅ 200 | Listed |
| /feedback/sessions/1/best | GET | ✅ 200 | Got best |
| /feedback/sessions/1/sorted-by-score | GET | ✅ 200 | Sorted |
| /feedback/sessions/1/sorted-by-grammar | GET | ✅ 200 | Sorted |
| /feedback/sessions/1/sorted-by-vocabulary | GET | ✅ 200 | Sorted |
| /feedback/sessions/1/statistics | GET | ✅ 200 | Got stats |

### TASK 17: Speaking Progress
| Endpoint | Method | Status | Notes |
|----------|--------|--------|-------|
| /progress | GET | ✅ 200 | Got progress |
| /progress/summary | GET | ✅ 200 | Got summary |
| /progress/trend/7 | GET | ✅ 200 | Got 7-day trend |
| /progress/trend/30 | GET | ✅ 200 | Got 30-day trend |
| /progress/milestones | GET | ✅ 200 | Got milestones |

### Security Tests
| Test | Expected | Result | Notes |
|------|----------|--------|-------|
| Missing auth | 401 | ✅ | Got 401 |
| Invalid token | 401 | ✅ | Got 401 |
| Not found | 404 | ✅ | Got 404 |
| Invalid param | 400 | ✅ | Got 400 |

## Summary
- **Total Endpoints Tested:** 32+
- **Passed:** 32+ ✅
- **Failed:** 0
- **Issues Found:** None
- **Build Status:** ✅ SUCCESS

## Sign-off
- Tested by: [Your Name]
- Date: [Date]
- Environment: Development
- Status: ✅ PASS - Ready for Phase 8 Completion
```

---

## 🚀 Advanced Testing

### Using Thunder Client (VS Code)
1. Install "Thunder Client" extension
2. Import `postman-collection-speaking.json`
3. Set environment variables
4. Run requests

### Using Newman (CLI)
```bash
npm install -g newman

newman run postman-collection-speaking.json \
  -e postman-environment.json \
  -r cli

# Generate HTML report
newman run postman-collection-speaking.json \
  -e postman-environment.json \
  -r html,cli
```

### Swagger UI (Interactive)
1. Open http://localhost:8080/swagger-ui.html
2. Authorize with your token
3. Test each endpoint interactively
4. See real request/response

---

## 📋 Common Test Patterns

### Pattern 1: Full Session Flow
```
1. Create session (GET session id)
2. Submit audio attempt (GET attempt id)
3. Get feedback (verify scores)
4. Get progress (verify stats)
5. Complete session
6. List completed sessions
7. Delete session
```

### Pattern 2: Feedback Analysis
```
1. Create session
2. Submit multiple attempts
3. List feedback sorted by score
4. Get best attempt
5. Get session statistics
```

### Pattern 3: Progress Tracking
```
1. Create multiple sessions over time
2. Get progress (verify stats accumulate)
3. Check milestones
4. Analyze trends
```

---

## ✨ Next Steps After MVP Testing

1. **TASK 19:** Documentation & Completion
2. **Integration Testing Phase:**
   - Unit tests for services
   - Integration tests with Gemini API
   - E2E testing

3. **Deployment Preparation:**
   - Docker containerization
   - CI/CD pipeline setup
   - Production environment config

---

**Questions?** Check TASK-18-MVP-TESTING.md for detailed scenarios and responses.
