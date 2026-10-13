# VocabularyAI API Documentation

Complete API documentation for all endpoints integrated in the VocabularyAI application.

## Table of Contents

1. [AI Endpoints](#ai-endpoints)
   - [Vocabulary](#vocabulary)
   - [Grammar](#grammar)
   - [Chat](#chat)
   - [Hints](#hints)
   - [Personalization](#personalization)
   - [Speaking](#speaking)

2. [Response Format](#response-format)
3. [Error Handling](#error-handling)
4. [Rate Limiting](#rate-limiting)

---

## AI Endpoints

### Vocabulary

#### Explain Word

Get detailed explanation for a single word.

```
POST /api/v1/ai/vocabulary/explain
Content-Type: application/json

Request Body:
{
  "word": "example",
  "level": "B1",          // Optional: A1-C2
  "context": "..."        // Optional: context for better explanation
}

Response:
{
  "success": true,
  "data": {
    "wordId": 1,
    "word": "example",
    "explanation": "A thing characteristic of its kind...",
    "examples": ["Can you give me an example?", "..."],
    "etymology": "From Latin exemplum...",
    "relatedWords": ["instance", "specimen", "sample"],
    "partOfSpeech": "noun"
  }
}
```

**Status Codes:**
- 200: Success
- 400: Invalid request
- 401: Unauthorized
- 429: Rate limited
- 500: Server error

#### Explain Words (Batch)

Get explanations for multiple words in one request.

```
POST /api/v1/ai/vocabulary/explain-batch
Content-Type: application/json

Request Body:
{
  "words": [
    { "word": "example", "level": "B1" },
    { "word": "instance", "level": "B1" }
  ]
}

Response:
{
  "success": true,
  "data": [
    { "wordId": 1, "word": "example", ... },
    { "wordId": 2, "word": "instance", ... }
  ]
}
```

#### Clear Cache

Clear vocabulary cache on server.

```
POST /api/v1/ai/vocabulary/cache-clear

Response:
{
  "success": true,
  "message": "Vocabulary cache cleared"
}
```

---

### Grammar

#### Check Grammar

Check grammar for a text.

```
POST /api/v1/ai/grammar/check
Content-Type: application/json

Request Body:
{
  "text": "The examples is very helpful."
}

Response:
{
  "success": true,
  "data": {
    "originalText": "The examples is very helpful.",
    "correctedText": "The examples are very helpful.",
    "errors": [
      {
        "position": 12,
        "errorType": "GRAMMAR",
        "original": "is",
        "correction": "are",
        "explanation": "Subject-verb agreement: plural subject requires plural verb"
      }
    ],
    "suggestions": [
      {
        "title": "Consider a more formal tone",
        "content": "...",
        "priority": "LOW"
      }
    ]
  }
}
```

#### Check Grammar (Batch)

Check grammar for multiple texts.

```
POST /api/v1/ai/grammar/check-batch
Content-Type: application/json

Request Body:
{
  "texts": ["Text 1", "Text 2", "Text 3"]
}

Response:
{
  "success": true,
  "data": [
    { "originalText": "...", "correctedText": "...", ... },
    { "originalText": "...", "correctedText": "...", ... }
  ]
}
```

---

### Chat

#### Create Conversation

Create a new conversation.

```
POST /api/v1/ai/chat/conversations
Content-Type: application/json

Request Body:
{
  "title": "English Conversation",
  "type": "LEARNING"  // LEARNING, PRACTICE, or FEEDBACK
}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "title": "English Conversation",
    "type": "LEARNING",
    "summary": null,
    "createdAt": "2024-02-01T10:00:00Z",
    "updatedAt": "2024-02-01T10:00:00Z"
  }
}
```

#### Get Conversations

Get list of all conversations.

```
GET /api/v1/ai/chat/conversations

Response:
{
  "success": true,
  "data": [
    { "id": 1, "title": "...", "type": "LEARNING", ... },
    { "id": 2, "title": "...", "type": "PRACTICE", ... }
  ]
}
```

#### Get Conversation Detail

Get conversation with full message history.

```
GET /api/v1/ai/chat/conversations/{conversationId}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "title": "English Conversation",
    "type": "LEARNING",
    "summary": "Discussing daily life",
    "createdAt": "...",
    "updatedAt": "...",
    "isArchived": false,
    "messages": [
      {
        "id": 1,
        "conversationId": 1,
        "content": "Hello",
        "role": "USER",
        "timestamp": "..."
      },
      {
        "id": 2,
        "conversationId": 1,
        "content": "Hi there!",
        "role": "ASSISTANT",
        "timestamp": "..."
      }
    ]
  }
}
```

#### Send Message

Send a message in a conversation.

```
POST /api/v1/ai/chat/conversations/{conversationId}/messages
Content-Type: application/json

Request Body:
{
  "content": "How are you?"
}

Response:
{
  "success": true,
  "data": {
    "id": 3,
    "conversationId": 1,
    "content": "I'm great, thank you!",
    "role": "ASSISTANT",
    "timestamp": "..."
  }
}
```

#### Delete Conversation

Delete a conversation.

```
DELETE /api/v1/ai/chat/conversations/{conversationId}

Response:
{
  "success": true,
  "message": "Conversation deleted"
}
```

---

### Hints

#### Generate Hint

Generate a hint for a word.

```
POST /api/v1/ai/hints/generate
Content-Type: application/json

Request Body:
{
  "wordId": 1,
  "level": "B1"  // Optional
}

Response:
{
  "success": true,
  "data": {
    "learningProgressId": 1,
    "hint": "Think of a situation where this word would be used...",
    "difficulty": "MEDIUM"
  }
}
```

#### Generate Hints (Batch)

Generate hints for multiple words.

```
POST /api/v1/ai/hints/generate-batch
Content-Type: application/json

Request Body:
{
  "hints": [
    { "wordId": 1 },
    { "wordId": 2 }
  ]
}

Response:
{
  "success": true,
  "data": [
    { "learningProgressId": 1, "hint": "...", ... },
    { "learningProgressId": 2, "hint": "...", ... }
  ]
}
```

---

### Personalization

#### Get Profile

Get user profile with personalization settings.

```
GET /api/v1/ai/personalization/profile

Response:
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "learner",
    "email": "learner@example.com",
    "nativeLanguage": "Vietnamese",
    "targetLanguage": "English",
    "proficiencyLevel": "B1",
    "learningStyle": "visual",
    "dailyGoal": 30,
    "joinedAt": "2024-01-15T10:00:00Z",
    "lastActive": "...",
    "totalLearningMinutes": 1250,
    "currentStreak": 15,
    "longestStreak": 45
  }
}
```

#### Get Learning Plan

Get personalized learning plan.

```
GET /api/v1/ai/personalization/learning-plan?weeks=4

Response:
{
  "success": true,
  "data": {
    "planId": 1,
    "userId": 1,
    "startDate": "2024-02-01",
    "endDate": "2024-02-28",
    "totalDays": 28,
    "completedDays": 12,
    "weeklyHours": 7,
    "focusAreas": ["Business English", "Speaking"],
    "items": [...],
    "overallProgress": 42
  }
}
```

#### Get Progress Analysis

Get progress analysis and metrics.

```
GET /api/v1/ai/personalization/progress-analysis?daysBack=30

Response:
{
  "success": true,
  "data": {
    "userId": 1,
    "analysisDate": "...",
    "overallProgress": 65,
    "weeklyProgress": [...],
    "metrics": [
      {
        "name": "Vocabulary Mastery",
        "value": 450,
        "target": 500,
        "progress": 90,
        "trend": "up"
      }
    ],
    "estimatedNextLevelDate": "...",
    "estimatedCompletionDate": "..."
  }
}
```

#### Get Recommendations

Get personalized learning recommendations.

```
GET /api/v1/ai/personalization/recommendations?limit=5

Response:
{
  "success": true,
  "data": {
    "userId": 1,
    "generatedAt": "...",
    "recommendations": [
      {
        "id": 1,
        "type": "vocabulary",
        "title": "Business Email Writing",
        "description": "Learn vocabulary for professional emails",
        "reason": "Based on your weak areas",
        "difficulty": "B2",
        "priority": "high",
        "content": { "words": [...] },
        "estimatedMinutes": 20
      }
    ],
    "nextReviewDate": "..."
  }
}
```

#### Get Weakness/Strength

Get weakness and strength analysis.

```
GET /api/v1/ai/personalization/weakness-strength

Response:
{
  "success": true,
  "data": {
    "userId": 1,
    "analysisDate": "...",
    "overallProficiency": 75,
    "strengths": [
      {
        "skillName": "Reading Comprehension",
        "proficiency": 88,
        "level": "advanced",
        "accuracy": 92,
        "averageResponseTime": 2500,
        "attemptCount": 45,
        "successCount": 41,
        "lastPracticed": "...",
        "recommendedAction": "Continue with advanced materials"
      }
    ],
    "weaknesses": [...],
    "topicBreakdown": [...],
    "recommendations": [...]
  }
}
```

#### Get Adaptive Difficulty

Get adaptive difficulty settings.

```
GET /api/v1/ai/personalization/adaptive-difficulty

Response:
{
  "success": true,
  "data": {
    "userId": 1,
    "currentLevel": "B1",
    "nextLevel": "B2",
    "progressToNextLevel": 65,
    "adjustmentRecommended": false,
    "availableLevels": [...],
    "performanceHistory": [...]
  }
}
```

#### Get Daily Practice

Get daily practice exercises.

```
GET /api/v1/ai/personalization/daily-practice?date=2024-02-01

Response:
{
  "success": true,
  "data": {
    "date": "2024-02-01",
    "totalEstimatedMinutes": 30,
    "completedMinutes": 18,
    "items": [
      {
        "id": 1,
        "type": "vocabulary",
        "title": "Daily Vocabulary",
        "description": "Learn 5 new words",
        "estimatedMinutes": 10,
        "difficulty": "B1",
        "content": "Word 1, Word 2, ...",
        "completed": true,
        "score": 95
      }
    ],
    "streakDays": 15,
    "motivation": "Great job! Keep it up!",
    "nextChallenge": "..."
  }
}
```

---

### Speaking

#### Create Speaking Session

Create a new speaking practice session.

```
POST /api/v1/ai/speaking/sessions
Content-Type: application/json

Request Body:
{
  "topic": "Restaurant ordering",
  "difficulty": "B1",
  "title": "My first session",
  "description": "Learn how to order at a restaurant"
}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "title": "My first session",
    "description": "...",
    "topic": "Restaurant ordering",
    "difficulty": "B1",
    "totalQuestions": 5,
    "currentQuestion": 0,
    "duration": 300,
    "createdAt": "...",
    "status": "active"
  }
}
```

#### Get Speaking Sessions

Get list of all speaking sessions.

```
GET /api/v1/ai/speaking/sessions?page=1&pageSize=10&status=active

Response:
{
  "success": true,
  "data": {
    "sessions": [
      { "id": 1, "title": "...", ... },
      { "id": 2, "title": "...", ... }
    ],
    "total": 2,
    "page": 1,
    "pageSize": 10
  }
}
```

#### Get Speaking Session

Get a specific speaking session.

```
GET /api/v1/ai/speaking/sessions/{sessionId}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "title": "My first session",
    "topic": "Restaurant ordering",
    "difficulty": "B1",
    "totalQuestions": 5,
    "currentQuestion": 2,
    "duration": 300,
    "createdAt": "...",
    "status": "active"
  }
}
```

#### Submit Speaking Answer

Submit an audio answer to a speaking question.

```
POST /api/v1/ai/speaking/sessions/{sessionId}/answers
Content-Type: multipart/form-data

Form Data:
- questionId: 1
- audio: [audio blob]
- duration: 5
- text: "I would like to order the grilled salmon"

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "sessionId": 1,
    "questionId": 1,
    "audioUrl": "blob:...",
    "text": "I would like to order the grilled salmon",
    "duration": 5,
    "submittedAt": "...",
    "feedback": { "score": 85, ... }
  }
}
```

#### Get Speaking Feedback

Get feedback for a speaking session.

```
GET /api/v1/ai/speaking/sessions/{sessionId}/feedback

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "answerId": 1,
    "score": 85,
    "accuracy": 88,
    "fluency": 82,
    "explanation": "Good pronunciation and grammar...",
    "corrections": [
      {
        "type": "pronunciation",
        "original": "grilled",
        "suggestion": "pronounced as 'grild'",
        "explanation": "..."
      }
    ],
    "strengths": ["Clear articulation", "Correct grammar"],
    "areasToImprove": ["Intonation", "Speaking pace"]
  }
}
```

#### Complete Speaking Session

Complete a speaking session.

```
POST /api/v1/ai/speaking/sessions/{sessionId}/complete

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "title": "My first session",
    "status": "completed",
    "score": 87,
    "accuracy": 88,
    "completedAt": "..."
  }
}
```

---

## Response Format

All API responses follow a standard format:

```json
{
  "success": true|false,
  "data": {},
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message",
    "details": {}
  }
}
```

### Success Response

```json
{
  "success": true,
  "data": {
    // Response data
  }
}
```

### Error Response

```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Invalid request parameters",
    "details": {
      "field": "word",
      "reason": "Word cannot be empty"
    }
  }
}
```

---

## Error Handling

### Common Error Codes

| Code | HTTP Status | Description |
|------|------------|-------------|
| INVALID_REQUEST | 400 | Invalid request parameters |
| UNAUTHORIZED | 401 | Authentication required |
| FORBIDDEN | 403 | Access denied |
| NOT_FOUND | 404 | Resource not found |
| RATE_LIMITED | 429 | Too many requests |
| SERVER_ERROR | 500 | Internal server error |
| AI_SERVICE_UNAVAILABLE | 503 | AI service temporarily unavailable |

---

## Rate Limiting

API implements rate limiting per user:

- **Vocabulary**: 100 requests per hour
- **Grammar**: 100 requests per hour
- **Chat**: 50 requests per hour
- **Hints**: 100 requests per hour
- **Speaking**: 20 requests per hour
- **Personalization**: 50 requests per hour

Rate limit headers:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1706755200
```

---

## Authentication

All endpoints require authentication via JWT token:

```
Authorization: Bearer {token}
```

Token should be included in request headers.

---

## Caching Strategy

**Client-side caching (React Query):**
- Vocabulary explanations: 10 minutes
- Grammar checks: 10 minutes
- Chat conversations: 5 minutes
- Personalization data: 10 minutes
- Speaking sessions: 5 minutes

**Server-side caching:**
- Vocabulary explanations: 24 hours
- Grammar checks: 24 hours
- Personalization data: 1 hour

---

## Version History

### Version 1.0.0 (2024-02-01)
- Initial release with all AI modules
- Full personalization support
- Speaking practice module
- Comprehensive documentation

