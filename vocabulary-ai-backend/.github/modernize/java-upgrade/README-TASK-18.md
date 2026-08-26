# TASK 18: MVP Verification - AI Speaking Module Testing

**Phase:** Phase 8 - AI Speaking Module  
**Task:** 18/19  
**Status:** Ready for Manual Testing  
**Scope:** Happy path testing of all 32+ REST API endpoints  

---

## 📋 Files in This Directory

### 1. **TASK-18-MVP-TESTING.md** (Comprehensive Guide)
- Complete test scenarios for all endpoints
- Request/response examples
- Error handling cases
- Test checklist
- Pass criteria

**Start here for detailed testing procedures.**

### 2. **TASK-18-QUICK-START.md** (5-Minute Setup)
- Quick authentication setup
- Happy path flow (5 key tests)
- Validation checklist
- Troubleshooting guide
- Test results template

**Start here for quick testing.**

### 3. **postman-collection-speaking.json** (Postman Collection)
- Pre-configured requests for all 32+ endpoints
- Organized by TASK (14-17)
- Security tests included
- Ready to import and run

**Import into Postman for GUI testing.**

### 4. **postman-environment.json** (Postman Environment)
- Base URL configuration
- Variable placeholders
- User credentials

**Import into Postman alongside collection.**

### 5. **TASK-18-CURL-COMMANDS.sh** (Command-Line Testing)
- Bash script with cURL commands
- Full test flow automation
- Color-coded output
- Security validation

**Run manually or execute script: `./TASK-18-CURL-COMMANDS.sh`**

---

## 🚀 Quick Start (Choose One)

### Option A: Postman (Recommended for Beginners)
```
1. Open Postman
2. File → Import → postman-collection-speaking.json
3. File → Import → postman-environment.json
4. Get auth token (Authentication → Login)
5. Collections → Run
```
**Time:** 5-10 minutes  
**Skill:** Beginner-friendly

### Option B: Quick Manual Test (5 minutes)
```
1. Start application: mvn spring-boot:run
2. Open: TASK-18-QUICK-START.md
3. Follow 5-request happy path
4. Use provided cURL commands
```
**Time:** 5 minutes  
**Skill:** Intermediate

### Option C: Command-Line Automation (10 minutes)
```
1. chmod +x TASK-18-CURL-COMMANDS.sh
2. ./TASK-18-CURL-COMMANDS.sh
3. Review colored output
4. Document results
```
**Time:** 10 minutes  
**Skill:** Advanced

### Option D: Swagger UI (Interactive)
```
1. Open: http://localhost:8080/swagger-ui.html
2. Authorize with JWT token
3. Test endpoints interactively
4. View request/response format
```
**Time:** 15 minutes  
**Skill:** Beginner-friendly

---

## 📊 Testing Scope

### Endpoints Covered (32+)

#### TASK 14: Speaking Sessions (10)
- ✓ Create session
- ✓ Get all sessions (paginated)
- ✓ Filter by status
- ✓ Filter by level
- ✓ Get session detail
- ✓ Get latest in-progress
- ✓ Get top sessions
- ✓ Complete session
- ✓ Cancel session
- ✓ Delete session

#### TASK 15: Speaking Practice (2)
- ✓ Submit audio attempt
- ✓ Get practice session info

#### TASK 16: Speaking Feedback (8)
- ✓ Get attempt feedback
- ✓ Get session feedback
- ✓ List feedback (paginated)
- ✓ Get best feedback
- ✓ Sort by score
- ✓ Sort by grammar
- ✓ Sort by vocabulary
- ✓ Get statistics

#### TASK 17: Speaking Progress (5)
- ✓ Get full progress
- ✓ Get summary (dashboard)
- ✓ Get 7-day trend
- ✓ Get 30-day trend
- ✓ Get milestones

#### Security Tests (4)
- ✓ Missing authorization (401)
- ✓ Invalid token (401)
- ✓ Not found (404)
- ✓ Invalid parameter (400)

---

## ✅ Test Checklist

### Before Testing
- [ ] Application running on http://localhost:8080
- [ ] MySQL database connected (datn_2)
- [ ] Valid user account exists
- [ ] Postman/cURL available
- [ ] Read TASK-18-QUICK-START.md

### During Testing
- [ ] Authenticate (get JWT token)
- [ ] Create session
- [ ] Submit audio attempt
- [ ] Verify response format
- [ ] Check error handling
- [ ] Test pagination
- [ ] Verify ownership checks

### After Testing
- [ ] All endpoints return correct HTTP status codes
- [ ] Response JSON is valid
- [ ] Error responses have appropriate messages
- [ ] JWT authentication works
- [ ] Pagination works correctly
- [ ] Security checks working (401, 403, 404)
- [ ] Document results

---

## 🔧 Prerequisites

### 1. Running Application
```bash
cd d:\demo-datn\vocabulary-ai-backend
mvn spring-boot:run
```

Wait for:
```
Tomcat started on port(s): 8080 (http)
```

### 2. Database
```bash
mysql -u root -p180703 datn_2 -e "SELECT 1;"
```

### 3. Valid User
- Email: test.user@example.com
- Password: your-password
- (Or use existing test user)

### 4. Optional: Audio File
```bash
# Generate test audio using ffmpeg
ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 5 -q:a 9 -acodec libmp3lame test-audio.mp3
```

---

## 📝 Test Flow

### Happy Path (Recommended for MVP)
1. **Authenticate** → Get JWT token
2. **Create Session** → Get session ID
3. **Submit Audio** → Get attempt ID
4. **Get Feedback** → Verify scores
5. **Get Progress** → Verify statistics
6. **Complete Session** → Mark finished

### Error Path (For Validation)
1. Missing auth header → 401
2. Invalid token → 401
3. Session not found → 404
4. Invalid parameter → 400

### Pagination Path
1. List sessions (page 0)
2. Verify pagination metadata
3. Get next page
4. Verify sorting

---

## 🎯 Pass Criteria

**MVP Testing PASS requires:**

1. ✅ All endpoints respond with correct HTTP status codes
2. ✅ Response JSON is valid and parseable
3. ✅ Required fields present in responses
4. ✅ Error handling returns appropriate errors (401, 403, 404, 400)
5. ✅ JWT authentication working
6. ✅ Ownership verification working
7. ✅ Pagination working correctly
8. ✅ No runtime exceptions
9. ✅ No compilation errors
10. ✅ Build succeeds: `mvn package -DskipTests`

---

## 🔍 Common Issues & Solutions

### Issue: Cannot connect to localhost:8080
**Solution:** Start application with `mvn spring-boot:run`

### Issue: 401 Unauthorized
**Solution:** Token expired or invalid. Get new token from login endpoint.

### Issue: 403 Forbidden
**Solution:** Accessing another user's session. Verify using same user's token.

### Issue: 404 Not Found
**Solution:** Resource doesn't exist. Create new session/attempt first.

### Issue: Audio upload fails
**Solution:** Ensure audio file is valid. Generate with ffmpeg:
```bash
ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 5 -q:a 9 -acodec libmp3lame test-audio.mp3
```

### Issue: Connection timeout
**Solution:** Check MySQL is running and accessible

---

## 📈 Testing Timeline

| Phase | Time | Tasks |
|-------|------|-------|
| Setup | 5 min | Start app, verify DB, get token |
| Happy Path | 10 min | Create session, upload audio, get feedback |
| Full Suite | 20 min | Test all 32+ endpoints |
| Error Cases | 5 min | Test 401, 403, 404, 400 responses |
| Documentation | 10 min | Record results, note any issues |
| **Total** | **50 min** | Full MVP verification |

---

## 📊 Test Results Documentation

After testing, document results:

1. Copy **Test Results Template** from TASK-18-QUICK-START.md
2. Fill in all test results
3. Note any issues found
4. Get sign-off from team

---

## 🔗 Related Documents

- **TASK-18-MVP-TESTING.md** - Detailed scenarios & examples
- **TASK-18-QUICK-START.md** - Fast 5-minute setup
- **postman-collection-speaking.json** - Postman import file
- **postman-environment.json** - Environment variables

---

## 📞 Support

### If Tests Fail
1. Check application logs: `mvn spring-boot:run`
2. Verify database: `mysql datn_2`
3. Check JWT token expiration
4. Review error message in response
5. Check TASK-18-MVP-TESTING.md for expected responses

### If You Need Help
1. Check Swagger UI: http://localhost:8080/swagger-ui.html
2. Review error response
3. Check application logs for stack trace
4. Compare with expected response in testing guide

---

## ✨ After MVP Testing

### If All Tests Pass ✅
- Proceed to **TASK 19: Documentation & Completion**
- Prepare Phase 8 summary
- Create release notes
- Archive test results

### If Issues Found 🔴
1. Document the issue
2. Find root cause in logs
3. Fix the bug
4. Re-test affected endpoint
5. Document resolution

---

## 🎓 Learning Resources

### API Documentation
- OpenAPI Spec: http://localhost:8080/v3/api-docs
- Swagger UI: http://localhost:8080/swagger-ui.html

### Spring Boot
- [Spring Boot REST API Documentation](https://spring.io/guides/gs/rest-service/)
- [Spring Security with JWT](https://spring.io/guides/tutorials/spring-boot-oauth2/)

### Testing Tools
- [Postman Learning Center](https://learning.postman.com/)
- [cURL Documentation](https://curl.se/docs/)
- [Thunder Client Docs](https://www.thunderclient.com/docs)

---

## 📝 Notes

- Tests focus on **happy path and happy error paths** only
- Integration with Gemini AI uses mock/stubbed responses for MVP
- Full integration testing deferred to Final Testing phase
- Unit tests deferred to Final Testing phase
- Performance testing not included in MVP

---

## ✅ Sign-Off

- **Task:** TASK 18 - MVP Verification
- **Status:** Ready for Manual Testing
- **Build:** ✅ mvn package -DskipTests SUCCESS
- **Endpoints:** 32+ controllers implemented
- **Expected Duration:** 45-60 minutes for full test suite

**Ready to start testing? Choose a guide above and begin!**

---

**Next Task:** TASK 19 - Documentation & Phase 8 Completion
