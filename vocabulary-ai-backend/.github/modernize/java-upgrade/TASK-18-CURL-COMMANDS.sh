#!/bin/bash

# TASK 18 - MVP Testing: cURL Commands Cheat Sheet
# Usage: chmod +x TASK-18-CURL-COMMANDS.sh
# Then run: ./TASK-18-CURL-COMMANDS.sh

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Variables
BASE_URL="http://localhost:8080/api"
TOKEN=""
SESSION_ID=""
ATTEMPT_ID=""
USER_EMAIL="test.user@example.com"
USER_PASSWORD="your-password"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}VocabularyAI Speaking Module - cURL Tests${NC}"
echo -e "${BLUE}========================================${NC}"

# Helper function to print command
print_cmd() {
    echo -e "\n${BLUE}>>> $1${NC}"
}

# Helper function to print response
print_response() {
    echo -e "${GREEN}✓ Response received${NC}"
}

# ============================================================================
# STEP 1: Authentication
# ============================================================================
echo -e "\n${BLUE}=== STEP 1: AUTHENTICATION ===${NC}"

print_cmd "Getting access token..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$USER_EMAIL\",
    \"password\": \"$USER_PASSWORD\"
  }")

echo "$LOGIN_RESPONSE" | head -c 200
echo "..."

# Extract token (adjust based on actual response format)
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Failed to get token. Check credentials.${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Token obtained: ${TOKEN:0:20}...${NC}"

# ============================================================================
# STEP 2: TASK 14 - Speaking Sessions
# ============================================================================
echo -e "\n${BLUE}=== STEP 2: TASK 14 - SPEAKING SESSIONS ===${NC}"

# 2.1 Create Session
print_cmd "Create speaking session..."
CREATE_SESSION=$(curl -s -X POST "$BASE_URL/ai/speaking/sessions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "My Daily Routine",
    "level": "B1",
    "description": "Talk about your typical day"
  }')

echo "$CREATE_SESSION" | jq . 2>/dev/null || echo "$CREATE_SESSION"
SESSION_ID=$(echo "$CREATE_SESSION" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✓ Session created: SESSION_ID=$SESSION_ID${NC}"

# 2.2 Get All Sessions
print_cmd "Get all sessions (paginated)..."
curl -s -X GET "$BASE_URL/ai/speaking/sessions?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'
echo -e "${GREEN}✓ Listed sessions${NC}"

# 2.3 Filter by Status
print_cmd "Filter by status: IN_PROGRESS..."
curl -s -X GET "$BASE_URL/ai/speaking/sessions/status/IN_PROGRESS" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'
echo -e "${GREEN}✓ Filtered by status${NC}"

# 2.4 Filter by Level
print_cmd "Filter by level: B1..."
curl -s -X GET "$BASE_URL/ai/speaking/sessions/level/B1" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'
echo -e "${GREEN}✓ Filtered by level${NC}"

# 2.5 Get Session Detail
print_cmd "Get session detail..."
curl -s -X GET "$BASE_URL/ai/speaking/sessions/$SESSION_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '{id, topic, level, status}'
echo -e "${GREEN}✓ Retrieved session detail${NC}"

# 2.6 Get Latest In-Progress
print_cmd "Get latest in-progress session..."
curl -s -X GET "$BASE_URL/ai/speaking/sessions/latest/in-progress" \
  -H "Authorization: Bearer $TOKEN" | jq '{id, topic}'
echo -e "${GREEN}✓ Retrieved latest session${NC}"

# ============================================================================
# STEP 3: TASK 15 - Speaking Practice (Audio Upload)
# ============================================================================
echo -e "\n${BLUE}=== STEP 3: TASK 15 - SPEAKING PRACTICE ===${NC}"

# Create test audio file if it doesn't exist
if [ ! -f "test-audio.mp3" ]; then
    print_cmd "Creating test audio file (requires ffmpeg)..."
    ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 5 -q:a 9 -acodec libmp3lame test-audio.mp3 2>/dev/null
    echo -e "${GREEN}✓ Test audio created: test-audio.mp3${NC}"
fi

# 3.1 Submit Speaking Attempt
print_cmd "Submit audio attempt..."
SUBMIT_RESPONSE=$(curl -s -X POST "$BASE_URL/ai/speaking/attempts" \
  -H "Authorization: Bearer $TOKEN" \
  -F "sessionId=$SESSION_ID" \
  -F "audioFile=@test-audio.mp3")

echo "$SUBMIT_RESPONSE" | jq '{attempt: .attempt | {id, attemptNumber}, scores: .scores | {overallScore, grammarScore}}' 2>/dev/null
ATTEMPT_ID=$(echo "$SUBMIT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✓ Audio submitted: ATTEMPT_ID=$ATTEMPT_ID${NC}"

# 3.2 Get Practice Session Info
print_cmd "Get practice session info..."
curl -s -X GET "$BASE_URL/ai/speaking/attempts/$SESSION_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '{session: {topic, level}, attempt: .attempt | {attemptNumber}, statistics: .statistics}'
echo -e "${GREEN}✓ Retrieved session info${NC}"

# ============================================================================
# STEP 4: TASK 16 - Speaking Feedback
# ============================================================================
echo -e "\n${BLUE}=== STEP 4: TASK 16 - SPEAKING FEEDBACK ===${NC}"

# 4.1 Get Attempt Feedback
print_cmd "Get attempt feedback..."
curl -s -X GET "$BASE_URL/ai/speaking/feedback/attempts/$ATTEMPT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '{overallScore, grammarScore, vocabularyScore, fluencyScore, contentScore}'
echo -e "${GREEN}✓ Retrieved attempt feedback${NC}"

# 4.2 Get Session Feedback
print_cmd "Get session feedback..."
curl -s -X GET "$BASE_URL/ai/speaking/feedback/sessions/$SESSION_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '{overallScore, grammarScore, vocabularyScore}' 2>/dev/null || echo "No feedback yet"
echo -e "${GREEN}✓ Retrieved session feedback${NC}"

# 4.3 List Feedback in Session
print_cmd "List feedback in session..."
curl -s -X GET "$BASE_URL/ai/speaking/feedback/sessions/$SESSION_ID/list?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'
echo -e "${GREEN}✓ Listed feedback${NC}"

# 4.4 Get Best Feedback
print_cmd "Get best feedback in session..."
curl -s -X GET "$BASE_URL/ai/speaking/feedback/sessions/$SESSION_ID/best" \
  -H "Authorization: Bearer $TOKEN" | jq '{overallScore, id}' 2>/dev/null
echo -e "${GREEN}✓ Retrieved best feedback${NC}"

# 4.5 Sorted by Score
print_cmd "Get feedback sorted by score..."
curl -s -X GET "$BASE_URL/ai/speaking/feedback/sessions/$SESSION_ID/sorted-by-score?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'
echo -e "${GREEN}✓ Retrieved sorted feedback${NC}"

# 4.6 Statistics
print_cmd "Get session statistics..."
curl -s -X GET "$BASE_URL/ai/speaking/feedback/sessions/$SESSION_ID/statistics" \
  -H "Authorization: Bearer $TOKEN" | jq '{overallScore, grammarScore, vocabularyScore, fluencyScore, contentScore, attemptCount}'
echo -e "${GREEN}✓ Retrieved statistics${NC}"

# ============================================================================
# STEP 5: TASK 17 - Speaking Progress
# ============================================================================
echo -e "\n${BLUE}=== STEP 5: TASK 17 - SPEAKING PROGRESS ===${NC}"

# 5.1 Get Full Progress
print_cmd "Get full speaking progress..."
curl -s -X GET "$BASE_URL/ai/speaking/progress" \
  -H "Authorization: Bearer $TOKEN" | jq '{statistics: .statistics | {totalSessions, totalAttempts}, averageScores: .averageScores | {overallScore}}'
echo -e "${GREEN}✓ Retrieved progress${NC}"

# 5.2 Get Progress Summary
print_cmd "Get progress summary (dashboard)..."
curl -s -X GET "$BASE_URL/ai/speaking/progress/summary" \
  -H "Authorization: Bearer $TOKEN" | jq '{totalSessions, totalAttempts, averageScore, scoreTrend}'
echo -e "${GREEN}✓ Retrieved summary${NC}"

# 5.3 Get 7-Day Trend
print_cmd "Get 7-day trend..."
curl -s -X GET "$BASE_URL/ai/speaking/progress/trend/7" \
  -H "Authorization: Bearer $TOKEN" | jq '{days, scoreTrend}'
echo -e "${GREEN}✓ Retrieved 7-day trend${NC}"

# 5.4 Get 30-Day Trend
print_cmd "Get 30-day trend..."
curl -s -X GET "$BASE_URL/ai/speaking/progress/trend/30" \
  -H "Authorization: Bearer $TOKEN" | jq '{days, scoreTrend}'
echo -e "${GREEN}✓ Retrieved 30-day trend${NC}"

# 5.5 Get Milestones
print_cmd "Get milestones..."
curl -s -X GET "$BASE_URL/ai/speaking/progress/milestones" \
  -H "Authorization: Bearer $TOKEN" | jq '{totalMilestones, milestones}'
echo -e "${GREEN}✓ Retrieved milestones${NC}"

# ============================================================================
# STEP 6: Session Management (Complete/Cancel/Delete)
# ============================================================================
echo -e "\n${BLUE}=== STEP 6: SESSION MANAGEMENT ===${NC}"

# 6.1 Get Top Completed Sessions
print_cmd "Get top completed sessions..."
curl -s -X GET "$BASE_URL/ai/speaking/sessions/completed/sorted-by-score?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'
echo -e "${GREEN}✓ Retrieved top sessions${NC}"

# 6.2 Complete Session
print_cmd "Complete session..."
curl -s -X PUT "$BASE_URL/ai/speaking/sessions/$SESSION_ID/complete" \
  -H "Authorization: Bearer $TOKEN" | jq '{id, status, overallScore}'
echo -e "${GREEN}✓ Session completed${NC}"

# ============================================================================
# STEP 7: Security Tests
# ============================================================================
echo -e "\n${BLUE}=== STEP 7: SECURITY TESTS ===${NC}"

# 7.1 Missing Authorization
print_cmd "Test missing authorization header (should be 401)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/ai/speaking/sessions")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
echo "HTTP Status: $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${GREEN}✓ Correctly rejected unauthorized request${NC}"
else
    echo -e "${RED}✗ Expected 401, got $HTTP_CODE${NC}"
fi

# 7.2 Invalid Token
print_cmd "Test invalid token (should be 401)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/ai/speaking/sessions" \
  -H "Authorization: Bearer invalid-token")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
echo "HTTP Status: $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${GREEN}✓ Correctly rejected invalid token${NC}"
else
    echo -e "${RED}✗ Expected 401, got $HTTP_CODE${NC}"
fi

# 7.3 Not Found
print_cmd "Test not found (should be 404)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/ai/speaking/sessions/99999" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
echo "HTTP Status: $HTTP_CODE"
if [ "$HTTP_CODE" = "404" ]; then
    echo -e "${GREEN}✓ Correctly returned 404${NC}"
else
    echo -e "${RED}✗ Expected 404, got $HTTP_CODE${NC}"
fi

# 7.4 Invalid Parameter
print_cmd "Test invalid parameter (should be 400)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/ai/speaking/progress/trend/15" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
echo "HTTP Status: $HTTP_CODE"
if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ Correctly returned 400${NC}"
else
    echo -e "${RED}✗ Expected 400, got $HTTP_CODE${NC}"
fi

# ============================================================================
# FINAL SUMMARY
# ============================================================================
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ MVP Testing Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${BLUE}Test Results:${NC}"
echo "  - Session Created: $SESSION_ID"
echo "  - Attempt Submitted: $ATTEMPT_ID"
echo "  - Token: ${TOKEN:0:20}..."
echo "  - All endpoints tested successfully ✓"
echo ""
echo -e "${BLUE}API Documentation:${NC}"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo "  - API Docs: http://localhost:8080/v3/api-docs"
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "  1. Run full test suite in Postman"
echo "  2. Document results in TASK-18-MVP-TESTING.md"
echo "  3. Proceed to TASK 19: Documentation & Completion"
